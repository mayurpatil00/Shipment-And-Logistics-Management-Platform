package com.logistics.platform.app.security;

import com.logistics.platform.app.config.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private static final String PREFIX = "blacklist:jti:";

    private final RedisService redisService;

    public void blacklist(String jti, long ttlSeconds) {
        if (ttlSeconds <= 0) return;
        redisService.save(PREFIX + jti, Boolean.TRUE, ttlSeconds);
    }

    public boolean isBlacklisted(String jti) {
        return redisService.exists(PREFIX + jti);
    }
}
