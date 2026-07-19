package com.erp.auth.config;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Password@123";

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Auth seed data already exists — skipping.");
            return;
        }

        log.info("Seeding default users...");

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@college.edu")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(User.Role.ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        userRepository.save(User.builder()
                .username("faculty1")
                .email("faculty1@college.edu")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(User.Role.FACULTY)
                .referenceId(1L)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        userRepository.save(User.builder()
                .username("faculty2")
                .email("faculty2@college.edu")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(User.Role.FACULTY)
                .referenceId(2L)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        userRepository.save(User.builder()
                .username("student1")
                .email("student1@college.edu")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(User.Role.STUDENT)
                .referenceId(1L)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        userRepository.save(User.builder()
                .username("parent1")
                .email("parent1@gmail.com")
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(User.Role.PARENT)
                .referenceId(1L)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        log.info("Default users seeded. Login with username 'admin' / password 'Password@123'");
    }
}
