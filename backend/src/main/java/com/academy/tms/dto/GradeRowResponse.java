package com.academy.tms.dto;

import java.time.LocalDateTime;

/** صفّ في جدول علامات المدرّب: متدرّب واحد في اختبار واحد. */
public class GradeRowResponse {

    private Long traineeId;
    private String traineeName;
    private String traineeEmail;
    private Integer score;
    private Integer totalMarks;
    private String state;
    private LocalDateTime submittedAt;

    public GradeRowResponse(Long traineeId, String traineeName, String traineeEmail,
                            Integer score, Integer totalMarks, String state,
                            LocalDateTime submittedAt) {
        this.traineeId = traineeId;
        this.traineeName = traineeName;
        this.traineeEmail = traineeEmail;
        this.score = score;
        this.totalMarks = totalMarks;
        this.state = state;
        this.submittedAt = submittedAt;
    }

    public Long getTraineeId() { return traineeId; }
    public String getTraineeName() { return traineeName; }
    public String getTraineeEmail() { return traineeEmail; }
    public Integer getScore() { return score; }
    public Integer getTotalMarks() { return totalMarks; }
    public String getState() { return state; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
