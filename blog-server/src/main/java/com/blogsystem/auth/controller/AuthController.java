package com.blogsystem.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.blogsystem.auth.dto.LoginUserVO;
import com.blogsystem.auth.dto.PasswordLoginRequest;
import com.blogsystem.auth.dto.PhoneCodeAuthRequest;
import com.blogsystem.auth.dto.PasswordUpdateRequest;
import com.blogsystem.auth.dto.ProfileUpdateRequest;
import com.blogsystem.auth.dto.ResetPasswordRequest;
import com.blogsystem.auth.dto.SendSmsCodeRequest;
import com.blogsystem.auth.service.AuthService;
import com.blogsystem.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口 —— 验证码 / 注册 / 登录 / 个人信息 / 登出
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送手机短信验证码（开发环境返回 mock 码）
     */
    @PostMapping("/sms-code")
    public ApiResponse<Map<String, String>> sendSmsCode(@RequestBody @Valid SendSmsCodeRequest request,
                                                         HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(authService.sendSmsCode(request, httpServletRequest.getRemoteAddr()));
    }

    /**
     * 手机验证码注册
     */
    @PostMapping("/register")
    public ApiResponse<LoginUserVO> register(@RequestBody @Valid PhoneCodeAuthRequest request) {
        return ApiResponse.ok(authService.registerByPhoneCode(request));
    }

    /**
     * 手机验证码登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@RequestBody @Valid PhoneCodeAuthRequest request) {
        return ApiResponse.ok(authService.loginByPhoneCode(request));
    }

    /**
     * 获取当前登录用户信息
     */
    @SaCheckLogin
    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me() {
        return ApiResponse.ok(authService.currentUser());
    }

    /**
     * 更新个人信息（昵称 / 邮箱 / 头像）
     */
    @SaCheckLogin
    @PutMapping("/profile")
    public ApiResponse<LoginUserVO> updateProfile(@RequestBody @Valid ProfileUpdateRequest request) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        return ApiResponse.ok(authService.updateProfile(userId, request.nickname(), request.email(), request.avatar()));
    }

    /**
     * 修改密码
     */
    @SaCheckLogin
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody @Valid PasswordUpdateRequest request) {
        authService.changePassword(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), request.oldPassword(), request.newPassword());
        return ApiResponse.ok();
    }

    /**
     * 密码登录
     */
    @PostMapping("/login/password")
    public ApiResponse<LoginUserVO> loginByPassword(@RequestBody @Valid PasswordLoginRequest request) {
        return ApiResponse.ok(authService.loginByPassword(request));
    }

    /**
     * 忘记密码 — 短信验证后重置密码
     */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok();
    }

    /**
     * 退出登录
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }
}
