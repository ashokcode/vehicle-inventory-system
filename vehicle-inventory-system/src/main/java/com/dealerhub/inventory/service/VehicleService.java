package com.dealerhub.inventory.service;

import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import com.dealerhub.inventory.dto.VehicleRequest;
import com.dealerhub.inventory.dto.VehicleResponse;
import com.dealerhub.inventory.dto.VehicleSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface VehicleService {

    VehicleResponse create(VehicleRequest request);

    VehicleResponse update(UUID id, VehicleRequest request);

    VehicleResponse getById(UUID id);

    void delete(UUID id);

    Page<VehicleSummaryResponse> search(
            String brand, String model, Integer year, VehicleStatus status, VehicleCondition condition,
            BigDecimal minPrice, BigDecimal maxPrice, String query, Pageable pageable);

    List<VehicleSummaryResponse> recentlyAdded(int limit);

    VehicleResponse addPhotos(UUID vehicleId, List<MultipartFile> files);

    void deletePhoto(UUID vehicleId, UUID photoId);

    VehicleResponse setPrimaryPhoto(UUID vehicleId, UUID photoId);
}
