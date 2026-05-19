package com.blogsystem.content.dto;

import jakarta.validation.constraints.NotBlank;

public record TagSaveRequest(
        Long id,
        @NotBlank(message = "标签名不能为空")
        String name,
        @NotBlank(message = "slug不能为空")
        String slug,
        String color,
        Integer sort,
        Integer status
) {}
