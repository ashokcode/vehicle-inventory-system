package com.dealerhub.inventory.seed;

import com.dealerhub.inventory.domain.AdminUser;
import com.dealerhub.inventory.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures at least one admin login exists on a fresh database. Credentials come
 * from environment/config (never hard-coded), and the password is only ever
 * held as a BCrypt hash — this runner never logs or persists it in plaintext.
 */
@Component
public class AdminUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedUsername;
    private final String seedPassword;

    public AdminUserSeeder(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.seed-username:admin}") String seedUsername,
            @Value("${app.admin.seed-password:#{null}}") String seedPassword
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedUsername = seedUsername;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        if (seedPassword == null || seedPassword.isBlank()) {
            log.warn("No admin users exist and app.admin.seed-password is not set — "
                    + "set APP_ADMIN_SEED_PASSWORD and restart to create the first login.");
            return;
        }

        adminUserRepository.save(AdminUser.builder()
                .username(seedUsername)
                .passwordHash(passwordEncoder.encode(seedPassword))
                .enabled(true)
                .build());
        log.info("Seeded initial admin user '{}'", seedUsername);
    }
}
