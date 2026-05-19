package com.blogsystem.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ArticleSaveRequest(
        Long id,
        @NotBlank(message = "标题不能为空")
        String title,
        String summary,
        @NotBlank(message = "内容不能为空")
        String contentMd,
        String coverUrl,
        Long categoryId,
        @NotNull(message = "状态不能为空")
        Integer status,
        Integer isTop,
        Integer isCommentEnabled,
        List<Long> tagIds
) {}
