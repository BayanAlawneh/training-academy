package com.academy.tms.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * جلسة تدريبية واحدة ضمن كورس.
 *
 * سُمّي الصنف TrainingSession وليس Session لأن Session اسم مستخدم في
 * jakarta وspring، وتشابه الأسماء يسبب استيرادات خاطئة يصعب تتبّعها.
 *
 * هذا الجدول هو العمود الفقري: صفحة "حضوري" للمتدرّب، وجدول المواعيد،
 * ورابط اللقاء — كلها تُقرأ منه، فلا حاجة لجداول منفصلة لكل ميزة.
 */
@Entity
@Table(name = "training_sessions")
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** رابط تيمز أو زووم — يضعه المدرّب يدوياً. */
    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status = SessionStatus.SCHEDULED;

    protected TrainingSession() {
    }

    public TrainingSession(Course course, String title, LocalDate sessionDate,
                           LocalTime startTime, LocalTime endTime, String meetingLink) {
        this.course = course;
        this.title = title;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.meetingLink = meetingLink;
        this.status = SessionStatus.SCHEDULED;
    }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
}
