package com.academy.tms.entities;

public enum QuestionType {
    MCQ,
    TRUE_FALSE,
    /** مطابقة: كل خيار يحمل عنصره الأيسر ومقابله الصحيح في match_text. */
    MATCHING
}
