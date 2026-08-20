package com.academy.tms.dto;

import com.academy.tms.entities.Course;

public class CourseBriefResponse {

    private Long id;
    private String title;

    public CourseBriefResponse(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public static CourseBriefResponse from(Course course) {
        return new CourseBriefResponse(course.getId(), course.getTitle());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
