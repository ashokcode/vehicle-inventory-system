package com.dealerhub.inventory.repository;

import com.dealerhub.inventory.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    boolean existsByVinIgnoreCase(String vin);

    Optional<Vehicle> findByVinIgnoreCase(String vin);
}
