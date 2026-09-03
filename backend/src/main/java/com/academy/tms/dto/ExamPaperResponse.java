package com.academy.tms.dto;

import java.util.List;

/** ورقة الاختبار المسلَّمة للمتدرّب لحظة البدء — بلا إجابات صحيحة. */
public class ExamPaperResponse {

    private Long examId;
    private String title;
    private String description;
    private Integer totalMarks;
    private String closesAt;
    private List<QuestionResponse> questions;

    public ExamPaperResponse(Long examId, String title, String description,
                             Integer totalMarks, String closesAt,
                             List<QuestionResponse> questions) {
        this.examId = examId;
        this.title = title;
        this.description = description;
        this.totalMarks = totalMarks;
        this.closesAt = closesAt;
        this.questions = questions;
    }

    public Long getExamId() { return examId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getTotalMarks() { return totalMarks; }
    public String getClosesAt() { return closesAt; }
    public List<QuestionResponse> getQuestions() { return questions; }
}
