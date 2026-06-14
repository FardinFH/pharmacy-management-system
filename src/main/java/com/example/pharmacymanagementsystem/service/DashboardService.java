package com.example.pharmacymanagementsystem.service;

import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.repository.MedicineRepository;
import com.example.pharmacymanagementsystem.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public long getTotalMedicine() {
        return medicineRepository.count();
    }

    @Transactional(readOnly = true)
    public long getLowStockCount() {
        return medicineRepository.countLowStockMedicines();
    }

    @Transactional(readOnly = true)
    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines();
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyTotalSell() {
        LocalDate today = LocalDate.now();

        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        BigDecimal totalSell = saleRepository.getMonthlyTotalSell(monthStart, nextMonthStart);

        return totalSell == null ? BigDecimal.ZERO : totalSell;
    }
}