package com.logistics.platform.app.auth.controller;

import com.logistics.platform.app.auth.dto.*;
import com.logistics.platform.app.auth.service.AuthService;
import com.logistics.platform.app.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ApiResponse.ok("Registration successful", authService.register(r));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest r) {
        return ApiResponse.ok("Login successful", authService.login(r));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest r) {
        return ApiResponse.ok("Token refreshed", authService.refresh(r.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ApiResponse.ok("Logged out successfully", null);
    }
}
