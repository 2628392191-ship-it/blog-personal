package com.blogsystem.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.admin.service.AdminService;
import com.blogsystem.auth.entity.SysUser;
import com.blogsystem.common.ApiResponse;
import com.blogsystem.content.mapper.ArticleMapper;
import com.blogsystem.comment.mapper.CommentMapper;
import com.blogsystem.log.entity.OperationLog;
import com.blogsystem.log.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OperationLogMapper operationLogMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    @SaCheckPermission("admin:user:list")
    @GetMapping("/users")
    public ApiResponse<Page<SysUser>> users(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Long pageNum,
                                            @RequestParam(defaultValue = "10") Long pageSize) {
        return ApiResponse.ok(adminService.listUsers(status, pageNum, pageSize));
    }

    @SaCheckPermission("admin:user:update")
    @PutMapping("/users/{id}/status")
    public ApiResponse<Map<String, Object>> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.toggleUserStatus(id, status);
        return ApiResponse.ok(Map.of("id", id, "status", status));
    }

    @SaCheckPermission("admin:user:delete")
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.ok();
    }

    @SaCheckLogin
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        // 文章统计
        long totalArticles = articleMapper.selectCount(null);
        long publishedArticles = articleMapper.selectCount(
                new LambdaQueryWrapper<com.blogsystem.content.entity.Article>()
                        .eq(com.blogsystem.content.entity.Article::getStatus, 1)
                        .eq(com.blogsystem.content.entity.Article::getDeleted, 0));
        // 评论统计
        long totalComments = commentMapper.selectCount(null);
        long pendingComments = commentMapper.selectCount(
                new LambdaQueryWrapper<com.blogsystem.comment.entity.Comment>()
                        .eq(com.blogsystem.comment.entity.Comment::getStatus, 1));
        // 用户统计
        long totalUsers = adminService.countUsers();
        // 最近文章
        List<com.blogsystem.content.entity.Article> recentArticles = articleMapper.selectList(
                new LambdaQueryWrapper<com.blogsystem.content.entity.Article>()
                        .eq(com.blogsystem.content.entity.Article::getDeleted, 0)
                        .orderByDesc(com.blogsystem.content.entity.Article::getId)
                        .last("limit 5"));
        data.put("totalArticles", totalArticles);
        data.put("publishedArticles", publishedArticles);
        data.put("totalComments", totalComments);
        data.put("pendingComments", pendingComments);
        data.put("totalUsers", totalUsers);
        data.put("recentArticles", recentArticles);
        return ApiResponse.ok(data);
    }

    @SaCheckPermission("admin:log:list")
    @GetMapping("/logs")
    public ApiResponse<Page<OperationLog>> logs(@RequestParam(required = false) String module,
                                                @RequestParam(defaultValue = "1") Long pageNum,
                                                @RequestParam(defaultValue = "10") Long pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getId);
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        return ApiResponse.ok(operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }
}
