package com.logistics.platform.app.audit.controller;

import com.logistics.platform.app.audit.entity.AuditLog;
import com.logistics.platform.app.audit.repository.AuditLogRepository;
import com.logistics.platform.app.common.ApiResponse;
import com.logistics.platform.app.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ApiResponse<PageResponse<AuditLog>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        var result = repository.findAll(PageRequest.of(safePage, safeSize, Sort.by("timestamp").descending()));
        return ApiResponse.ok("Audit logs fetched", PageResponse.from(result));
    }
}
