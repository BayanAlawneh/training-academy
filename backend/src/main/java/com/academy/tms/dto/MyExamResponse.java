package com.academy.tms.dto;

import java.time.LocalDateTime;

/**
 * الاختبار كما يراه المتدرّب في صفحة "اختباراتي".
 *
 * state يلخّص كل شيء للواجهة:
 *   UPCOMING  — لم تفتح النافذة بعد
 *   OPEN      — يمكن التقديم الآن
 *   SUBMITTED — قدّم بالفعل، وscore متاح
 *   MISSED    — أُغلقت النافذة ولم يقدّم ← صفر
 */
public class MyExamResponse {

    private Long examId;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Integer totalMarks;
    private LocalDateTime opensAt;
    private LocalDateTime closesAt;
    private int questionCount;
    private String state;
    private Integer score;
    private LocalDateTime submittedAt;

    public MyExamResponse(Long examId, Long courseId, String courseTitle, String title,
                          String description, Integer totalMarks, LocalDateTime opensAt,
                          LocalDateTime closesAt, int questionCount, String state,
                          Integer score, LocalDateTime submittedAt) {
        this.examId = examId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.title = title;
        this.description = description;
        this.totalMarks = totalMarks;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.questionCount = questionCount;
        this.state = state;
        this.score = score;
        this.submittedAt = submittedAt;
    }

    public Long getExamId() { return examId; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getTotalMarks() { return totalMarks; }
    public LocalDateTime getOpensAt() { return opensAt; }
    public LocalDateTime getClosesAt() { return closesAt; }
    public int getQuestionCount() { return questionCount; }
    public String getState() { return state; }
    public Integer getScore() { return score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
