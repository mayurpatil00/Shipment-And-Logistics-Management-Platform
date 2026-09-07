package com.logistics.platform.app.audit.service;

import com.logistics.platform.app.audit.entity.AuditLog;
import com.logistics.platform.app.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;

    public void record(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue) {
        try {
            repository.save(AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build());
        } catch (Exception ex) {
            log.error("Failed to write audit log for {} {} (entityId={})", action, entityType, entityId, ex);
        }
    }
}
