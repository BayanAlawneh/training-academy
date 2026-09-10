package com.academy.tms.dto;

/**
 * عنصر في العمود الأيمن كما يُعرض للمتدرّب.
 *
 * لا يحمل معرّف الخيار — بل فهرسه في الترتيب المخلوط فقط، فلا يكشف
 * الاقتران الصحيح لمن يفحص استجابة الشبكة.
 */
public class MatchItemResponse {

    private int index;
    private String text;

    public MatchItemResponse(int index, String text) {
        this.index = index;
        this.text = text;
    }

    public int getIndex() { return index; }
    public String getText() { return text; }
}
