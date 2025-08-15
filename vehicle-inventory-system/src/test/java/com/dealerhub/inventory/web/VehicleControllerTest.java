package com.dealerhub.inventory.web;

import com.dealerhub.inventory.repository.VehicleRepository;
import com.dealerhub.inventory.service.PhotoStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @MockBean
    private PhotoStorageService photoStorageService;

    @BeforeEach
    void cleanDatabase() {
        vehicleRepository.deleteAll();
    }

    private Map<String, Object> sampleVehiclePayload(String vin) {
        return Map.of(
                "brand", "Honda",
                "model", "Civic",
                "year", 2023,
                "engine", "1.5L Turbo",
                "vin", vin,
                "mileage", 5000,
                "purchasePrice", 19500.00,
                "sellingPrice", 22900.00,
                "condition", "USED"
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void unauthenticatedRequestsAreRejected_thenAuthenticatedCrudWorks() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVehiclePayload("1HGCM82633A123456"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Honda"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void anonymousRequestIsRejectedWithUnauthorized() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void creatingTheSameVinTwiceReturnsConflict() throws Exception {
        String payload = objectMapper.writeValueAsString(sampleVehiclePayload("2HGFC2F59NH123456"));

        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void invalidPayloadReturnsFieldValidationErrors() throws Exception {
        Map<String, Object> invalid = Map.of(
                "brand", "",
                "model", "Civic",
                "year", 1800,
                "vin", "TOO-SHORT",
                "mileage", -5,
                "purchasePrice", -1,
                "condition", "USED"
        );

        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void searchFiltersByStatusAndBrand() throws Exception {
        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVehiclePayload("3HGFC2F59NH123456"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/vehicles").param("brand", "Honda").param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].brand").value("Honda"));

        mockMvc.perform(get("/api/vehicles").param("brand", "Toyota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deletingAVehicleCleansUpItsPhotos() throws Exception {
        String created = mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVehiclePayload("4HGFC2F59NH123456"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(delete("/api/vehicles/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/vehicles/{id}", id)).andExpect(status().isNotFound());
    }
}
