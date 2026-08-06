package com.academy.tms.dto;

import com.academy.tms.entities.Trainer;

import java.time.LocalDate;

public class TrainerResponse {

    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private String specialization;

    public TrainerResponse(Long id, Long userId, String username, String name,
                           String email, LocalDate dateOfBirth, String specialization) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.specialization = specialization;
    }

    public static TrainerResponse from(Trainer trainer) {
        return new TrainerResponse(
                trainer.getId(),
                trainer.getUser().getId(),
                trainer.getUser().getUsername(),
                trainer.getUser().getName(),
                trainer.getUser().getEmail(),
                trainer.getUser().getDateOfBirth(),
                trainer.getSpecialization()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getSpecialization() {
        return specialization;
    }
}