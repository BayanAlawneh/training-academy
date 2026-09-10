package com.academy.tms.dto;

import com.academy.tms.entities.Question;
import com.academy.tms.entities.QuestionOption;
import com.academy.tms.entities.QuestionType;
import com.academy.tms.services.MatchShuffle;

import java.util.List;

/**
 * السؤال بصيغتين:
 *   forTrainer — يتضمّن الإجابة الصحيحة ومقابل كل طرف.
 *   forTrainee — يحجبهما تماماً.
 *
 * الحجب يتمّ هنا في طبقة الـ DTO لا في الواجهة، لأن أي شيء يُرسَل إلى
 * المتصفح يمكن قراءته من أدوات المطوّر مهما أخفته الواجهة.
 *
 * في سؤال المطابقة تُرسَل الأطراف اليمنى في قائمة منفصلة بفهارس مجهولة
 * وبترتيب مخلوط، فلا يكشف تسلسلها الاقتران الصحيح.
 */
public class QuestionResponse {

    private Long id;
    private String text;
    private String type;
    private Integer marks;
    private Integer position;
    private List<OptionResponse> options;
    /** MATCHING فقط: العمود الأيمن مخلوطاً. null في MCQ. */
    private List<MatchItemResponse> matches;

    public QuestionResponse(Long id, String text, String type, Integer marks,
                            Integer position, List<OptionResponse> options,
                            List<MatchItemResponse> matches) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.marks = marks;
        this.position = position;
        this.options = options;
        this.matches = matches;
    }

    /** للمدرّب: كل شيء ظاهر — الإجابة الصحيحة ومقابل كل طرف. */
    public static QuestionResponse forTrainer(Question q) {
        return new QuestionResponse(q.getId(), q.getText(), q.getType().name(),
                q.getMarks(), q.getPosition(),
                q.getOptions().stream()
                        .map(o -> new OptionResponse(o.getId(), o.getText(),
                                o.isCorrect(), o.getMatchText()))
                        .toList(),
                null);
    }

    /**
     * للمتدرّب: بلا إجابة صحيحة وبلا مقابل.
     * traineeId يدخل في بذرة خلط العمود الأيمن، فيرى كل متدرّب ترتيباً مختلفاً.
     */
    public static QuestionResponse forTrainee(Question q, Long traineeId) {

        List<OptionResponse> options = q.getOptions().stream()
                .map(o -> new OptionResponse(o.getId(), o.getText(), null, null))
                .toList();

        if (q.getType() != QuestionType.MATCHING) {
            return new QuestionResponse(q.getId(), q.getText(), q.getType().name(),
                    q.getMarks(), q.getPosition(), options, null);
        }

        List<QuestionOption> rightOrder =
                MatchShuffle.rightColumnOrder(q.getOptions(), q.getId(), traineeId);

        List<MatchItemResponse> matches = new java.util.ArrayList<>();
        for (int i = 0; i < rightOrder.size(); i++) {
            matches.add(new MatchItemResponse(i, rightOrder.get(i).getMatchText()));
        }

        return new QuestionResponse(q.getId(), q.getText(), q.getType().name(),
                q.getMarks(), q.getPosition(), options, matches);
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getType() { return type; }
    public Integer getMarks() { return marks; }
    public Integer getPosition() { return position; }
    public List<OptionResponse> getOptions() { return options; }
    public List<MatchItemResponse> getMatches() { return matches; }

    public static class OptionResponse {
        private Long id;
        private String text;
        /** null للمتدرّب — لا يُسرَّب المفتاح إطلاقاً. */
        private Boolean correct;
        /** null للمتدرّب — المقابل الصحيح يُرسَل في matches مخلوطاً. */
        private String matchText;

        public OptionResponse(Long id, String text, Boolean correct, String matchText) {
            this.id = id;
            this.text = text;
            this.correct = correct;
            this.matchText = matchText;
        }

        public Long getId() { return id; }
        public String getText() { return text; }
        public Boolean getCorrect() { return correct; }
        public String getMatchText() { return matchText; }
    }
}
