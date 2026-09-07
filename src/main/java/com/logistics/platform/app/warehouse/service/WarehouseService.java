package com.logistics.platform.app.warehouse.service;

import com.logistics.platform.app.common.PageResponse;
import com.logistics.platform.app.exception.ResourceNotFoundException;
import com.logistics.platform.app.warehouse.dto.WarehouseCreateRequest;
import com.logistics.platform.app.warehouse.dto.WarehouseResponse;
import com.logistics.platform.app.warehouse.entity.Warehouse;
import com.logistics.platform.app.warehouse.repository.WarehouseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @CacheEvict(value = "warehouses", key = "'all'")
    @Transactional
    public WarehouseResponse create(@Valid WarehouseCreateRequest request) {
        Warehouse warehouse = Warehouse.builder()
                .name(request.name().trim())
                .location(request.location().trim())
                .capacity(request.capacity())
                .currentLoad(BigDecimal.ZERO)
                .build();

        warehouseRepository.save(warehouse);
        return toResponse(warehouse);
    }

    @CachePut(value = "warehouses", key = "#id")
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseCreateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        warehouse.setName(request.name().trim());
        warehouse.setLocation(request.location().trim());
        warehouse.setCapacity(request.capacity());

        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return toResponse(updatedWarehouse);
    }

    @CacheEvict(value = "warehouses", key = "#id")
    @Transactional
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warehouse not found");
        }
        warehouseRepository.deleteById(id);
    }

    @Cacheable(value = "warehouses", key = "#id")
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        return toResponse(warehouse);
    }


    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> list(int page, int size) {
        Page<WarehouseResponse> p = warehouseRepository
                .findAll(PageRequest.of(page, size))
                .map(this::toResponse);
        return PageResponse.from(p);
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCapacity(),
                warehouse.getCurrentLoad()
        );
    }
}
