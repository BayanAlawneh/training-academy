package com.academy.tms.dto;

import com.academy.tms.entities.Enrollment;

import java.time.LocalDate;

/** الكورس كما يراه المتدرّب: بياناته + مدرّبه + تاريخ تسجيله هو فيه. */
public class MyCourseResponse {

    private Long enrollmentId;
    private Long courseId;
    private String title;
    private Integer capacity;
    private String trainerName;
    private String trainerEmail;
    private String trainerSpecialization;
    private LocalDate enrolledOn;
    private String description;
    private Integer durationWeeks;

    public MyCourseResponse(Long enrollmentId, Long courseId, String title, Integer capacity,
                            String trainerName, String trainerEmail,
                            String trainerSpecialization, LocalDate enrolledOn,
                            String description, Integer durationWeeks) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.title = title;
        this.capacity = capacity;
        this.trainerName = trainerName;
        this.trainerEmail = trainerEmail;
        this.trainerSpecialization = trainerSpecialization;
        this.enrolledOn = enrolledOn;
        this.description = description;
        this.durationWeeks = durationWeeks;
    }

    public static MyCourseResponse from(Enrollment enrollment) {
        var course = enrollment.getCourse();
        var trainerUser = course.getTrainer().getUser();

        return new MyCourseResponse(
                enrollment.getId(),
                course.getId(),
                course.getTitle(),
                course.getCapacity(),
                trainerUser.getName(),
                trainerUser.getEmail(),
                course.getTrainer().getSpecialization(),
                enrollment.getEnrolledOn(),
                course.getDescription(),
                course.getDurationWeeks()
        );
    }

    public Long getEnrollmentId() { return enrollmentId; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public Integer getCapacity() { return capacity; }
    public String getTrainerName() { return trainerName; }
    public String getTrainerEmail() { return trainerEmail; }
    public String getTrainerSpecialization() { return trainerSpecialization; }
    public LocalDate getEnrolledOn() { return enrolledOn; }
    public String getDescription() { return description; }
    public Integer getDurationWeeks() { return durationWeeks; }
}
