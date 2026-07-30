package com.academy.tms.config;

import com.academy.tms.user.entity.Role;
import com.academy.tms.user.entity.RoleName;
import com.academy.tms.user.entity.User;
import com.academy.tms.user.repository.RoleRepository;
import com.academy.tms.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        seedUser("System Admin", "admin@gmail.com", RoleName.ADMIN);
        seedUser("Ahmed Zaid", "ahmed@gmail.com", RoleName.TRAINER);
        seedUser("Sara Khalil", "sara@gmail.com", RoleName.TRAINEE);
    }

    private void seedUser(String name, String email, RoleName roleName) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        userRepository.save(new User(name, email, passwordEncoder.encode(DEMO_PASSWORD), role));
    }
}