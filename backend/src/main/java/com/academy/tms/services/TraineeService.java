package com.academy.tms.services;

import com.academy.tms.dto.TraineeRequest;
import com.academy.tms.dto.TraineeResponse;
import com.academy.tms.entities.Role;
import com.academy.tms.entities.RoleName;
import com.academy.tms.entities.Trainee;
import com.academy.tms.entities.User;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.RoleRepository;
import com.academy.tms.repository.TraineeRepository;
import com.academy.tms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TraineeService(TraineeRepository traineeRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TraineeResponse> findAll() {
        return traineeRepository.findAllWithUser().stream()
                .map(TraineeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TraineeResponse findById(Long id) {
        return TraineeResponse.from(loadTrainee(id));
    }

    @Transactional
    public TraineeResponse create(TraineeRequest request) {

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required for a new trainee");
        }
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

        LocalDate enrollmentDate = request.getEnrollmentDate() != null
                ? request.getEnrollmentDate()
                : LocalDate.now();

        Trainee trainee = new Trainee(userRepository.save(user), enrollmentDate);
        return TraineeResponse.from(traineeRepository.save(trainee));
    }

    @Transactional
    public TraineeResponse update(Long id, TraineeRequest request) {

        Trainee trainee = loadTrainee(id);
        User user = trainee.getUser();

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("This email is already registered");
        }
        if (!user.getUsername().equalsIgnoreCase(request.getUsername())
                && userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getEnrollmentDate() != null) {
            trainee.setEnrollmentDate(request.getEnrollmentDate());
        }

        return TraineeResponse.from(trainee);
    }

    @Transactional
    public void delete(Long id) {
        Trainee trainee = loadTrainee(id);
        User user = trainee.getUser();
        traineeRepository.delete(trainee);
        userRepository.delete(user);
    }

    private Trainee loadTrainee(Long id) {
        return traineeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + id));
    }
}