package com.example.pharmacymanagementsystem.dto.billing;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingCartItem implements Serializable {

    private Long medicineId;
    private String medicineName;
    private String genericName;
    private String batchNo;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal total;
}