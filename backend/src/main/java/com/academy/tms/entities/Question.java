package com.academy.tms.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * سؤال ضمن اختبار. النوع محفوظ كـ enum ليسمح بإضافة DRAG_DROP لاحقاً
 * دون تغيير المخطّط — تكفي إضافة قيمة جديدة ومعالجة في التصحيح.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private QuestionType type = QuestionType.MCQ;

    @Column(name = "marks", nullable = false)
    private Integer marks;

    @Column(name = "position", nullable = false)
    private Integer position;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<QuestionOption> options = new ArrayList<>();

    protected Question() {
    }

    public Question(Exam exam, String text, QuestionType type, Integer marks, Integer position) {
        this.exam = exam;
        this.text = text;
        this.type = type;
        this.marks = marks;
        this.position = position;
    }

    public Long getId() { return id; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public List<QuestionOption> getOptions() { return options; }
}
