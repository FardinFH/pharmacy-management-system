package com.example.pharmacymanagementsystem.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingBill implements Serializable {

    private String invoiceNo;
    private String customerName;
    private String customerPhone;
    private LocalDateTime dateTime;
    private List<com.example.pharmacymanagementsystem.dto.billing.BillingCartItem> items;
    private BigDecimal total;
}