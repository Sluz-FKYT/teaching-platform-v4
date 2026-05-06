package com.opencode.teachingplatform.assessment.model;

public record GradingResultView(
        Double autoScore,
        Double suggestedScore,
        Double finalScore,
        String judgeDetail,
        String scoreSource,
        Boolean correct,
        String normalizedAnswerJson
) {
}
