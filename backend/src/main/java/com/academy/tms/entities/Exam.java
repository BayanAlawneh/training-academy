package com.academy.tms.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * اختبار ينشئه المدرّب على كورسه.
 *
 * النافذة الزمنية (opensAt / closesAt) تُفرض عند التقديم، ويُحتسب الصفر
 * للمتدرّب الذي لم يقدّم بعد إغلاقها — لكن بلا مهمة مجدولة: تُحسب لحظة
 * عرض العلامات. أبسط، ولا يحتاج cron ولا حالة إضافية في القاعدة.
 */
@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    /** العلامة الكاملة للاختبار — تُوزَّع على الأسئلة بأوزانها. */
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "opens_at", nullable = false)
    private LocalDateTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalDateTime closesAt;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<Question> questions = new ArrayList<>();

    protected Exam() {
    }

    public Exam(Course course, String title, String description, Integer totalMarks,
                LocalDateTime opensAt, LocalDateTime closesAt) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.totalMarks = totalMarks;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.published = false;
    }

    public boolean isOpenNow() {
        LocalDateTime now = LocalDateTime.now();
        return published && !now.isBefore(opensAt) && !now.isAfter(closesAt);
    }

    public boolean hasClosed() {
        return LocalDateTime.now().isAfter(closesAt);
    }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public LocalDateTime getOpensAt() { return opensAt; }
    public void setOpensAt(LocalDateTime opensAt) { this.opensAt = opensAt; }
    public LocalDateTime getClosesAt() { return closesAt; }
    public void setClosesAt(LocalDateTime closesAt) { this.closesAt = closesAt; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public List<Question> getQuestions() { return questions; }
}
