package com.cryo.auth.config;

import com.cryo.auth.entity.User;
import com.cryo.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ROOT_ADMIN_EMAIL:#{null}}")
    private String adminEmail;

    @Value("${ROOT_ADMIN_PASSWORD:#{null}}")
    private String adminPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminPassword == null) {
            logger.info("ℹ️ No ROOT_ADMIN environment variables found. Skipping admin bootstrap.");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            logger.info("ℹ️ Root Admin ({}) already exists. No action needed.", adminEmail);
            return;
        }

        try {
            User rootAdmin = new User();
            rootAdmin.setName("System Administrator");
            rootAdmin.setEmail(adminEmail);

            // ✅ FIX: Changed to start with '9' to pass validation regex
            rootAdmin.setMobileNumber("+919999999999");

            rootAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
            rootAdmin.setRoles("ADMIN");
            rootAdmin.setStatus(User.UserStatus.ACTIVE);
            rootAdmin.setOwnerUserId("A00001");

            userRepository.save(rootAdmin);
            logger.info("✅ ROOT ADMIN created successfully from Environment Config: {}", adminEmail);

        } catch (Exception e) {
            logger.error("❌ Failed to bootstrap Root Admin", e);
        }
    }
}