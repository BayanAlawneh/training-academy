package com.academy.tms.dto;

import jakarta.validation.constraints.NotNull;

public class EnrollmentRequest {

    @NotNull(message = "A course must be selected")
    private Long courseId;

    @NotNull(message = "A trainee must be selected")
    private Long traineeId;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(Long traineeId) {
        this.traineeId = traineeId;
    }
}
