package com.dealerhub.inventory.dto;

import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Lightweight projection for the list/search view — one photo, not the full gallery. */
@Getter
@Builder
@AllArgsConstructor
public class VehicleSummaryResponse {
    private UUID id;
    private String brand;
    private String model;
    private Integer year;
    private String vin;
    private Integer mileage;
    private BigDecimal sellingPrice;
    private VehicleStatus status;
    private VehicleCondition condition;
    private String primaryPhotoUrl;
    private Instant dateAdded;
    private Instant lastUpdated;
}
