package com.personal.store_api.config;

import com.personal.store_api.entity.Role;
import com.personal.store_api.entity.User;
import com.personal.store_api.repository.RoleRepository;
import com.personal.store_api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    static final String ADMIN_NAME = "admin";
    static final String ADMIN_EMAIL = "admin@example.com";
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner() {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name("USER")
                        .displayName("User")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name("ADMIN")
                        .displayName("Admin")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .email(ADMIN_EMAIL)
                        .name(ADMIN_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}
