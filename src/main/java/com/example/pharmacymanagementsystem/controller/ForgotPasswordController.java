package com.example.pharmacymanagementsystem.controller;

import com.example.pharmacymanagementsystem.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Authentication authentication, Model model) {

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("username", "");
        model.addAttribute("verifiedUsername", null);

        return "forget";
    }

    @PostMapping("/forgot-password/verify")
    public String verifyUsername(
            @RequestParam("username") String username,
            Model model
    ) {
        String cleanUsername = username == null ? "" : username.trim();

        if (forgotPasswordService.usernameExists(cleanUsername)) {
            model.addAttribute("username", cleanUsername);
            model.addAttribute("verifiedUsername", cleanUsername);
            model.addAttribute("successMessage", "Username matched. Now set your new password.");
        } else {
            model.addAttribute("username", cleanUsername);
            model.addAttribute("verifiedUsername", null);
            model.addAttribute("errorMessage", "Username does not match!");
        }

        return "forget";
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(
            @RequestParam("username") String username,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            forgotPasswordService.resetPassword(username, newPassword, confirmPassword);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Password reset successful! Please login with your new password."
            );

            return "redirect:/login";

        } catch (IllegalArgumentException exception) {

            model.addAttribute("username", username);
            model.addAttribute("verifiedUsername", username);
            model.addAttribute("errorMessage", exception.getMessage());

            return "forget";
        }
    }
}