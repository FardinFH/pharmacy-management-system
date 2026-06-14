package com.example.pharmacymanagementsystem.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingCart implements Serializable {

    @Builder.Default
    private List<com.example.pharmacymanagementsystem.dto.billing.BillingCartItem> items = new ArrayList<>();

    private PendingBill pendingBill;

    @Builder.Default
    private boolean customerBoxOpen = false;

    public BigDecimal getTotal() {
        return items.stream()
                .map(com.example.pharmacymanagementsystem.dto.billing.BillingCartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public void clearAll() {
        items.clear();
        pendingBill = null;
        customerBoxOpen = false;
    }
}