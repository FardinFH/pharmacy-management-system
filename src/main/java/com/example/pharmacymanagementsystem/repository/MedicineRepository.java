package com.example.pharmacymanagementsystem.repository;

import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.model.MedicineStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByBatchNoIgnoreCase(String batchNo);

    boolean existsByBatchNoIgnoreCaseAndIdNot(String batchNo, Long id);

    List<Medicine> findAllByOrderByCreatedAtDesc();

    List<Medicine> findByQuantityGreaterThanAndMedicineStatusOrderByMedicineNameAsc(
            Integer quantity,
            MedicineStatus medicineStatus
    );

    @Query("""
            SELECT m FROM Medicine m
            WHERE LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(m.batchNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(m.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY m.createdAt DESC
            """)
    List<Medicine> searchMedicines(@Param("keyword") String keyword);

    @Query("""
            SELECT m FROM Medicine m
            WHERE m.quantity > 0
              AND m.medicineStatus = com.example.pharmacymanagementsystem.model.MedicineStatus.AVAILABLE
              AND (
                    LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(m.batchNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(m.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY m.medicineName ASC
            """)
    List<Medicine> searchAvailableMedicines(@Param("keyword") String keyword);

    @Query("""
            SELECT COUNT(m)
            FROM Medicine m
            WHERE m.reorderLevel IS NOT NULL
              AND m.quantity <= m.reorderLevel
            """)
    long countLowStockMedicines();

    @Query("""
            SELECT m
            FROM Medicine m
            WHERE m.reorderLevel IS NOT NULL
              AND m.quantity <= m.reorderLevel
            ORDER BY m.quantity ASC, m.medicineName ASC
            """)
    List<Medicine> findLowStockMedicines();

    @Query("""
            SELECT COUNT(m) FROM Medicine m
            WHERE m.expiryDate > :today
              AND m.expiryDate <= :nearExpiryDate
            """)
    long countNearExpiryMedicines(
            @Param("today") LocalDate today,
            @Param("nearExpiryDate") LocalDate nearExpiryDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medicine m WHERE m.id = :id")
    Optional<Medicine> findLockedById(@Param("id") Long id);
}