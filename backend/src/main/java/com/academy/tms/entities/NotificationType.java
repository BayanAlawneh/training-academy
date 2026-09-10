package com.academy.tms.entities;

public enum NotificationType {
    // للمتدرّب
    EXAM_PUBLISHED,
    SESSION_SCHEDULED,
    ATTENDANCE_RECORDED,
    ENROLLED,

    // للمدرّب
    EXAM_SUBMITTED,
    TRAINEE_ENROLLED,
    TRAINEE_REMOVED,
    COURSE_ASSIGNED,

    GENERAL
}
