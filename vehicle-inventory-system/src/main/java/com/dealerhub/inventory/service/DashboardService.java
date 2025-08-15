package com.dealerhub.inventory.service;

import com.dealerhub.inventory.domain.Vehicle;
import com.dealerhub.inventory.domain.VehicleStatus;
import com.dealerhub.inventory.dto.DashboardSummaryResponse;
import com.dealerhub.inventory.mapper.VehicleMapper;
import com.dealerhub.inventory.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public DashboardService(VehicleRepository vehicleRepository, VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    public DashboardSummaryResponse summarize() {
        List<Vehicle> all = vehicleRepository.findAll();

        Map<VehicleStatus, Long> countByStatus = new EnumMap<>(VehicleStatus.class);
        Arrays.stream(VehicleStatus.values()).forEach(s -> countByStatus.put(s, 0L));
        all.forEach(v -> countByStatus.merge(v.getStatus(), 1L, Long::sum));

        Map<String, Long> countByStatusName = new LinkedHashMap<>();
        countByStatus.forEach((status, count) -> countByStatusName.put(status.name(), count));

        BigDecimal totalInventoryValue = all.stream()
                .filter(v -> v.getStatus() != VehicleStatus.SOLD)
                .map(Vehicle::getPurchasePrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPotentialRevenue = all.stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE || v.getStatus() == VehicleStatus.RESERVED)
                .map(Vehicle::getSellingPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Vehicle> sold = all.stream().filter(v -> v.getStatus() == VehicleStatus.SOLD).toList();
        BigDecimal soldRevenue = sold.stream()
                .map(Vehicle::getSellingPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal soldProfit = sold.stream()
                .filter(v -> v.getSellingPrice() != null && v.getPurchasePrice() != null)
                .map(v -> v.getSellingPrice().subtract(v.getPurchasePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryResponse.builder()
                .totalVehicles(all.size())
                .countByStatus(countByStatusName)
                .totalInventoryValue(totalInventoryValue)
                .totalPotentialRevenue(totalPotentialRevenue)
                .soldRevenueToDate(soldRevenue)
                .soldGrossProfitToDate(soldProfit)
                .recentlyAdded(vehicleService.recentlyAdded(5))
                .build();
    }
}
