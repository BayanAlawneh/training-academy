package com.academy.tms.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollments_trainee_course",
                columnNames = {"trainee_id", "course_id"})
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn;

    protected Enrollment() {
    }

    public Enrollment(Trainee trainee, Course course, LocalDate enrolledOn) {
        this.trainee = trainee;
        this.course = course;
        this.enrolledOn = enrolledOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public void setTrainee(Trainee trainee) {
        this.trainee = trainee;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }
}
