package com.example.pharmacymanagementsystem.service;

import com.example.pharmacymanagementsystem.dto.AddToCartRequest;
import com.example.pharmacymanagementsystem.dto.BillingCart;
import com.example.pharmacymanagementsystem.dto.CustomerBillRequest;
import com.example.pharmacymanagementsystem.dto.PendingBill;
import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.model.MedicineStatus;
import com.example.pharmacymanagementsystem.model.Sale;
import com.example.pharmacymanagementsystem.model.SaleItem;
import com.example.pharmacymanagementsystem.repository.MedicineRepository;
import com.example.pharmacymanagementsystem.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<Medicine> getAvailableMedicines(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return medicineRepository.findByQuantityGreaterThanAndMedicineStatusOrderByMedicineNameAsc(
                    0,
                    MedicineStatus.AVAILABLE
            );
        }

        return medicineRepository.searchAvailableMedicines(keyword.trim());
    }

    @Transactional(readOnly = true)
    public void addToCart(BillingCart cart, AddToCartRequest request) {
        if (cart.getPendingBill() != null) {
            throw new IllegalArgumentException("Please confirm or cancel current bill first.");
        }

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found."));

        if (medicine.getMedicineStatus() != MedicineStatus.AVAILABLE || medicine.getQuantity() <= 0) {
            throw new IllegalArgumentException("This medicine is not available.");
        }

        com.example.pharmacymanagementsystem.dto.billing.BillingCartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getMedicineId().equals(medicine.getId()))
                .findFirst()
                .orElse(null);

        int existingQuantity = existingItem == null ? 0 : existingItem.getQuantity();
        int requestedQuantity = existingQuantity + request.getQuantity();

        if (requestedQuantity > medicine.getQuantity()) {
            throw new IllegalArgumentException("Only " + medicine.getQuantity() + " pcs available in stock.");
        }

        BigDecimal total = medicine.getSellingPrice().multiply(BigDecimal.valueOf(requestedQuantity));

        if (existingItem != null) {
            existingItem.setQuantity(requestedQuantity);
            existingItem.setTotal(total);
        } else {
            com.example.pharmacymanagementsystem.dto.billing.BillingCartItem item =
                    com.example.pharmacymanagementsystem.dto.billing.BillingCartItem.builder()
                            .medicineId(medicine.getId())
                            .medicineName(medicine.getMedicineName())
                            .genericName(medicine.getGenericName())
                            .batchNo(medicine.getBatchNo())
                            .price(medicine.getSellingPrice())
                            .quantity(request.getQuantity())
                            .total(medicine.getSellingPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                            .build();

            cart.getItems().add(item);
        }
    }

    public void removeFromCart(BillingCart cart, Long medicineId) {
        if (cart.getPendingBill() != null) {
            throw new IllegalArgumentException("Please confirm or cancel current bill first.");
        }

        cart.getItems().removeIf(item -> item.getMedicineId().equals(medicineId));
    }

    public void openCustomerBox(BillingCart cart) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Please add at least one medicine first.");
        }

        if (cart.getPendingBill() != null) {
            throw new IllegalArgumentException("Please confirm or cancel current bill first.");
        }

        cart.setCustomerBoxOpen(true);
    }

    public void makePendingBill(BillingCart cart, CustomerBillRequest request) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Please add medicine first.");
        }

        String invoiceNo = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        List<com.example.pharmacymanagementsystem.dto.billing.BillingCartItem> copiedItems = new ArrayList<>();

        for (com.example.pharmacymanagementsystem.dto.billing.BillingCartItem item : cart.getItems()) {
            copiedItems.add(com.example.pharmacymanagementsystem.dto.billing.BillingCartItem.builder()
                    .medicineId(item.getMedicineId())
                    .medicineName(item.getMedicineName())
                    .genericName(item.getGenericName())
                    .batchNo(item.getBatchNo())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .total(item.getTotal())
                    .build());
        }

        PendingBill pendingBill = PendingBill.builder()
                .invoiceNo(invoiceNo)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .dateTime(LocalDateTime.now())
                .items(copiedItems)
                .total(cart.getTotal())
                .build();

        cart.setPendingBill(pendingBill);
        cart.setCustomerBoxOpen(false);
    }

    public void cancelPendingBill(BillingCart cart) {
        if (cart.getPendingBill() == null) {
            throw new IllegalArgumentException("No bill found to cancel.");
        }

        cart.setPendingBill(null);
        cart.setCustomerBoxOpen(false);
    }

    @Transactional
    public Sale confirmBill(BillingCart cart) {
        PendingBill pendingBill = cart.getPendingBill();

        if (pendingBill == null) {
            throw new IllegalArgumentException("No bill found to confirm.");
        }

        Sale sale = Sale.builder()
                .invoiceNo(pendingBill.getInvoiceNo())
                .customerName(pendingBill.getCustomerName())
                .customerPhone(pendingBill.getCustomerPhone())
                .saleDateTime(LocalDateTime.now())
                .grandTotal(pendingBill.getTotal())
                .build();

        for (com.example.pharmacymanagementsystem.dto.billing.BillingCartItem cartItem : pendingBill.getItems()) {
            Medicine medicine = medicineRepository.findLockedById(cartItem.getMedicineId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + cartItem.getMedicineName()));

            if (medicine.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for " + medicine.getMedicineName()
                                + ". Available: " + medicine.getQuantity()
                                + " pcs."
                );
            }

            medicine.setQuantity(medicine.getQuantity() - cartItem.getQuantity());
            medicineRepository.save(medicine);

            SaleItem saleItem = SaleItem.builder()
                    .medicine(medicine)
                    .medicineName(medicine.getMedicineName())
                    .genericName(medicine.getGenericName())
                    .batchNo(medicine.getBatchNo())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPrice())
                    .lineTotal(cartItem.getTotal())
                    .build();

            sale.addItem(saleItem);
        }

        Sale savedSale = saleRepository.save(sale);
        cart.clearAll();

        return savedSale;
    }

    @Transactional(readOnly = true)
    public Sale getSaleById(Long id) {
        return saleRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found."));
    }
}