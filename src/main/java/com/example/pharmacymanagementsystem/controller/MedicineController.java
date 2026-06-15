package com.example.pharmacymanagementsystem.controller;

import com.example.pharmacymanagementsystem.dto.MedicineRequest;
import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.model.MedicineStatus;
import com.example.pharmacymanagementsystem.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping("/add-medicine")
    public String addMedicinePage(Model model) {
        if (!model.containsAttribute("medicineRequest")) {
            model.addAttribute("medicineRequest", new MedicineRequest());
        }

        model.addAttribute("medicineStatuses", MedicineStatus.values());
        return "add-medicine";
    }

    @PostMapping("/add-medicine")
    public String saveMedicine(
            @Valid @ModelAttribute("medicineRequest") MedicineRequest medicineRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("medicineStatuses", MedicineStatus.values());
            model.addAttribute("errorMessage", "Please fill all required fields correctly.");
            return "add-medicine";
        }

        try {
            medicineService.addMedicine(medicineRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Medicine added successfully!");
            return "redirect:/add-medicine";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("medicineStatuses", MedicineStatus.values());
            model.addAttribute("errorMessage", exception.getMessage());
            return "add-medicine";
        } catch (Exception exception) {
            model.addAttribute("medicineStatuses", MedicineStatus.values());
            model.addAttribute("errorMessage", "Something went wrong while adding medicine.");
            return "add-medicine";
        }
    }

    @GetMapping("/stock")
    public String stockPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        List<Medicine> medicines = medicineService.getAllMedicines(keyword);

        model.addAttribute("medicines", medicines);
        model.addAttribute("keyword", keyword);

        model.addAttribute("totalMedicine", medicineService.countTotalMedicines());
        model.addAttribute("lowStockCount", medicineService.countLowStockMedicines());
        model.addAttribute("nearExpiryCount", medicineService.countNearExpiryMedicines());

        model.addAttribute("today", LocalDate.now());
        model.addAttribute("nearExpiryDate", LocalDate.now().plusDays(60));

        return "stock";
    }

    @GetMapping("/medicines/edit/{id}")
    public String editMedicinePage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Medicine medicine = medicineService.getMedicineById(id);

            model.addAttribute("medicineId", medicine.getId());
            model.addAttribute("medicineRequest", medicineService.convertToRequest(medicine));
            model.addAttribute("medicineStatuses", MedicineStatus.values());

            return "edit-medicine";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/stock";
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong while opening edit page.");
            return "redirect:/stock";
        }
    }

    @PostMapping("/medicines/update/{id}")
    public String updateMedicine(
            @PathVariable Long id,
            @Valid @ModelAttribute("medicineRequest") MedicineRequest medicineRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("medicineId", id);
            model.addAttribute("medicineStatuses", MedicineStatus.values());
            model.addAttribute("errorMessage", "Please fill all required fields correctly.");
            return "edit-medicine";
        }

        try {
            medicineService.updateMedicine(id, medicineRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Medicine updated successfully!");
            return "redirect:/stock";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("medicineId", id);
            model.addAttribute("medicineStatuses", MedicineStatus.values());
            model.addAttribute("errorMessage", exception.getMessage());
            return "edit-medicine";
        } catch (Exception exception) {
            model.addAttribute("medicineId", id);
            model.addAttribute("medicineStatuses", MedicineStatus.values());
            model.addAttribute("errorMessage", "Something went wrong while updating medicine.");
            return "edit-medicine";
        }
    }

    @PostMapping("/medicines/delete/{id}")
    public String deleteMedicine(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean deleted = medicineService.deleteOrDeactivateMedicine(id);

            if (deleted) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Medicine deleted successfully!"
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Medicine is used in sales history, so it has been deactivated instead of deleted."
                );
            }

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Something went wrong while deleting medicine."
            );
        }

        return "redirect:/stock";
    }
}