package com.logistics.platform.app.delivery.controller;

import com.logistics.platform.app.common.ApiResponse;
import com.logistics.platform.app.delivery.dto.*;
import com.logistics.platform.app.delivery.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<DeliveryPartnerResponse> create(@Valid @RequestBody DeliveryPartnerCreateRequest request) {
        return ApiResponse.ok("Delivery partner created", service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PARTNER_READ')")
    public ApiResponse<List<DeliveryPartnerResponse>> list() {
        return ApiResponse.ok("Delivery partners fetched", service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER_READ')")
    public ApiResponse<DeliveryPartnerResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok("Delivery partner fetched", service.getById(id));
    }
}
