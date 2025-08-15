package com.dealerhub.inventory.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String brand;

    @Column(nullable = false, length = 60)
    private String model;

    // Mapped to a non-default column name because YEAR is a reserved word in
    // several SQL dialects (H2 included) — "year" stays the Java/JSON field
    // name since that's what matters for the API and for callers of this class.
    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Column(length = 120)
    private String engine;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Column(nullable = false)
    private Integer mileage;

    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleCondition condition;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VehiclePhoto> photos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "date_added", nullable = false, updatable = false)
    private Instant dateAdded;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public void addPhoto(VehiclePhoto photo) {
        photos.add(photo);
        photo.setVehicle(this);
    }

    public void removePhoto(VehiclePhoto photo) {
        photos.remove(photo);
        photo.setVehicle(null);
    }
}
