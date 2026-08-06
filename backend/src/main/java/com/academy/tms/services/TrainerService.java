package com.academy.tms.services;

import com.academy.tms.dto.TrainerRequest;
import com.academy.tms.dto.TrainerResponse;
import com.academy.tms.entities.Role;
import com.academy.tms.entities.RoleName;
import com.academy.tms.entities.Trainer;
import com.academy.tms.entities.User;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.RoleRepository;
import com.academy.tms.repository.TrainerRepository;
import com.academy.tms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TrainerService(TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TrainerResponse> findAll() {
        return trainerRepository.findAllWithUser().stream()
                .map(TrainerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainerResponse findById(Long id) {
        return TrainerResponse.from(loadTrainer(id));
    }

    @Transactional
    public TrainerResponse create(TrainerRequest request) {

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required for a new trainer");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("This email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        Role trainerRole = roleRepository.findByRoleName(RoleName.TRAINER)
                .orElseThrow(() -> new IllegalStateException("TRAINER role not found"));

        User user = new User(
                request.getUsername(),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDateOfBirth(),
                trainerRole
        );

        Trainer trainer = new Trainer(userRepository.save(user), request.getSpecialization());
        return TrainerResponse.from(trainerRepository.save(trainer));
    }

    @Transactional
    public TrainerResponse update(Long id, TrainerRequest request) {

        Trainer trainer = loadTrainer(id);
        User user = trainer.getUser();

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

        trainer.setSpecialization(request.getSpecialization());

        return TrainerResponse.from(trainer);
    }

    @Transactional
    public void delete(Long id) {
        Trainer trainer = loadTrainer(id);
        User user = trainer.getUser();
        trainerRepository.delete(trainer);
        userRepository.delete(user);
    }

    private Trainer loadTrainer(Long id) {
        return trainerRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + id));
    }
}