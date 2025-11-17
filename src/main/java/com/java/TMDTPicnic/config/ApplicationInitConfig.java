package com.java.TMDTPicnic.config;

import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.enums.Role;
import com.java.TMDTPicnic.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String ADMIN_EMAIL = "admin@example.com";

    @NonFinal
    static final String ADMIN_PHONE = "0123456789";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .email(ADMIN_EMAIL)
                        .fullName("Admin")
                        .phone(ADMIN_PHONE)
                        .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(Role.ADMIN)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin, please change it!");
            }
            log.info("Application initialization completed.");
        };
    }
}

