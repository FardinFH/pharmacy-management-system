package com.example.pharmacymanagementsystem.dto;

import com.example.pharmacymanagementsystem.model.MedicineStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequest {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 120, message = "Medicine name must be within 120 characters")
    private String medicineName;

    @NotBlank(message = "Generic name is required")
    @Size(max = 120, message = "Generic name must be within 120 characters")
    private String genericName;

    @NotBlank(message = "Category is required")
    @Size(max = 60, message = "Category must be within 60 characters")
    private String category;

    @NotBlank(message = "Manufacturer is required")
    @Size(max = 120, message = "Manufacturer must be within 120 characters")
    private String manufacturer;

    @NotBlank(message = "Batch number is required")
    @Size(max = 80, message = "Batch number must be within 80 characters")
    private String batchNo;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Low stock alert quantity is required")
    @Min(value = 1, message = "Low stock alert quantity must be at least 1")
    private Integer reorderLevel;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid selling price")
    private BigDecimal sellingPrice;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be a future date")
    private LocalDate expiryDate;

    @Size(max = 120, message = "Supplier name must be within 120 characters")
    private String supplier;

    @Size(max = 80, message = "Rack number must be within 80 characters")
    private String rackNo;

    @NotNull(message = "Medicine status is required")
    private MedicineStatus medicineStatus;

    @Size(max = 2000, message = "Description must be within 2000 characters")
    private String description;
}