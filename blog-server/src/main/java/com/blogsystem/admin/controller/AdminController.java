package com.blogsystem.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.admin.service.AdminService;
import com.blogsystem.auth.entity.SysUser;
import com.blogsystem.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端接口 —— 用户管理
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminController {

    private final AdminService adminService;

    /**
     * 分页查询用户列表，支持按状态筛选
     */
    @GetMapping("/users")
    public ApiResponse<Page<SysUser>> users(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Long pageNum,
                                            @RequestParam(defaultValue = "10") Long pageSize) {
        return ApiResponse.ok(adminService.listUsers(status, pageNum, pageSize));
    }

    /**
     * 切换用户启用/禁用状态
     */
    @PutMapping("/users/{id}/status")
    public ApiResponse<Map<String, Object>> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.toggleUserStatus(id, status);
        return ApiResponse.ok(Map.of("id", id, "status", status));
    }

    /**
     * 软删除用户
     */
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.ok();
    }
}
