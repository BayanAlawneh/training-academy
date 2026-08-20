package com.academy.tms.dto;

import java.util.List;

public class TrainerDeletionCheckResponse {

    private List<CourseBriefResponse> courses;
    private List<TrainerBriefResponse> alternativeTrainers;

    public TrainerDeletionCheckResponse(List<CourseBriefResponse> courses,
                                        List<TrainerBriefResponse> alternativeTrainers) {
        this.courses = courses;
        this.alternativeTrainers = alternativeTrainers;
    }

    public List<CourseBriefResponse> getCourses() {
        return courses;
    }

    public List<TrainerBriefResponse> getAlternativeTrainers() {
        return alternativeTrainers;
    }
}
