package com.example.pharmacymanagementsystem.service;

import com.example.pharmacymanagementsystem.dto.MedicineRequest;
import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    @Transactional
    public Medicine addMedicine(MedicineRequest request) {
        String batchNo = clean(request.getBatchNo());

        if (medicineRepository.existsByBatchNoIgnoreCase(batchNo)) {
            throw new IllegalArgumentException("This batch number already exists.");
        }

        Medicine medicine = Medicine.builder()
                .medicineName(clean(request.getMedicineName()))
                .genericName(clean(request.getGenericName()))
                .category(clean(request.getCategory()))
                .manufacturer(clean(request.getManufacturer()))
                .batchNo(batchNo)
                .unit("pcs")
                .quantity(request.getQuantity())
                .reorderLevel(request.getReorderLevel())
                .sellingPrice(request.getSellingPrice())
                .expiryDate(request.getExpiryDate())
                .supplier(cleanNullable(request.getSupplier()))
                .rackNo(cleanNullable(request.getRackNo()))
                .medicineStatus(request.getMedicineStatus())
                .description(cleanNullable(request.getDescription()))
                .build();

        return medicineRepository.save(medicine);
    }

    @Transactional(readOnly = true)
    public List<Medicine> getAllMedicines(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return medicineRepository.findAllByOrderByCreatedAtDesc();
        }

        return medicineRepository.searchMedicines(keyword.trim());
    }

    @Transactional(readOnly = true)
    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found."));
    }

    @Transactional
    public void updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = getMedicineById(id);

        String batchNo = clean(request.getBatchNo());

        if (medicineRepository.existsByBatchNoIgnoreCaseAndIdNot(batchNo, id)) {
            throw new IllegalArgumentException("This batch number already exists.");
        }

        medicine.setMedicineName(clean(request.getMedicineName()));
        medicine.setGenericName(clean(request.getGenericName()));
        medicine.setCategory(clean(request.getCategory()));
        medicine.setManufacturer(clean(request.getManufacturer()));
        medicine.setBatchNo(batchNo);
        medicine.setUnit("pcs");
        medicine.setQuantity(request.getQuantity());
        medicine.setReorderLevel(request.getReorderLevel());
        medicine.setSellingPrice(request.getSellingPrice());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setSupplier(cleanNullable(request.getSupplier()));
        medicine.setRackNo(cleanNullable(request.getRackNo()));
        medicine.setMedicineStatus(request.getMedicineStatus());
        medicine.setDescription(cleanNullable(request.getDescription()));

        medicineRepository.save(medicine);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new IllegalArgumentException("Medicine not found.");
        }

        medicineRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countTotalMedicines() {
        return medicineRepository.count();
    }

    @Transactional(readOnly = true)
    public long countLowStockMedicines() {
        return medicineRepository.countLowStockMedicines();
    }

    @Transactional(readOnly = true)
    public long countNearExpiryMedicines() {
        LocalDate today = LocalDate.now();
        LocalDate nearExpiryDate = today.plusDays(60);

        return medicineRepository.countNearExpiryMedicines(today, nearExpiryDate);
    }

    public MedicineRequest convertToRequest(Medicine medicine) {
        return MedicineRequest.builder()
                .medicineName(medicine.getMedicineName())
                .genericName(medicine.getGenericName())
                .category(medicine.getCategory())
                .manufacturer(medicine.getManufacturer())
                .batchNo(medicine.getBatchNo())
                .quantity(medicine.getQuantity())
                .reorderLevel(medicine.getReorderLevel())
                .sellingPrice(medicine.getSellingPrice())
                .expiryDate(medicine.getExpiryDate())
                .supplier(medicine.getSupplier())
                .rackNo(medicine.getRackNo())
                .medicineStatus(medicine.getMedicineStatus())
                .description(medicine.getDescription())
                .build();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
    @Transactional
    public boolean deleteOrDeactivateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found."));

        try {
            medicineRepository.delete(medicine);
            medicineRepository.flush();
            return true; // permanently deleted
        } catch (Exception exception) {
            medicine.setMedicineStatus(com.example.pharmacymanagementsystem.model.MedicineStatus.NOT_AVAILABLE);
            medicine.setQuantity(0);
            medicineRepository.save(medicine);
            return false; // deactivated because sales history exists
        }
    }
}
