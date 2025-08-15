package com.dealerhub.inventory.web;

import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Enum values for the admin UI's dropdowns, so the frontend never hard-codes them. */
@RestController
@RequestMapping("/api/lookups")
@Tag(name = "Lookups", description = "Enum values for admin UI dropdowns")
public class LookupController {

    @GetMapping("/statuses")
    public VehicleStatus[] statuses() {
        return VehicleStatus.values();
    }

    @GetMapping("/conditions")
    public VehicleCondition[] conditions() {
        return VehicleCondition.values();
    }
}
