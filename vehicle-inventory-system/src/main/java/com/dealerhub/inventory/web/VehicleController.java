package com.dealerhub.inventory.web;

import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import com.dealerhub.inventory.dto.PageResponse;
import com.dealerhub.inventory.dto.VehicleRequest;
import com.dealerhub.inventory.dto.VehicleResponse;
import com.dealerhub.inventory.dto.VehicleSummaryResponse;
import com.dealerhub.inventory.service.VehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Inventory CRUD, search, and photo management")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public PageResponse<VehicleSummaryResponse> search(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) VehicleCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return PageResponse.of(vehicleService.search(brand, model, year, status, condition, minPrice, maxPrice, q, pageable));
    }

    @GetMapping("/{id}")
    public VehicleResponse getById(@PathVariable UUID id) {
        return vehicleService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@Valid @RequestBody VehicleRequest request) {
        return vehicleService.create(request);
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@PathVariable UUID id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        vehicleService.delete(id);
    }

    @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
    public VehicleResponse uploadPhotos(@PathVariable UUID id, @RequestParam("files") List<MultipartFile> files) {
        return vehicleService.addPhotos(id, files);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable UUID id, @PathVariable UUID photoId) {
        vehicleService.deletePhoto(id, photoId);
    }

    @PutMapping("/{id}/photos/{photoId}/primary")
    public VehicleResponse setPrimaryPhoto(@PathVariable UUID id, @PathVariable UUID photoId) {
        return vehicleService.setPrimaryPhoto(id, photoId);
    }
}
