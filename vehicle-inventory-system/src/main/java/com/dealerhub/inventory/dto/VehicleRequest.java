package com.dealerhub.inventory.dto;

import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Year;

@Getter
@Setter
public class VehicleRequest {

    @NotBlank
    @Size(max = 60)
    private String brand;

    @NotBlank
    @Size(max = 60)
    private String model;

    @NotNull
    @Min(1900)
    private Integer year;

    @Size(max = 120)
    private String engine;

    @NotBlank
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must be 17 characters (letters/digits, excluding I, O, Q)")
    private String vin;

    @NotNull
    @Min(0)
    private Integer mileage;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal sellingPrice;

    private VehicleStatus status;

    @NotNull
    private VehicleCondition condition;

    @Size(max = 4000)
    private String notes;

    @AssertTrue(message = "year cannot be more than one model-year ahead of today")
    public boolean isYearNotTooFarInFuture() {
        return year == null || year <= Year.now().getValue() + 1;
    }
}
