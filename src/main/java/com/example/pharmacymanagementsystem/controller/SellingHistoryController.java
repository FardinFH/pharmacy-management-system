package com.example.pharmacymanagementsystem.controller;

import com.example.pharmacymanagementsystem.model.Sale;
import com.example.pharmacymanagementsystem.service.SellingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SellingHistoryController {

    private final SellingHistoryService sellingHistoryService;

    @GetMapping("/sell")
    public String sellingHistoryPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        List<Sale> sales = sellingHistoryService.getSalesHistory(keyword);

        model.addAttribute("sales", sales);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalSalesCount", sellingHistoryService.getTotalSalesCount());

        return "sell";
    }
}