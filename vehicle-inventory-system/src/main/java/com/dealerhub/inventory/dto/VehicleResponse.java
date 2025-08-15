package com.dealerhub.inventory.dto;

import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class VehicleResponse {
    private UUID id;
    private String brand;
    private String model;
    private Integer year;
    private String engine;
    private String vin;
    private Integer mileage;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private VehicleStatus status;
    private VehicleCondition condition;
    private String notes;
    private List<PhotoResponse> photos;
    private Instant dateAdded;
    private Instant lastUpdated;
}
