package com.example.pharmacymanagementsystem.repository;

import com.example.pharmacymanagementsystem.model.Sale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByInvoiceNo(String invoiceNo);

    @EntityGraph(attributePaths = {"items"})
    Optional<Sale> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"items"})
    List<Sale> findAllByOrderBySaleDateTimeDesc();

    @EntityGraph(attributePaths = {"items"})
    @Query("""
            SELECT DISTINCT s
            FROM Sale s
            LEFT JOIN s.items i
            WHERE LOWER(s.invoiceNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(i.medicineName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(i.batchNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY s.saleDateTime DESC
            """)
    List<Sale> searchSalesHistory(@Param("keyword") String keyword);

    @Query("""
            SELECT COALESCE(SUM(s.grandTotal), 0)
            FROM Sale s
            WHERE s.saleDateTime >= :monthStart
              AND s.saleDateTime < :nextMonthStart
            """)
    BigDecimal getMonthlyTotalSell(
            @Param("monthStart") LocalDateTime monthStart,
            @Param("nextMonthStart") LocalDateTime nextMonthStart
    );
}