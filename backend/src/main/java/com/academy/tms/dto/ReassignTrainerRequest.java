package com.academy.tms.dto;

import jakarta.validation.constraints.NotNull;

public class ReassignTrainerRequest {

    @NotNull(message = "A trainer must be selected")
    private Long trainerId;

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
}
