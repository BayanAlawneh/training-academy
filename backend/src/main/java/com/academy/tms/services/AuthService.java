package com.academy.tms.services;

import com.academy.tms.dto.LoginRequest;
import com.academy.tms.dto.LoginResponse;
import com.academy.tms.dto.SignupRequest;
import com.academy.tms.entities.Role;
import com.academy.tms.entities.RoleName;
import com.academy.tms.entities.Trainee;
import com.academy.tms.entities.User;
import com.academy.tms.exception.BadCredentialsException;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.repository.RoleRepository;
import com.academy.tms.repository.TraineeRepository;
import com.academy.tms.repository.UserRepository;
import com.academy.tms.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TraineeRepository traineeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       TraineeRepository traineeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.traineeRepository = traineeRepository;
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

    /**
     * التسجيل الذاتي يُنشئ متدرّباً — لا مديراً.
     *
     * كان هذا السطر يمنح RoleName.ADMIN، ومسار /api/auth/signup مفتوح للجميع
     * في SecurityConfig، فكان أي شخص يفتح صفحة التسجيل يحصل على صلاحية كاملة
     * لحذف المدربين وتعديل الكورسات. حسابات المدير تُنشأ عبر التهيئة الأولية
     * أو من مدير قائم، لا من نموذج عام.
     *
     * ويُنشأ معه صفّ في جدول trainees داخل نفس المعاملة. بدون ذلك يبقى الحساب
     * "يتيماً": يسجّل الدخول لكنه غير موجود كمتدرّب، فلا يظهر للمدير ولا يمكن
     * تسجيله في كورس، وكل صفحاته ترجع 404.
     */
    @Transactional
    public LoginResponse signup(SignupRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("This email is already registered");
        }

        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        Role traineeRole = roleRepository.findByRoleName(RoleName.TRAINEE)
                .orElseThrow(() -> new IllegalStateException("TRAINEE role not found"));

        User user = new User(
                request.getUsername(),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDateOfBirth(),
                traineeRole
        );

        User saved = userRepository.save(user);
        traineeRepository.save(new Trainee(saved, LocalDate.now()));

        return buildResponse(saved);
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
