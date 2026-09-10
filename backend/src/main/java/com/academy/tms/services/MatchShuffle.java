package com.academy.tms.services;

import com.academy.tms.entities.QuestionOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * ترتيب العمود الأيمن في سؤال المطابقة.
 *
 * المشكلة: لو أُرسل كل عنصر أيمن مع معرّف خياره، لأمكن للمتدرّب فتح
 * تبويب الشبكة ومطابقة المعرّفات فيحصل على الحل كاملاً. ولو أُرسلت
 * الأطراف مخلوطة بترتيب عشوائي لحظي، لما استطاع الخادم — وهو بلا حالة —
 * أن يعرف أي ترتيب أرسله عند وصول التسليم.
 *
 * الحل: خلط حتمي ببذرة مشتقّة من رقم السؤال ورقم المتدرّب. الخادم يعيد
 * حساب الترتيب نفسه في أي لحظة دون تخزين شيء، فتُرسل الأطراف بفهارس
 * مجهولة لا تكشف الاقتران، ويظلّ التصحيح ممكناً. ولأن البذرة تشمل رقم
 * المتدرّب، يرى كل متدرّب ترتيباً مختلفاً.
 */
public final class MatchShuffle {

    private MatchShuffle() {
    }

    /**
     * يرجع الخيارات مرتّبة بترتيب العمود الأيمن كما يراه هذا المتدرّب.
     * الفهرس في القائمة الراجعة هو ما يُرسله المتدرّب عند التسليم.
     */
    public static List<QuestionOption> rightColumnOrder(List<QuestionOption> options,
                                                        Long questionId, Long traineeId) {
        List<QuestionOption> ordered = new ArrayList<>(options);

        // ترتيب أوّلي ثابت: بدون هذا يتغيّر الناتج بتغيّر ترتيب الجلب من القاعدة
        ordered.sort((a, b) -> Integer.compare(
                a.getPosition() == null ? 0 : a.getPosition(),
                b.getPosition() == null ? 0 : b.getPosition()));

        long seed = 31L * questionId + 17L * traineeId;
        Collections.shuffle(ordered, new Random(seed));

        return ordered;
    }
}
