package com.academy.tms.dto;

import com.academy.tms.entities.Question;

import java.util.List;

/**
 * السؤال بصيغتين:
 *   forTrainer — يتضمّن أي الخيارات صحيح.
 *   forTrainee — يحجب الإجابة الصحيحة تماماً.
 *
 * الحجب يتمّ هنا في طبقة الـ DTO لا في الواجهة، لأن أي شيء يُرسَل إلى
 * المتصفح يمكن قراءته من أدوات المطوّر مهما أخفته الواجهة.
 */
public class QuestionResponse {

    private Long id;
    private String text;
    private String type;
    private Integer marks;
    private Integer position;
    private List<OptionResponse> options;

    public QuestionResponse(Long id, String text, String type, Integer marks,
                            Integer position, List<OptionResponse> options) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.marks = marks;
        this.position = position;
        this.options = options;
    }

    public static QuestionResponse forTrainer(Question q) {
        return new QuestionResponse(q.getId(), q.getText(), q.getType().name(),
                q.getMarks(), q.getPosition(),
                q.getOptions().stream()
                        .map(o -> new OptionResponse(o.getId(), o.getText(), o.isCorrect()))
                        .toList());
    }

    public static QuestionResponse forTrainee(Question q) {
        return new QuestionResponse(q.getId(), q.getText(), q.getType().name(),
                q.getMarks(), q.getPosition(),
                q.getOptions().stream()
                        .map(o -> new OptionResponse(o.getId(), o.getText(), null))
                        .toList());
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getType() { return type; }
    public Integer getMarks() { return marks; }
    public Integer getPosition() { return position; }
    public List<OptionResponse> getOptions() { return options; }

    public static class OptionResponse {
        private Long id;
        private String text;
        /** null للمتدرّب — لا يُسرَّب المفتاح إطلاقاً. */
        private Boolean correct;

        public OptionResponse(Long id, String text, Boolean correct) {
            this.id = id;
            this.text = text;
            this.correct = correct;
        }

        public Long getId() { return id; }
        public String getText() { return text; }
        public Boolean getCorrect() { return correct; }
    }
}
