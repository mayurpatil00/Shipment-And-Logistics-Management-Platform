package com.logistics.platform.app.auth.dto;

import jakarta.validation.constraints.NotBlank;


public record RefreshTokenRequest(@NotBlank String refreshToken) {}
