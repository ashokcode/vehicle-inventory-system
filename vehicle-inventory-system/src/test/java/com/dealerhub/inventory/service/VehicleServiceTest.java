package com.dealerhub.inventory.service;

import com.dealerhub.inventory.domain.Vehicle;
import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import com.dealerhub.inventory.dto.VehicleRequest;
import com.dealerhub.inventory.dto.VehicleResponse;
import com.dealerhub.inventory.exception.DuplicateVinException;
import com.dealerhub.inventory.exception.ResourceNotFoundException;
import com.dealerhub.inventory.mapper.VehicleMapper;
import com.dealerhub.inventory.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PhotoStorageService photoStorageService;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, new VehicleMapper(), photoStorageService);
    }

    private VehicleRequest sampleRequest() {
        VehicleRequest request = new VehicleRequest();
        request.setBrand("Toyota");
        request.setModel("Camry");
        request.setYear(2022);
        request.setEngine("2.5L I4");
        request.setVin("4T1BF1FK5CU123456");
        request.setMileage(15000);
        request.setPurchasePrice(new BigDecimal("18000.00"));
        request.setSellingPrice(new BigDecimal("21500.00"));
        request.setCondition(VehicleCondition.USED);
        return request;
    }

    @Test
    void create_savesVehicleWithDefaultAvailableStatus() {
        when(vehicleRepository.existsByVinIgnoreCase("4T1BF1FK5CU123456")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse response = vehicleService.create(sampleRequest());

        assertThat(response.getBrand()).isEqualTo("Toyota");
        assertThat(response.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(response.getVin()).isEqualTo("4T1BF1FK5CU123456");
    }

    @Test
    void create_rejectsDuplicateVin() {
        when(vehicleRepository.existsByVinIgnoreCase("4T1BF1FK5CU123456")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(sampleRequest()))
                .isInstanceOf(DuplicateVinException.class);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void getById_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_rejectsVinAlreadyUsedByAnotherVehicle() {
        UUID id = UUID.randomUUID();
        Vehicle existingUnderUpdate = Vehicle.builder().id(id).vin("OLDVIN000000000AA").build();
        Vehicle otherVehicleWithSameVin = Vehicle.builder().id(UUID.randomUUID()).vin("4T1BF1FK5CU123456").build();

        when(vehicleRepository.findById(id)).thenReturn(Optional.of(existingUnderUpdate));
        when(vehicleRepository.findByVinIgnoreCase("4T1BF1FK5CU123456")).thenReturn(Optional.of(otherVehicleWithSameVin));

        assertThatThrownBy(() -> vehicleService.update(id, sampleRequest()))
                .isInstanceOf(DuplicateVinException.class);
    }

    @Test
    void delete_removesStoredPhotosBeforeDeletingVehicle() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder().id(id).build();
        vehicle.addPhoto(com.dealerhub.inventory.domain.VehiclePhoto.builder()
                .storagePath("vehicles/x/photo.jpg")
                .url("https://example.com/photo.jpg")
                .build());

        when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));

        vehicleService.delete(id);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(photoStorageService).delete(pathCaptor.capture());
        assertThat(pathCaptor.getValue()).isEqualTo("vehicles/x/photo.jpg");
        verify(vehicleRepository).delete(vehicle);
    }
}
