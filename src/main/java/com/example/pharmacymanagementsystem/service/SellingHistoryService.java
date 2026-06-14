package com.example.pharmacymanagementsystem.service;

import com.example.pharmacymanagementsystem.model.Sale;
import com.example.pharmacymanagementsystem.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellingHistoryService {

    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<Sale> getSalesHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return saleRepository.findAllByOrderBySaleDateTimeDesc();
        }

        return saleRepository.searchSalesHistory(keyword.trim());
    }

    @Transactional(readOnly = true)
    public long getTotalSalesCount() {
        return saleRepository.count();
    }
}