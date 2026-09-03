package com.academy.tms.dto;

import com.academy.tms.entities.TrainingSession;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String meetingLink;
    private String status;

    public SessionResponse(Long id, Long courseId, String courseTitle, String title,
                           LocalDate sessionDate, LocalTime startTime, LocalTime endTime,
                           String meetingLink, String status) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.title = title;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.meetingLink = meetingLink;
        this.status = status;
    }

    public static SessionResponse from(TrainingSession s) {
        return new SessionResponse(
                s.getId(),
                s.getCourse().getId(),
                s.getCourse().getTitle(),
                s.getTitle(),
                s.getSessionDate(),
                s.getStartTime(),
                s.getEndTime(),
                s.getMeetingLink(),
                s.getStatus().name()
        );
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getTitle() { return title; }
    public LocalDate getSessionDate() { return sessionDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getMeetingLink() { return meetingLink; }
    public String getStatus() { return status; }
}
