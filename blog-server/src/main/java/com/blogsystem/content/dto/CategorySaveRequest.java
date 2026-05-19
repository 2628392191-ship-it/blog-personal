package com.blogsystem.content.dto;

import jakarta.validation.constraints.NotBlank;

public record CategorySaveRequest(
        Long id,
        @NotBlank(message = "分类名不能为空")
        String name,
        @NotBlank(message = "slug不能为空")
        String slug,
        String description,
        Integer sort,
        Integer status
) {}
