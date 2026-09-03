package com.academy.tms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * توليد جدول لقاءات دفعة واحدة.
 * مثال: ابدأ 2026-09-10، 4 أسابيع، أيام الأحد والثلاثاء، 10:00–12:00
 * ← ينشئ 8 جلسات بحلقة بسيطة، بلا مكتبات جدولة ولا cron.
 */
public class ScheduleRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Min(value = 1, message = "Weeks must be at least 1")
    @Max(value = 26, message = "Weeks cannot exceed 26")
    private int weeks;

    /** 1 = الاثنين … 7 = الأحد (ISO-8601، مطابق لـ DayOfWeek.getValue()) */
    @NotEmpty(message = "Select at least one weekday")
    private List<Integer> weekdays;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String titlePrefix;

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public int getWeeks() { return weeks; }
    public void setWeeks(int weeks) { this.weeks = weeks; }
    public List<Integer> getWeekdays() { return weekdays; }
    public void setWeekdays(List<Integer> weekdays) { this.weekdays = weekdays; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getTitlePrefix() { return titlePrefix; }
    public void setTitlePrefix(String titlePrefix) { this.titlePrefix = titlePrefix; }
}
