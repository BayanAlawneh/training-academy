package com.academy.tms.dto;

import com.academy.tms.entities.Course;

public class CourseResponse {

    private Long id;
    private String title;
    private Integer capacity;
    private Long trainerId;
    private String trainerName;
    private long enrolledCount;
    private boolean full;

    public CourseResponse(Long id, String title, Integer capacity, Long trainerId,
                          String trainerName, long enrolledCount) {
        this.id = id;
        this.title = title;
        this.capacity = capacity;
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.enrolledCount = enrolledCount;
        this.full = enrolledCount >= capacity;
    }

    public static CourseResponse from(Course course, long enrolledCount) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getCapacity(),
                course.getTrainer().getId(),
                course.getTrainer().getUser().getName(),
                enrolledCount
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public boolean isFull() {
        return full;
    }
}
