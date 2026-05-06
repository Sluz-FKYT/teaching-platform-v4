package com.opencode.teachingplatform.assessment;

import com.opencode.teachingplatform.assessment.model.GradingResultView;
import com.opencode.teachingplatform.assessment.model.QuestionAnswerPayload;
import com.opencode.teachingplatform.assessment.model.QuestionSnapshot;
import com.opencode.teachingplatform.assessment.service.GradingAdapter;
import com.opencode.teachingplatform.grading.service.ScoringEngine;
import com.opencode.teachingplatform.grading.strategy.FillBlankScoringStrategy;
import com.opencode.teachingplatform.grading.strategy.MultipleChoiceScoringStrategy;
import com.opencode.teachingplatform.grading.strategy.SingleChoiceScoringStrategy;
import com.opencode.teachingplatform.grading.strategy.SubjectiveRecommendationStrategy;
import com.opencode.teachingplatform.grading.strategy.TrueFalseScoringStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradingAdapterTests {

    private final GradingAdapter gradingAdapter = new GradingAdapter(
            new ScoringEngine(List.of(
                    new SingleChoiceScoringStrategy(),
                    new MultipleChoiceScoringStrategy(),
                    new TrueFalseScoringStrategy(),
                    new FillBlankScoringStrategy(),
                    new SubjectiveRecommendationStrategy()
            ))
    );

    @Test
    void shouldMapObjectiveSnapshotAndAnswerPayloadIntoGradingResultView() {
        QuestionSnapshot snapshot = new QuestionSnapshot(
                12L,
                "BANK",
                "TRUE_FALSE",
                "Java is statically typed.",
                1,
                5.0,
                null,
                "{\"correct\":true}",
                null,
                true
        );
        QuestionAnswerPayload payload = new QuestionAnswerPayload(
                21L,
                "TRUE_FALSE",
                null,
                "{\"selectedOptions\":[\"true\"]}",
                List.of("true"),
                null
        );

        GradingResultView result = gradingAdapter.grade(snapshot, payload);

        assertEquals(5.0, result.autoScore());
        assertNull(result.suggestedScore());
        assertEquals(5.0, result.finalScore());
        assertEquals("AUTO", result.scoreSource());
        assertTrue(result.correct());
        assertEquals("{\"selectedOptions\":[\"true\"]}", result.normalizedAnswerJson());
    }

    @Test
    void shouldReturnRecommendationForSubjectiveSnapshotWithoutAutoFinalizing() {
        QuestionSnapshot snapshot = new QuestionSnapshot(
                null,
                "INLINE",
                "SUBJECTIVE",
                "Explain why strategy pattern improves reuse.",
                2,
                10.0,
                null,
                null,
                "{\"keywords\":[{\"term\":\"strategy\",\"weight\":6},{\"term\":\"reuse\",\"weight\":4}],\"minLength\":6}",
                false
        );
        QuestionAnswerPayload payload = new QuestionAnswerPayload(
                22L,
                "SUBJECTIVE",
                "strategy improves reuse across implementations",
                null,
                List.of(),
                null
        );

        GradingResultView result = gradingAdapter.grade(snapshot, payload);

        assertNull(result.autoScore());
        assertEquals(10.0, result.suggestedScore());
        assertNull(result.finalScore());
        assertEquals("RECOMMENDED", result.scoreSource());
        assertNull(result.correct());
        assertNull(result.normalizedAnswerJson());
    }
}
