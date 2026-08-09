package com.academy.tms.dto;

import com.academy.tms.entities.Enrollment;

import java.time.LocalDate;

public class EnrollmentResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long traineeId;
    private String traineeName;
    private String traineeEmail;
    private LocalDate enrolledOn;

    public EnrollmentResponse(Long id, Long courseId, String courseTitle, Long traineeId,
                              String traineeName, String traineeEmail, LocalDate enrolledOn) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.traineeId = traineeId;
        this.traineeName = traineeName;
        this.traineeEmail = traineeEmail;
        this.enrolledOn = enrolledOn;
    }

    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getTrainee().getId(),
                enrollment.getTrainee().getUser().getName(),
                enrollment.getTrainee().getUser().getEmail(),
                enrollment.getEnrolledOn()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public String getTraineeName() {
        return traineeName;
    }

    public String getTraineeEmail() {
        return traineeEmail;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }
}
