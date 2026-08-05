package com.academy.tms.config;

import com.academy.tms.entities.Role;
import com.academy.tms.entities.RoleName;
import com.academy.tms.entities.User;
import com.academy.tms.repository.RoleRepository;
import com.academy.tms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "password123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("admin", "System Admin", "admin@gmail.com",
                LocalDate.of(1990, 1, 15), RoleName.ADMIN);
        seedUser("ahmed", "Ahmed Zaid", "ahmed@gmail.com",
                LocalDate.of(1988, 6, 22), RoleName.TRAINER);
        seedUser("sara", "Sara Khalil", "sara@gmail.com",
                LocalDate.of(2001, 3, 10), RoleName.TRAINEE);
    }

    private void seedUser(String username, String name, String email,
                          LocalDate dateOfBirth, RoleName roleName) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        userRepository.save(new User(username, name, email,
                passwordEncoder.encode(DEMO_PASSWORD), dateOfBirth, role));
    }
}