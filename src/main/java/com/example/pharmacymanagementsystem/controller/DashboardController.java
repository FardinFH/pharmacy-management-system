package com.example.pharmacymanagementsystem.controller;

import com.example.pharmacymanagementsystem.model.Medicine;
import com.example.pharmacymanagementsystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboardPage(Model model, Authentication authentication) {

        String adminName = authentication != null ? authentication.getName() : "Admin";

        String todayDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        long totalMedicine = dashboardService.getTotalMedicine();
        long lowStockCount = dashboardService.getLowStockCount();
        var monthlyTotalSell = dashboardService.getMonthlyTotalSell();
        List<Medicine> lowStockMedicines = dashboardService.getLowStockMedicines();

        model.addAttribute("adminName", adminName);
        model.addAttribute("todayDate", todayDate);
        model.addAttribute("totalMedicine", totalMedicine);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("monthlyTotalSell", monthlyTotalSell);
        model.addAttribute("lowStockMedicines", lowStockMedicines);

        return "dashboard";
    }
}