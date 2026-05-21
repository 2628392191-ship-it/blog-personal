package com.blogsystem.content.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.common.ApiResponse;
import com.blogsystem.content.dto.ArticleSaveRequest;
import com.blogsystem.content.dto.CategorySaveRequest;
import com.blogsystem.content.dto.TagSaveRequest;
import com.blogsystem.content.entity.Article;
import com.blogsystem.content.entity.Category;
import com.blogsystem.content.entity.Tag;
import com.blogsystem.content.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容接口 —— 文章 / 分类 / 标签 的 CRUD
 */
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    /**
     * 新增 / 更新文章（需登录）
     */
    @SaCheckLogin
    @PostMapping("/article")
    public ApiResponse<Map<String, Long>> saveArticle(@RequestBody @Valid ArticleSaveRequest request) {
        return ApiResponse.ok(Map.of("id", contentService.saveArticle(request)));
    }

    /**
     * 文章分页查询，支持按状态 / 分类 / 标签筛选
     */
    @GetMapping("/article/list")
    public ApiResponse<Page<Article>> listArticle(@RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) Long tagId,
                                                  @RequestParam(defaultValue = "1") Long pageNum,
                                                  @RequestParam(defaultValue = "10") Long pageSize) {
        return ApiResponse.ok(contentService.listArticles(status, categoryId, tagId, pageNum, pageSize));
    }

    /**
     * 热门文章（按阅读量降序）
     */
    @GetMapping("/article/hot")
    public ApiResponse<List<Article>> listHotArticle(@RequestParam(defaultValue = "6") Long limit) {
        return ApiResponse.ok(contentService.listHotArticles(limit));
    }

    /**
     * 文章详情，阅读量 +1
     */
    @GetMapping("/article/{id}")
    public ApiResponse<Article> getArticle(@PathVariable Long id) {
        return ApiResponse.ok(contentService.getArticle(id));
    }

    /**
     * 删除文章（需登录，软删除）
     */
    @SaCheckLogin
    @DeleteMapping("/article/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        contentService.deleteArticle(id);
        return ApiResponse.ok();
    }

    /**
     * 新增 / 更新分类（需登录）
     */
    @SaCheckLogin
    @PostMapping("/category")
    public ApiResponse<Map<String, Long>> saveCategory(@RequestBody @Valid CategorySaveRequest request) {
        return ApiResponse.ok(Map.of("id", contentService.saveCategory(request)));
    }

    /**
     * 分类列表
     */
    @GetMapping("/category/list")
    public ApiResponse<List<Category>> listCategory() {
        return ApiResponse.ok(contentService.listCategory());
    }

    /**
     * 删除分类（需登录，软删除）
     */
    @SaCheckLogin
    @DeleteMapping("/category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        contentService.deleteCategory(id);
        return ApiResponse.ok();
    }

    /**
     * 新增 / 更新标签（需登录）
     */
    @SaCheckLogin
    @PostMapping("/tag")
    public ApiResponse<Map<String, Long>> saveTag(@RequestBody @Valid TagSaveRequest request) {
        return ApiResponse.ok(Map.of("id", contentService.saveTag(request)));
    }

    /**
     * 标签列表
     */
    @GetMapping("/tag/list")
    public ApiResponse<List<Tag>> listTag() {
        return ApiResponse.ok(contentService.listTag());
    }

    /**
     * 删除标签（需登录，软删除）
     */
    @SaCheckLogin
    @DeleteMapping("/tag/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        contentService.deleteTag(id);
        return ApiResponse.ok();
    }

    /**
     * 切换文章点赞（需登录）
     */
    @SaCheckLogin
    @PostMapping("/article/{id}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable Long id) {
        boolean liked = contentService.toggleLike(id);
        return ApiResponse.ok(Map.of("liked", liked));
    }
}
