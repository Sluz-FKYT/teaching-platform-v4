package com.opencode.teachingplatform.grading.model;

import com.opencode.teachingplatform.grading.enums.QuestionType;

import java.util.List;
import java.util.Map;

public record ScoringContext(
        QuestionType questionType,
        Double questionScore,
        Map<String, Object> scoringConfig,
        Object studentAnswer,
        boolean autoGradingEnabled,
        Map<String, List<String>> extraAcceptedAnswers
) {
    public ScoringContext(QuestionType questionType,
                          Double questionScore,
                          Map<String, Object> scoringConfig,
                          Object studentAnswer,
                          boolean autoGradingEnabled) {
        this(questionType, questionScore, scoringConfig, studentAnswer, autoGradingEnabled, Map.of());
    }

    public ScoringContext {
        extraAcceptedAnswers = extraAcceptedAnswers == null ? Map.of() : Map.copyOf(extraAcceptedAnswers);
    }
}
