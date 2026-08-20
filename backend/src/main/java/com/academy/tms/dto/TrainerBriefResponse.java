package com.academy.tms.dto;

import com.academy.tms.entities.Trainer;

public class TrainerBriefResponse {

    private Long id;
    private String name;

    public TrainerBriefResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TrainerBriefResponse from(Trainer trainer) {
        return new TrainerBriefResponse(trainer.getId(), trainer.getUser().getName());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
