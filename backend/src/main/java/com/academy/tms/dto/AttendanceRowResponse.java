package com.academy.tms.dto;

/** صفّ في شاشة تسجيل الحضور عند المدرّب. */
public class AttendanceRowResponse {

    private Long traineeId;
    private String traineeName;
    private String traineeEmail;
    private String status;
    private String note;

    public AttendanceRowResponse(Long traineeId, String traineeName, String traineeEmail,
                                 String status, String note) {
        this.traineeId = traineeId;
        this.traineeName = traineeName;
        this.traineeEmail = traineeEmail;
        this.status = status;
        this.note = note;
    }

    public Long getTraineeId() { return traineeId; }
    public String getTraineeName() { return traineeName; }
    public String getTraineeEmail() { return traineeEmail; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
}
