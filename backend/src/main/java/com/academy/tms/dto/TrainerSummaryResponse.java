package com.academy.tms.dto;

/** ملخّص لوحة المدرّب. courseId يكون null إن لم يُسند إليه كورس بعد. */
public class TrainerSummaryResponse {

    private String name;
    private String email;
    private String specialization;
    private Long courseId;
    private String courseTitle;
    private long traineeCount;
    private long sessionCount;

    public TrainerSummaryResponse(String name, String email, String specialization,
                                  Long courseId, String courseTitle,
                                  long traineeCount, long sessionCount) {
        this.name = name;
        this.email = email;
        this.specialization = specialization;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.traineeCount = traineeCount;
        this.sessionCount = sessionCount;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public long getTraineeCount() { return traineeCount; }
    public long getSessionCount() { return sessionCount; }
}
