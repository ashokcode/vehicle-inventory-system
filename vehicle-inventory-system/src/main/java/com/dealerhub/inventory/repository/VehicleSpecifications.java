package com.dealerhub.inventory.repository;

import com.dealerhub.inventory.domain.Vehicle;
import com.dealerhub.inventory.domain.VehicleCondition;
import com.dealerhub.inventory.domain.VehicleStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Builds the WHERE clause for the vehicle list/search endpoint from whatever
 * optional filters the caller supplied — every method here is null-safe and
 * simply contributes no predicate when its argument is absent.
 */
public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> withFilters(
            String brand,
            String model,
            Integer year,
            VehicleStatus status,
            VehicleCondition condition,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String query
    ) {
        return (root, cq, cb) -> {
            Predicate predicate = cb.conjunction();

            if (StringUtils.hasText(brand)) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("brand")), brand.toLowerCase()));
            }
            if (StringUtils.hasText(model)) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("model")), model.toLowerCase()));
            }
            if (year != null) {
                predicate = cb.and(predicate, cb.equal(root.get("year"), year));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (condition != null) {
                predicate = cb.and(predicate, cb.equal(root.get("condition"), condition));
            }
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }
            if (StringUtils.hasText(query)) {
                String like = "%" + query.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("brand")), like),
                        cb.like(cb.lower(root.get("model")), like),
                        cb.like(cb.lower(root.get("vin")), like),
                        cb.like(cb.lower(root.get("engine")), like)
                ));
            }
            return predicate;
        };
    }
}
