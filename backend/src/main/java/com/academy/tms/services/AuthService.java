package com.academy.tms.services;

import com.academy.tms.dto.LoginRequest;
import com.academy.tms.dto.LoginResponse;
import com.academy.tms.entities.User;
import com.academy.tms.exception.BadCredentialsException;
import com.academy.tms.repository.UserRepository;
import com.academy.tms.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
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

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                jwtService.getExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole().getRoleName().name()
        );
    }
}