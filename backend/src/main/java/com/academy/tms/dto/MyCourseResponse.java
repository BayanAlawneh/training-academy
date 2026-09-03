package com.academy.tms.dto;

import com.academy.tms.entities.Enrollment;

import java.time.LocalDate;

/**
 * الكورس كما يراه المتدرّب: بيانات الكورس + مدرّبه + تاريخ تسجيله هو فيه.
 * مختلف عن CourseResponse لأن المتدرّب لا يحتاج trainerId ولا حالة الامتلاء،
 * بل يحتاج "متى سجّلني الأدمن" و"مين مدرّبي".
 */
public class MyCourseResponse {

    private Long enrollmentId;
    private Long courseId;
    private String title;
    private Integer capacity;
    private String trainerName;
    private String trainerEmail;
    private String trainerSpecialization;
    private LocalDate enrolledOn;

    public MyCourseResponse(Long enrollmentId, Long courseId, String title, Integer capacity,
                            String trainerName, String trainerEmail,
                            String trainerSpecialization, LocalDate enrolledOn) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.title = title;
        this.capacity = capacity;
        this.trainerName = trainerName;
        this.trainerEmail = trainerEmail;
        this.trainerSpecialization = trainerSpecialization;
        this.enrolledOn = enrolledOn;
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
                enrollment.getEnrolledOn()
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
}
