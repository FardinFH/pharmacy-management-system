package com.example.pharmacymanagementsystem.controller;

import com.example.pharmacymanagementsystem.dto.AddToCartRequest;
import com.example.pharmacymanagementsystem.dto.BillingCart;
import com.example.pharmacymanagementsystem.dto.CustomerBillRequest;
import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.model.Sale;
import com.example.pharmacymanagementsystem.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("billingCart")
public class SellBillingController {

    private final BillingService billingService;

    @ModelAttribute("billingCart")
    public BillingCart billingCart() {
        return new BillingCart();
    }

    @GetMapping("/sell-billing")
    public String sellBillingPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "selectedMedicineId", required = false) Long selectedMedicineId,
            @ModelAttribute("billingCart") BillingCart billingCart,
            Model model
    ) {
        List<Medicine> medicines = billingService.getAvailableMedicines(keyword);

        if (!model.containsAttribute("addToCartRequest")) {
            AddToCartRequest addToCartRequest = new AddToCartRequest();

            if (selectedMedicineId != null) {
                addToCartRequest.setMedicineId(selectedMedicineId);
            }

            model.addAttribute("addToCartRequest", addToCartRequest);
        }

        if (!model.containsAttribute("customerBillRequest")) {
            model.addAttribute("customerBillRequest", new CustomerBillRequest());
        }

        Medicine selectedMedicine = null;

        if (selectedMedicineId != null) {
            selectedMedicine = medicines.stream()
                    .filter(medicine -> medicine.getId().equals(selectedMedicineId))
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("medicines", medicines);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedMedicineId", selectedMedicineId);
        model.addAttribute("selectedMedicine", selectedMedicine);
        model.addAttribute("now", LocalDateTime.now());
        model.addAttribute("cartTotal", billingCart.getTotal());

        return "sell-billing";
    }

    @PostMapping("/sell-billing/cart/add")
    public String addMedicineToCart(
            @Valid @ModelAttribute("addToCartRequest") AddToCartRequest addToCartRequest,
            BindingResult bindingResult,
            @ModelAttribute("billingCart") BillingCart billingCart,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select medicine and enter valid quantity.");
            redirectAttributes.addFlashAttribute("addToCartRequest", addToCartRequest);
            return "redirect:/sell-billing";
        }

        try {
            billingService.addToCart(billingCart, addToCartRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Medicine added to bill.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("addToCartRequest", addToCartRequest);
            return "redirect:/sell-billing?selectedMedicineId=" + addToCartRequest.getMedicineId();
        }

        return "redirect:/sell-billing";
    }

    @PostMapping("/sell-billing/cart/remove/{medicineId}")
    public String removeMedicineFromCart(
            @PathVariable Long medicineId,
            @ModelAttribute("billingCart") BillingCart billingCart,
            RedirectAttributes redirectAttributes
    ) {
        try {
            billingService.removeFromCart(billingCart, medicineId);
            redirectAttributes.addFlashAttribute("successMessage", "Medicine removed from bill.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/sell-billing";
    }

    @PostMapping("/sell-billing/finish")
    public String finishBill(
            @ModelAttribute("billingCart") BillingCart billingCart,
            RedirectAttributes redirectAttributes
    ) {
        try {
            billingService.openCustomerBox(billingCart);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/sell-billing";
    }

    @PostMapping("/sell-billing/make-bill")
    public String makeBill(
            @Valid @ModelAttribute("customerBillRequest") CustomerBillRequest customerBillRequest,
            BindingResult bindingResult,
            @ModelAttribute("billingCart") BillingCart billingCart,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            billingCart.setCustomerBoxOpen(true);
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter customer name and phone number.");
            redirectAttributes.addFlashAttribute("customerBillRequest", customerBillRequest);
            return "redirect:/sell-billing";
        }

        try {
            billingService.makePendingBill(billingCart, customerBillRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Bill created. Please confirm or cancel.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/sell-billing";
    }

    @PostMapping("/sell-billing/cancel")
    public String cancelBill(
            @ModelAttribute("billingCart") BillingCart billingCart,
            RedirectAttributes redirectAttributes
    ) {
        try {
            billingService.cancelPendingBill(billingCart);
            redirectAttributes.addFlashAttribute("errorMessage", "Bill cancelled. Stock quantity was not changed.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/sell-billing";
    }

    @PostMapping("/sell-billing/confirm")
    public String confirmBill(
            @ModelAttribute("billingCart") BillingCart billingCart,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Sale sale = billingService.confirmBill(billingCart);
            redirectAttributes.addFlashAttribute("successMessage", "Bill confirmed successfully. Stock updated. Now you can print.");
            redirectAttributes.addFlashAttribute("confirmedSaleId", sale.getId());
            return "redirect:/sell-billing";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/sell-billing";
        }
    }

    @GetMapping("/invoice/{id}")
    public String invoicePage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Sale sale = billingService.getSaleById(id);
            model.addAttribute("sale", sale);
            return "invoice";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/sell-billing";
        }
    }
}