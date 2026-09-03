package com.academy.tms.dto;

import com.academy.tms.entities.Exam;

import java.time.LocalDateTime;
import java.util.List;

/** الاختبار كما يراه المدرّب — يتضمّن الإجابات الصحيحة. */
public class ExamResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Integer totalMarks;
    private LocalDateTime opensAt;
    private LocalDateTime closesAt;
    private boolean published;
    private int questionCount;
    private long submissionCount;
    private List<QuestionResponse> questions;

    public ExamResponse(Long id, Long courseId, String courseTitle, String title, String description,
                        Integer totalMarks, LocalDateTime opensAt, LocalDateTime closesAt,
                        boolean published, int questionCount, long submissionCount,
                        List<QuestionResponse> questions) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.title = title;
        this.description = description;
        this.totalMarks = totalMarks;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.published = published;
        this.questionCount = questionCount;
        this.submissionCount = submissionCount;
        this.questions = questions;
    }

    public static ExamResponse summary(Exam exam, int questionCount, long submissionCount) {
        return new ExamResponse(
                exam.getId(), exam.getCourse().getId(), exam.getCourse().getTitle(),
                exam.getTitle(), exam.getDescription(), exam.getTotalMarks(),
                exam.getOpensAt(), exam.getClosesAt(), exam.isPublished(),
                questionCount, submissionCount, null);
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getTotalMarks() { return totalMarks; }
    public LocalDateTime getOpensAt() { return opensAt; }
    public LocalDateTime getClosesAt() { return closesAt; }
    public boolean isPublished() { return published; }
    public int getQuestionCount() { return questionCount; }
    public long getSubmissionCount() { return submissionCount; }
    public List<QuestionResponse> getQuestions() { return questions; }
}
