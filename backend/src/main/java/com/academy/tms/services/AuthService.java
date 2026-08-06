package com.academy.tms.services;

import com.academy.tms.dto.LoginRequest;
import com.academy.tms.dto.LoginResponse;
import com.academy.tms.dto.SignupRequest;
import com.academy.tms.entities.Role;
import com.academy.tms.entities.RoleName;
import com.academy.tms.entities.User;
import com.academy.tms.exception.BadCredentialsException;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.repository.RoleRepository;
import com.academy.tms.repository.UserRepository;
import com.academy.tms.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmailWithRole(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return buildResponse(user);
    }

    @Transactional
    public LoginResponse signup(SignupRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("This email is already registered");
        }

        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User user = new User(
                request.getUsername(),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDateOfBirth(),
                adminRole
        );

        return buildResponse(userRepository.save(user));
    }

    private LoginResponse buildResponse(User user) {
        return new LoginResponse(
                jwtService.generateToken(user),
                jwtService.getExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole().getRoleName().name()
        );
    }
}