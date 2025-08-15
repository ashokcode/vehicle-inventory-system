package com.dealerhub.inventory.service;

import com.dealerhub.inventory.domain.Vehicle;
import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehiclePhoto;
import com.dealerhub.inventory.domain.VehicleStatus;
import com.dealerhub.inventory.dto.VehicleRequest;
import com.dealerhub.inventory.dto.VehicleResponse;
import com.dealerhub.inventory.dto.VehicleSummaryResponse;
import com.dealerhub.inventory.exception.DuplicateVinException;
import com.dealerhub.inventory.exception.ResourceNotFoundException;
import com.dealerhub.inventory.mapper.VehicleMapper;
import com.dealerhub.inventory.repository.VehicleRepository;
import com.dealerhub.inventory.repository.VehicleSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final PhotoStorageService photoStorageService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper,
                               PhotoStorageService photoStorageService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
        this.photoStorageService = photoStorageService;
    }

    @Override
    public VehicleResponse create(VehicleRequest request) {
        String vin = request.getVin().toUpperCase();
        if (vehicleRepository.existsByVinIgnoreCase(vin)) {
            throw new DuplicateVinException(vin);
        }
        Vehicle vehicle = Vehicle.builder().status(VehicleStatus.AVAILABLE).build();
        vehicleMapper.applyRequest(vehicle, request);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse update(UUID id, VehicleRequest request) {
        Vehicle vehicle = getOrThrow(id);

        String vin = request.getVin().toUpperCase();
        vehicleRepository.findByVinIgnoreCase(vin)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateVinException(vin);
                });

        vehicleMapper.applyRequest(vehicle, request);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(UUID id) {
        return vehicleMapper.toResponse(getOrThrow(id));
    }

    @Override
    public void delete(UUID id) {
        Vehicle vehicle = getOrThrow(id);
        vehicle.getPhotos().forEach(photo -> photoStorageService.delete(photo.getStoragePath()));
        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleSummaryResponse> search(String brand, String model, Integer year, VehicleStatus status,
                                                VehicleCondition condition, BigDecimal minPrice, BigDecimal maxPrice,
                                                String query, Pageable pageable) {
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateAdded"));

        return vehicleRepository
                .findAll(VehicleSpecifications.withFilters(brand, model, year, status, condition, minPrice, maxPrice, query), sorted)
                .map(vehicleMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleSummaryResponse> recentlyAdded(int limit) {
        return vehicleRepository
                .findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "dateAdded")))
                .map(vehicleMapper::toSummary)
                .getContent();
    }

    @Override
    public VehicleResponse addPhotos(UUID vehicleId, List<MultipartFile> files) {
        Vehicle vehicle = getOrThrow(vehicleId);
        boolean needsPrimary = vehicle.getPhotos().stream().noneMatch(VehiclePhoto::isPrimary);
        int nextOrder = vehicle.getPhotos().size();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            PhotoStorageService.UploadResult uploaded = photoStorageService.upload(vehicleId, file);
            VehiclePhoto photo = VehiclePhoto.builder()
                    .url(uploaded.publicUrl())
                    .storagePath(uploaded.storagePath())
                    .primary(needsPrimary)
                    .displayOrder(nextOrder++)
                    .build();
            vehicle.addPhoto(photo);
            needsPrimary = false;
        }

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void deletePhoto(UUID vehicleId, UUID photoId) {
        Vehicle vehicle = getOrThrow(vehicleId);
        VehiclePhoto photo = vehicle.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Photo %s not found on vehicle %s".formatted(photoId, vehicleId)));

        boolean wasPrimary = photo.isPrimary();
        vehicle.removePhoto(photo);
        photoStorageService.delete(photo.getStoragePath());

        if (wasPrimary) {
            vehicle.getPhotos().stream()
                    .min((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                    .ifPresent(next -> next.setPrimary(true));
        }

        vehicleRepository.save(vehicle);
    }

    @Override
    public VehicleResponse setPrimaryPhoto(UUID vehicleId, UUID photoId) {
        Vehicle vehicle = getOrThrow(vehicleId);
        boolean found = false;
        for (VehiclePhoto photo : vehicle.getPhotos()) {
            boolean isTarget = photo.getId().equals(photoId);
            photo.setPrimary(isTarget);
            found = found || isTarget;
        }
        if (!found) {
            throw new ResourceNotFoundException("Photo %s not found on vehicle %s".formatted(photoId, vehicleId));
        }
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    private Vehicle getOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle %s not found".formatted(id)));
    }
}
