package com.academy.tms.entities;

import jakarta.persistence.*;

/**
 * حضور متدرّب في جلسة. القيد الفريد يمنع تسجيل نفس المتدرّب مرّتين
 * في نفس الجلسة — الحماية على مستوى قاعدة البيانات لا الكود وحده.
 */
@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_session_trainee",
                columnNames = {"session_id", "trainee_id"})
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "note", length = 300)
    private String note;

    protected Attendance() {
    }

    public Attendance(TrainingSession session, Trainee trainee, AttendanceStatus status, String note) {
        this.session = session;
        this.trainee = trainee;
        this.status = status;
        this.note = note;
    }

    public Long getId() { return id; }
    public TrainingSession getSession() { return session; }
    public void setSession(TrainingSession session) { this.session = session; }
    public Trainee getTrainee() { return trainee; }
    public void setTrainee(Trainee trainee) { this.trainee = trainee; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
