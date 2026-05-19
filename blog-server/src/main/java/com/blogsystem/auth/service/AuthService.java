package com.blogsystem.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blogsystem.auth.dto.LoginUserVO;
import com.blogsystem.auth.dto.PhoneCodeAuthRequest;
import com.blogsystem.auth.dto.SendSmsCodeRequest;
import com.blogsystem.auth.entity.SmsCodeLog;
import com.blogsystem.auth.entity.SysRole;
import com.blogsystem.auth.entity.SysUser;
import com.blogsystem.auth.entity.SysUserRole;
import com.blogsystem.auth.mapper.SmsCodeLogMapper;
import com.blogsystem.auth.mapper.SysRoleMapper;
import com.blogsystem.auth.mapper.SysUserMapper;
import com.blogsystem.auth.mapper.SysUserRoleMapper;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证业务 —— 验证码 / 注册 / 登录 / 个人信息 / 登出
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SmsCodeLogMapper smsCodeLogMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 发送短信验证码，开发环境返回 mockCode 明文
     */
    public Map<String, String> sendSmsCode(SendSmsCodeRequest request, String requestIp) {
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        SmsCodeLog log = new SmsCodeLog();
        log.setPhone(request.phone());
        log.setBizType(request.bizType());
        log.setCodeHash(md5(code));
        log.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        log.setStatus(0);
        log.setRequestIp(requestIp);
        smsCodeLogMapper.insert(log);
        return Map.of("phone", request.phone(), "bizType", request.bizType(), "mockCode", code);
    }

    /**
     * 手机验证码注册，自动分配用户名和"用户"角色
     */
    public LoginUserVO registerByPhoneCode(PhoneCodeAuthRequest request) {
        verifyCode(request.phone(), "REGISTER", request.code());
        SysUser exists = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, request.phone())
                .last("limit 1"));
        if (exists != null) {
            throw new IllegalArgumentException("手机号已注册");
        }
        SysUser user = new SysUser();
        user.setPhone(request.phone());
        user.setUsername("u" + request.phone().substring(3));
        user.setPassword(md5(request.phone() + "#init"));
        user.setNickname("用户" + request.phone().substring(7));
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        SysRole userRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "USER")
                .eq(SysRole::getDeleted, 0)
                .last("limit 1"));
        if (userRole != null) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(user.getId());
            relation.setRoleId(userRole.getId());
            sysUserRoleMapper.insert(relation);
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        return new LoginUserVO(user.getId(), user.getPhone(), user.getUsername(), user.getNickname(), null, null, token);
    }

    /**
     * 手机验证码登录，更新最后登录时间
     */
    public LoginUserVO loginByPhoneCode(PhoneCodeAuthRequest request) {
        verifyCode(request.phone(), "LOGIN", request.code());
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, request.phone())
                .eq(SysUser::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new IllegalArgumentException("用户不存在，请先注册");
        }
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        return new LoginUserVO(user.getId(), user.getPhone(), user.getUsername(), user.getNickname(), user.getEmail(), user.getAvatar(), token);
    }

    /**
     * 获取当前登录用户信息
     */
    public LoginUserVO currentUser() {
        Long loginId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(loginId);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("用户不存在");
        }
        return new LoginUserVO(user.getId(), user.getPhone(), user.getUsername(),
                user.getNickname(), user.getEmail(), user.getAvatar(), StpUtil.getTokenValue());
    }

    /**
     * 更新个人信息（昵称 / 邮箱 / 头像）
     */
    public LoginUserVO updateProfile(Long userId, String nickname, String email, String avatar) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        if (avatar != null) user.setAvatar(avatar);
        sysUserMapper.updateById(user);
        return new LoginUserVO(user.getId(), user.getPhone(), user.getUsername(),
                user.getNickname(), user.getEmail(), user.getAvatar(), StpUtil.getTokenValue());
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 校验手机验证码有效性
     */
    private void verifyCode(String phone, String bizType, String code) {
        SmsCodeLog last = smsCodeLogMapper.selectOne(new LambdaQueryWrapper<SmsCodeLog>()
                .eq(SmsCodeLog::getPhone, phone)
                .eq(SmsCodeLog::getBizType, bizType)
                .eq(SmsCodeLog::getStatus, 0)
                .orderByDesc(SmsCodeLog::getId)
                .last("limit 1"));
        if (last == null) {
            throw new IllegalArgumentException("请先获取验证码");
        }
        if (last.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码已过期");
        }
        if (!last.getCodeHash().equals(md5(code))) {
            throw new IllegalArgumentException("验证码错误");
        }
        last.setStatus(1);
        smsCodeLogMapper.updateById(last);
    }

    /**
     * MD5 哈希
     */
    private String md5(String source) {
        return DigestUtils.md5DigestAsHex(source.getBytes(StandardCharsets.UTF_8));
    }
}
