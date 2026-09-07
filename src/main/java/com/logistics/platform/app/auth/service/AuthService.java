package com.logistics.platform.app.auth.service;

import com.logistics.platform.app.auth.dto.*;
import com.logistics.platform.app.config.RedisService;
import com.logistics.platform.app.exception.BusinessException;
import com.logistics.platform.app.exception.UnauthorizedException;
import com.logistics.platform.app.security.JwtService;
import com.logistics.platform.app.security.TokenBlacklistService;
import com.logistics.platform.app.user.entity.*;
import com.logistics.platform.app.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String REFRESH_KEY_PREFIX = "refresh:user:";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RedisService redisService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("Email already registered");
        }

        Role customer = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not initialized"));

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .roles(new java.util.HashSet<>(java.util.Set.of(customer)))
                .build();

        userRepository.save(user);

        return issueTokens(user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {

        String normalizedEmail = request.email().toLowerCase().trim();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return issueTokens(normalizedEmail);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String jti = jwtService.extractJti(refreshToken);
        if (tokenBlacklistService.isBlacklisted(jti)) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        String email = jwtService.extractUsername(refreshToken);
        Object stored = redisService.get(REFRESH_KEY_PREFIX + email);
        if (stored == null || !jti.equals(stored.toString())) {
            throw new UnauthorizedException("Refresh token is no longer active");
        }

        long ttlSeconds = Math.max(0, (jwtService.extractExpiration(refreshToken).getTime() - System.currentTimeMillis()) / 1000);
        tokenBlacklistService.blacklist(jti, ttlSeconds);

        return issueTokens(email);
    }


    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing bearer token");
        }
        String token = authorizationHeader.substring(7);
        if (!jwtService.isValid(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        String jti = jwtService.extractJti(token);
        long ttlSeconds = Math.max(0, (jwtService.extractExpiration(token).getTime() - System.currentTimeMillis()) / 1000);
        tokenBlacklistService.blacklist(jti, ttlSeconds);

        String email = jwtService.extractUsername(token);
        redisService.delete(REFRESH_KEY_PREFIX + email);
    }

    private AuthResponse issueTokens(String email) {
        String accessToken = jwtService.generateToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        redisService.save(REFRESH_KEY_PREFIX + email, jwtService.extractJti(refreshToken),
                jwtService.getRefreshExpirationMs() / 1000);

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.getExpirationMs() / 1000);
    }
}
