package com.academy.tms.dto;

import com.academy.tms.entities.Trainee;

import java.time.LocalDate;

public class TraineeResponse {

    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;

    public TraineeResponse(Long id, Long userId, String username, String name,
                           String email, LocalDate dateOfBirth, LocalDate enrollmentDate) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.enrollmentDate = enrollmentDate;
    }

    public static TraineeResponse from(Trainee trainee) {
        return new TraineeResponse(
                trainee.getId(),
                trainee.getUser().getId(),
                trainee.getUser().getUsername(),
                trainee.getUser().getName(),
                trainee.getUser().getEmail(),
                trainee.getUser().getDateOfBirth(),
                trainee.getEnrollmentDate()
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

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
}