package com.logistics.platform.app.tracking.controller;

import com.logistics.platform.app.common.ApiResponse;
import com.logistics.platform.app.security.UserPrincipal;
import com.logistics.platform.app.tracking.dto.*;
import com.logistics.platform.app.tracking.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {
    private final TrackingService service;

    private boolean isStaff(Authentication authentication) {
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if (a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OPERATOR")) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAuthority('TRACKING_READ')")
    public ApiResponse<List<TrackingEventResponse>> history(@PathVariable String trackingNumber, Authentication a) {
        UserPrincipal principal = (UserPrincipal) a.getPrincipal();
        return ApiResponse.ok("Tracking history fetched", service.history(trackingNumber, principal.getId(), isStaff(a)));
    }

    @PostMapping("/{trackingNumber}/events")
    @PreAuthorize("hasAuthority('TRACKING_WRITE')")
    public ApiResponse<TrackingEventResponse> add(@PathVariable String trackingNumber, @Valid @RequestBody TrackingEventRequest r) {
        return ApiResponse.ok("Tracking event added", service.add(trackingNumber, r));
    }
}
