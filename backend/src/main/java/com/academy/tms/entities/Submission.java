package com.academy.tms.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * محاولة متدرّب في اختبار. القيد الفريد يمنع التقديم مرّتين على مستوى
 * قاعدة البيانات — لا يكفي فحص في الكود مع طلبين متزامنين.
 */
@Entity
@Table(
        name = "submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_submissions_exam_trainee",
                columnNames = {"exam_id", "trainee_id"})
)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    protected Submission() {
    }

    public Submission(Exam exam, Trainee trainee, Integer score, LocalDateTime submittedAt) {
        this.exam = exam;
        this.trainee = trainee;
        this.score = score;
        this.submittedAt = submittedAt;
    }

    public Long getId() { return id; }
    public Exam getExam() { return exam; }
    public Trainee getTrainee() { return trainee; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public List<Answer> getAnswers() { return answers; }
}
