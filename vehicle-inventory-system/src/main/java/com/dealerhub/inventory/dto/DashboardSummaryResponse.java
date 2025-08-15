package com.dealerhub.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalVehicles;
    private Map<String, Long> countByStatus;
    private BigDecimal totalInventoryValue;   // sum of purchasePrice for vehicles not yet SOLD
    private BigDecimal totalPotentialRevenue; // sum of sellingPrice for AVAILABLE + RESERVED
    private BigDecimal soldRevenueToDate;     // sum of sellingPrice for SOLD
    private BigDecimal soldGrossProfitToDate; // sum of (sellingPrice - purchasePrice) for SOLD
    private List<VehicleSummaryResponse> recentlyAdded;
}
