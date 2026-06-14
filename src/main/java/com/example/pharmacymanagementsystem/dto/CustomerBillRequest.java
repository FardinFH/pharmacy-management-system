package com.example.pharmacymanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBillRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 120, message = "Customer name must be within 120 characters")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(max = 30, message = "Phone number must be within 30 characters")
    private String customerPhone;
}