package com.blogsystem.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.auth.entity.SysUser;
import com.blogsystem.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理端业务 —— 用户管理
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final SysUserMapper sysUserMapper;

    /**
     * 分页查询用户列表，支持按状态筛选
     */
    public Page<SysUser> listUsers(Integer status, long pageNum, long pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getId);
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        return sysUserMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 切换用户启用/禁用状态
     */
    public void toggleUserStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    public long countUsers() {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0));
    }

    /**
     * 软删除用户
     */
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        sysUserMapper.deleteById(id);
    }
}
