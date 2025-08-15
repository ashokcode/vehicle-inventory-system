package com.dealerhub.inventory.repository;

import com.dealerhub.inventory.domain.VehiclePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehiclePhotoRepository extends JpaRepository<VehiclePhoto, UUID> {
}
