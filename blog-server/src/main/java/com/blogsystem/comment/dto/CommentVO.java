package com.blogsystem.comment.dto;

import java.time.LocalDateTime;

public record CommentVO(Long id, Long articleId, Long userId, String nickname, String avatar,
                        Long parentId, Long replyToUserId, String replyToNickname,
                        String content, LocalDateTime createdAt) {}
