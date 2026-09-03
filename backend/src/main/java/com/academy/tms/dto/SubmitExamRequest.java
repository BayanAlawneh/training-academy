package com.academy.tms.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SubmitExamRequest {

    @NotNull(message = "Answers are required")
    private List<AnswerEntry> answers;

    public List<AnswerEntry> getAnswers() { return answers; }
    public void setAnswers(List<AnswerEntry> answers) { this.answers = answers; }

    public static class AnswerEntry {
        private Long questionId;
        /** null إذا ترك السؤال بلا إجابة. */
        private Long selectedOptionId;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    }
}
