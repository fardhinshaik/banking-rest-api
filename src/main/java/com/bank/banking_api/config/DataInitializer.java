package com.bank.banking_api.config;

import com.bank.banking_api.model.User;
import com.bank.banking_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed initial admin user if not present in database
        if (userRepository.findByUsername("admin_boss").isEmpty()) {
            User admin = User.builder()
                    .fullName("Bank Administrator")
                    .username("admin_boss")
                    .email("admin@bank.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN") // Set explicit Admin role
                    .build();

            userRepository.save(admin);
        }
    }
}