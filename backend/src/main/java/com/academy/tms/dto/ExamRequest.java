package com.academy.tms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public class ExamRequest {

    @NotBlank(message = "Exam title is required")
    @Size(max = 180, message = "Title is too long")
    private String title;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    @NotNull(message = "Opening time is required")
    private LocalDateTime opensAt;

    @NotNull(message = "Closing time is required")
    private LocalDateTime closesAt;

    @Valid
    @NotEmpty(message = "Add at least one question")
    private List<QuestionRequest> questions;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getOpensAt() { return opensAt; }
    public void setOpensAt(LocalDateTime opensAt) { this.opensAt = opensAt; }
    public LocalDateTime getClosesAt() { return closesAt; }
    public void setClosesAt(LocalDateTime closesAt) { this.closesAt = closesAt; }
    public List<QuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<QuestionRequest> questions) { this.questions = questions; }

    public static class QuestionRequest {

        @NotBlank(message = "Question text is required")
        @Size(max = 1000, message = "Question text is too long")
        private String text;

        private String type;

        @NotNull(message = "Marks are required")
        @Min(value = 1, message = "Marks must be at least 1")
        private Integer marks;

        @Valid
        @NotEmpty(message = "Add at least two options")
        private List<OptionRequest> options;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getMarks() { return marks; }
        public void setMarks(Integer marks) { this.marks = marks; }
        public List<OptionRequest> getOptions() { return options; }
        public void setOptions(List<OptionRequest> options) { this.options = options; }
    }

    public static class OptionRequest {

        @NotBlank(message = "Option text is required")
        @Size(max = 500, message = "Option text is too long")
        private String text;

        private boolean correct;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }
}
