package com.academy.tms.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** null يعني أن المتدرّب ترك السؤال بلا إجابة. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "awarded_marks", nullable = false)
    private Integer awardedMarks;

    /** أزواج المطابقة. فارغة في MCQ. */
    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerPair> pairs = new ArrayList<>();

    protected Answer() {
    }

    public Answer(Submission submission, Question question,
                  QuestionOption selectedOption, Integer awardedMarks) {
        this.submission = submission;
        this.question = question;
        this.selectedOption = selectedOption;
        this.awardedMarks = awardedMarks;
    }

    public Long getId() { return id; }
    public Submission getSubmission() { return submission; }
    public Question getQuestion() { return question; }
    public QuestionOption getSelectedOption() { return selectedOption; }
    public Integer getAwardedMarks() { return awardedMarks; }
    public void setAwardedMarks(Integer awardedMarks) { this.awardedMarks = awardedMarks; }
    public List<AnswerPair> getPairs() { return pairs; }
}
