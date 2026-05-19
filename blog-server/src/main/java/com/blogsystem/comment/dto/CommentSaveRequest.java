package com.blogsystem.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentSaveRequest(
        @NotNull(message = "文章ID不能为空")
        Long articleId,
        Long parentId,
        Long replyToUserId,
        @NotBlank(message = "评论内容不能为空")
        String content
) {}
