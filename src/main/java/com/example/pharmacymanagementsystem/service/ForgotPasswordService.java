package com.example.pharmacymanagementsystem.service;

import com.example.pharmacymanagementsystem.model.Admin;
import com.example.pharmacymanagementsystem.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean usernameExists(String username) {

        if (username == null || username.trim().isBlank()) {
            return false;
        }

        return adminRepository.existsByUsername(username.trim());
    }

    @Transactional
    public void resetPassword(String username, String newPassword, String confirmPassword) {

        if (username == null || username.trim().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (newPassword == null || newPassword.trim().isBlank()) {
            throw new IllegalArgumentException("New password is required.");
        }

        if (confirmPassword == null || confirmPassword.trim().isBlank()) {
            throw new IllegalArgumentException("Confirm password is required.");
        }

        String cleanUsername = username.trim();
        String cleanPassword = newPassword.trim();
        String cleanConfirmPassword = confirmPassword.trim();

        if (cleanPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }

        if (!cleanPassword.equals(cleanConfirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        Admin admin = adminRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new IllegalArgumentException("Username does not match."));

        admin.setPassword(passwordEncoder.encode(cleanPassword));

        adminRepository.save(admin);
    }
}