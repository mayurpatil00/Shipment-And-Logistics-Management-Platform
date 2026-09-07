package com.logistics.platform.app.warehouse.controller;

import com.logistics.platform.app.common.ApiResponse;
import com.logistics.platform.app.common.PageResponse;
import com.logistics.platform.app.warehouse.dto.*;
import com.logistics.platform.app.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<WarehouseResponse> create(@Valid @RequestBody WarehouseCreateRequest request) {
        return ApiResponse.ok("Warehouse created successfully", warehouseService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')")
    public ApiResponse<PageResponse<WarehouseResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ApiResponse.ok("Warehouses fetched", warehouseService.list(safePage, safeSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')")
    public ApiResponse<WarehouseResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok("Warehouse fetched successfully", warehouseService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<WarehouseResponse> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseCreateRequest request) {
        return ApiResponse.ok("Warehouse updated successfully", warehouseService.updateWarehouse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
