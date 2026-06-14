package com.example.pharmacymanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "sales",
        indexes = {
                @Index(name = "idx_invoice_no", columnList = "invoice_no"),
                @Index(name = "idx_sale_date_time", columnList = "sale_date_time"),
                @Index(name = "idx_customer_phone", columnList = "customer_phone")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invoice_no", columnNames = "invoice_no")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", nullable = false, length = 60)
    private String invoiceNo;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Column(name = "sale_date_time", nullable = false)
    private LocalDateTime saleDateTime;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    @PrePersist
    public void beforeInsert() {
        this.createdAt = LocalDateTime.now();

        if (this.saleDateTime == null) {
            this.saleDateTime = LocalDateTime.now();
        }
    }
}