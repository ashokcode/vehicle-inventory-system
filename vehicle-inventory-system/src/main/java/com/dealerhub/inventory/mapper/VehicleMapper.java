package com.dealerhub.inventory.mapper;

import com.dealerhub.inventory.domain.Vehicle;
import com.dealerhub.inventory.domain.VehiclePhoto;
import com.dealerhub.inventory.dto.PhotoResponse;
import com.dealerhub.inventory.dto.VehicleResponse;
import com.dealerhub.inventory.dto.VehicleSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        List<PhotoResponse> photos = vehicle.getPhotos().stream()
                .sorted(Comparator.comparingInt(VehiclePhoto::getDisplayOrder))
                .map(this::toPhotoResponse)
                .toList();

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .engine(vehicle.getEngine())
                .vin(vehicle.getVin())
                .mileage(vehicle.getMileage())
                .purchasePrice(vehicle.getPurchasePrice())
                .sellingPrice(vehicle.getSellingPrice())
                .status(vehicle.getStatus())
                .condition(vehicle.getCondition())
                .notes(vehicle.getNotes())
                .photos(photos)
                .dateAdded(vehicle.getDateAdded())
                .lastUpdated(vehicle.getLastUpdated())
                .build();
    }

    public VehicleSummaryResponse toSummary(Vehicle vehicle) {
        String primaryUrl = vehicle.getPhotos().stream()
                .filter(VehiclePhoto::isPrimary)
                .findFirst()
                .or(() -> vehicle.getPhotos().stream()
                        .min(Comparator.comparingInt(VehiclePhoto::getDisplayOrder)))
                .map(VehiclePhoto::getUrl)
                .orElse(null);

        return VehicleSummaryResponse.builder()
                .id(vehicle.getId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .vin(vehicle.getVin())
                .mileage(vehicle.getMileage())
                .sellingPrice(vehicle.getSellingPrice())
                .status(vehicle.getStatus())
                .condition(vehicle.getCondition())
                .primaryPhotoUrl(primaryUrl)
                .dateAdded(vehicle.getDateAdded())
                .lastUpdated(vehicle.getLastUpdated())
                .build();
    }

    public PhotoResponse toPhotoResponse(VehiclePhoto photo) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .url(photo.getUrl())
                .primary(photo.isPrimary())
                .displayOrder(photo.getDisplayOrder())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }

    public void applyRequest(Vehicle vehicle, com.dealerhub.inventory.dto.VehicleRequest request) {
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setEngine(request.getEngine());
        vehicle.setVin(request.getVin().toUpperCase());
        vehicle.setMileage(request.getMileage());
        vehicle.setPurchasePrice(request.getPurchasePrice());
        vehicle.setSellingPrice(request.getSellingPrice());
        vehicle.setCondition(request.getCondition());
        vehicle.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }
    }
}
