package com.academy.tms.dto;

import java.time.LocalDate;

/** ملخّص لوحة المتدرّب — أرقام سريعة بدون تحميل كل التفاصيل. */
public class MySummaryResponse {

    private String name;
    private String email;
    private LocalDate memberSince;
    private long courseCount;

    public MySummaryResponse(String name, String email, LocalDate memberSince, long courseCount) {
        this.name = name;
        this.email = email;
        this.memberSince = memberSince;
        this.courseCount = courseCount;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDate getMemberSince() { return memberSince; }
    public long getCourseCount() { return courseCount; }
}
