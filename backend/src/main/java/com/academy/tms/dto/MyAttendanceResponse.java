package com.academy.tms.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** صفّ في صفحة "حضوري" عند المتدرّب: جلسة + حالتي فيها. */
public class MyAttendanceResponse {

    private Long sessionId;
    private Long courseId;
    private String courseTitle;
    private String sessionTitle;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String meetingLink;
    private String sessionStatus;
    /** PRESENT / ABSENT / LATE / EXCUSED، أو NOT_RECORDED إن لم يسجّل المدرّب بعد. */
    private String attendanceStatus;
    private String note;

    public MyAttendanceResponse(Long sessionId, Long courseId, String courseTitle,
                                String sessionTitle, LocalDate sessionDate,
                                LocalTime startTime, LocalTime endTime, String meetingLink,
                                String sessionStatus, String attendanceStatus, String note) {
        this.sessionId = sessionId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.sessionTitle = sessionTitle;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.meetingLink = meetingLink;
        this.sessionStatus = sessionStatus;
        this.attendanceStatus = attendanceStatus;
        this.note = note;
    }

    public Long getSessionId() { return sessionId; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getSessionTitle() { return sessionTitle; }
    public LocalDate getSessionDate() { return sessionDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getMeetingLink() { return meetingLink; }
    public String getSessionStatus() { return sessionStatus; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public String getNote() { return note; }
}
