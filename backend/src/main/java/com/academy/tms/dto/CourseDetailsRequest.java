package com.academy.tms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** يعدّله المدرّب المستلم للكورس — الوصف والمدّة فقط، لا السعة ولا المدرّب. */
public class CourseDetailsRequest {

    @Size(max = 1500, message = "Description is too long")
    private String description;

    @Min(value = 1, message = "Duration must be at least 1 week")
    private Integer durationWeeks;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
}
