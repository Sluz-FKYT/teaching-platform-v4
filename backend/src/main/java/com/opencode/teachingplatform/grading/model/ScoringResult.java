package com.opencode.teachingplatform.grading.model;

import com.opencode.teachingplatform.grading.enums.ScoreSource;

public record ScoringResult(
        Double autoScore,
        Double recommendedScore,
        Double finalScore,
        ScoreSource scoreSource,
        String judgeDetail,
        boolean needsTeacherReview,
        boolean canAutoFinalize
) {
}
