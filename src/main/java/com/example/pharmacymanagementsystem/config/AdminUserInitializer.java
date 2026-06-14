package com.example.pharmacymanagementsystem.config;

import com.example.pharmacymanagementsystem.model.Admin;
import com.example.pharmacymanagementsystem.model.Role;
import com.example.pharmacymanagementsystem.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    /*
     * Time Complexity: O(1) average case because username has unique index.
     * Space Complexity: O(1)
     */
    @Override
    @Transactional
    public void run(String... args) {

        String username = adminUsername.trim();

        if (username.isBlank()) {
            throw new IllegalStateException("ADMIN_USERNAME environment variable is empty.");
        }

        if (adminPassword == null || adminPassword.trim().isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD environment variable is empty.");
        }

        if (adminRepository.existsByUsername(username)) {
            return;
        }

        Admin admin = Admin.builder()
                .username(username)
                .password(passwordEncoder.encode(adminPassword.trim()))
                .fullName(adminFullName.trim().isBlank() ? "Main Admin" : adminFullName.trim())
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        adminRepository.save(admin);
    }
}