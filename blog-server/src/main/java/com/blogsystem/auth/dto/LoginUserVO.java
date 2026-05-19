package com.blogsystem.auth.dto;

public record LoginUserVO(Long userId, String phone, String username, String nickname, String email, String avatar, String token) {}
