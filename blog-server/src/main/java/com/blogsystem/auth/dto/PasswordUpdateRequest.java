package com.blogsystem.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {}
