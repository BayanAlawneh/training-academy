package com.academy.tms.entities;

import jakarta.persistence.*;

/**
 * زوج مطابقة واحد اختاره المتدرّب.
 *
 * option        = العنصر الأيسر المعروض عليه.
 * matchedOption = الخيار الذي يملك النص الأيمن الذي سحبه إليه.
 *
 * الزوج صحيح حين يتساوى المعرّفان: أي أنه وصل الطرف الأيسر لخيار
 * بالطرف الأيمن للخيار نفسه. هذا يجعل التصحيح مقارنة معرّفين لا نصوص.
 *
 * matchedOption يكون null إن ترك الزوج بلا وصل.
 */
@Entity
@Table(
        name = "answer_pairs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pairs_answer_option",
                columnNames = {"answer_id", "option_id"})
)
public class AnswerPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private QuestionOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_option_id")
    private QuestionOption matchedOption;

    protected AnswerPair() {
    }

    public AnswerPair(Answer answer, QuestionOption option, QuestionOption matchedOption) {
        this.answer = answer;
        this.option = option;
        this.matchedOption = matchedOption;
    }

    public boolean isCorrect() {
        return matchedOption != null && matchedOption.getId().equals(option.getId());
    }

    public Long getId() { return id; }
    public Answer getAnswer() { return answer; }
    public QuestionOption getOption() { return option; }
    public QuestionOption getMatchedOption() { return matchedOption; }
}
