package com.academy.tms.dto;

/** نتيجة التصحيح الفوري بعد التسليم. */
public class SubmissionResultResponse {

    private Long examId;
    private String examTitle;
    private Integer score;
    private Integer totalMarks;
    private int correctCount;
    private int questionCount;

    public SubmissionResultResponse(Long examId, String examTitle, Integer score,
                                    Integer totalMarks, int correctCount, int questionCount) {
        this.examId = examId;
        this.examTitle = examTitle;
        this.score = score;
        this.totalMarks = totalMarks;
        this.correctCount = correctCount;
        this.questionCount = questionCount;
    }

    public Long getExamId() { return examId; }
    public String getExamTitle() { return examTitle; }
    public Integer getScore() { return score; }
    public Integer getTotalMarks() { return totalMarks; }
    public int getCorrectCount() { return correctCount; }
    public int getQuestionCount() { return questionCount; }
}
