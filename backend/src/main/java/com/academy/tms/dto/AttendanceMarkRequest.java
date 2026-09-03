package com.academy.tms.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** تسجيل حضور جلسة كاملة دفعة واحدة. */
public class AttendanceMarkRequest {

    @NotNull(message = "Entries are required")
    private List<Entry> entries;

    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }

    public static class Entry {
        private Long traineeId;
        private String status;
        private String note;

        public Long getTraineeId() { return traineeId; }
        public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}
