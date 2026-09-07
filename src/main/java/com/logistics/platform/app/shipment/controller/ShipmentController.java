package com.logistics.platform.app.shipment.controller;

import com.logistics.platform.app.common.ApiResponse;
import com.logistics.platform.app.common.PageResponse;
import com.logistics.platform.app.security.UserPrincipal;
import com.logistics.platform.app.shipment.dto.*;
import com.logistics.platform.app.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    private Long currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    private boolean isStaff(Authentication authentication) {
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if (a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OPERATOR")) {
                return true;
            }
        }
        return false;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','OPERATOR','ADMIN')")
    public ApiResponse<ShipmentResponse> create(@Valid @RequestBody ShipmentCreateRequest r, Authentication a) {
        return ApiResponse.ok("Shipment created", shipmentService.create(r, currentUserId(a)));
    }

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAuthority('SHIPMENT_READ')")
    public ApiResponse<ShipmentResponse> get(@PathVariable String trackingNumber, Authentication a) {
        return ApiResponse.ok("Shipment fetched",
                shipmentService.getByTracking(trackingNumber, currentUserId(a), isStaff(a)));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<PageResponse<ShipmentResponse>> mine(Authentication a,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok("Shipments fetched", shipmentService.getMine(currentUserId(a), safePage, safeSize));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','OPERATOR','ADMIN')")
    public ApiResponse<ShipmentResponse> cancel(@PathVariable Long id, Authentication a) {
        return ApiResponse.ok("Shipment cancelled", shipmentService.cancel(id, currentUserId(a), isStaff(a)));
    }
}
