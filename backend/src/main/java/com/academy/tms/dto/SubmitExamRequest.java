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
        /** MCQ: null إذا ترك السؤال بلا إجابة. */
        private Long selectedOptionId;
        /** MATCHING: الأزواج التي وصلها المتدرّب. */
        private List<PairEntry> pairs;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
        public List<PairEntry> getPairs() { return pairs; }
        public void setPairs(List<PairEntry> pairs) { this.pairs = pairs; }
    }

    /**
     * زوج مطابقة: معرّف العنصر الأيسر، وفهرس العنصر الأيمن في الترتيب
     * المخلوط الذي عُرض على هذا المتدرّب. الفهرس لا يكشف الاقتران،
     * والخادم يعيد حساب الترتيب نفسه ليحوّله إلى خيار.
     */
    public static class PairEntry {
        private Long optionId;
        /** null إذا ترك الطرف بلا وصل. */
        private Integer matchIndex;

        public Long getOptionId() { return optionId; }
        public void setOptionId(Long optionId) { this.optionId = optionId; }
        public Integer getMatchIndex() { return matchIndex; }
        public void setMatchIndex(Integer matchIndex) { this.matchIndex = matchIndex; }
    }
}
