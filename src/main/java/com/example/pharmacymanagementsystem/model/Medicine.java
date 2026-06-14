package com.example.pharmacymanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "medicines",
        indexes = {
                @Index(name = "idx_medicine_name", columnList = "medicine_name"),
                @Index(name = "idx_batch_no", columnList = "batch_no"),
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_expiry_date", columnList = "expiry_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medicine_batch_no", columnNames = "batch_no")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_name", nullable = false, length = 120)
    private String medicineName;

    @Column(name = "generic_name", nullable = false, length = 120)
    private String genericName;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 120)
    private String manufacturer;

    @Column(name = "batch_no", nullable = false, length = 80)
    private String batchNo;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(length = 120)
    private String supplier;

    @Column(name = "rack_no", length = 80)
    private String rackNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "medicine_status", nullable = false, length = 30)
    private MedicineStatus medicineStatus;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeInsert() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.unit == null || this.unit.isBlank()) {
            this.unit = "pcs";
        }

        if (this.medicineStatus == null) {
            this.medicineStatus = MedicineStatus.AVAILABLE;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}