package com.academy.tms.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    /** لا يُرسَل أبداً إلى المتدرّب قبل التسليم — انظر QuestionResponse. */
    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "position", nullable = false)
    private Integer position;

    protected QuestionOption() {
    }

    public QuestionOption(Question question, String text, boolean correct, Integer position) {
        this.question = question;
        this.text = text;
        this.correct = correct;
        this.position = position;
    }

    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
