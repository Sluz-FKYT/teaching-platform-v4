package com.opencode.teachingplatform.grading;

import com.opencode.teachingplatform.grading.enums.QuestionType;
import com.opencode.teachingplatform.grading.enums.ScoreSource;
import com.opencode.teachingplatform.grading.model.ScoringContext;
import com.opencode.teachingplatform.grading.model.ScoringResult;
import com.opencode.teachingplatform.grading.service.ScoringEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ScoringEngineTests {

    @Autowired
    private ScoringEngine scoringEngine;

    @Test
    void multipleChoiceShouldSupportAveragePartialScore() {
        ScoringContext context = TestScoringContexts.multipleChoiceAveragePartial();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.autoScore()).isEqualTo(5.0);
        assertThat(result.finalScore()).isEqualTo(5.0);
        assertThat(result.scoreSource()).isEqualTo(ScoreSource.AUTO);
        assertThat(result.needsTeacherReview()).isFalse();
        assertThat(result.canAutoFinalize()).isTrue();
    }

    @Test
    void fillBlankShouldSupportPerBlankScore() {
        ScoringContext context = TestScoringContexts.fillBlankPerSlot();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.autoScore()).isEqualTo(6.0);
        assertThat(result.finalScore()).isEqualTo(6.0);
        assertThat(result.scoreSource()).isEqualTo(ScoreSource.AUTO);
    }

    @Test
    void fillBlankShouldAcceptExperimentOverrideAnswers() {
        ScoringContext context = TestScoringContexts.fillBlankWithOverrideAnswers();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.autoScore()).isEqualTo(10.0);
        assertThat(result.finalScore()).isEqualTo(10.0);
        assertThat(result.scoreSource()).isEqualTo(ScoreSource.AUTO);
        assertThat(result.judgeDetail()).contains("blank 1: matched");
    }

    @Test
    void fillBlankShouldNotBroadcastItemLevelAcceptedAnswerToAllBlankSlots() {
        ScoringContext context = new ScoringContext(
                QuestionType.FILL_BLANK,
                10.0,
                Map.of(
                        "blanks", List.of(
                                Map.of("answers", List.of("spring"), "score", 4.0),
                                Map.of("answers", List.of("boot"), "score", 6.0)
                        ),
                        "ignoreCase", true
                ),
                List.of("cloud", "cloud"),
                false,
                Map.of("blank-1", List.of("cloud"))
        );

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.autoScore()).isEqualTo(4.0);
        assertThat(result.finalScore()).isEqualTo(4.0);
        assertThat(result.judgeDetail()).contains("blank 1: matched");
        assertThat(result.judgeDetail()).contains("blank 2: mismatch");
    }

    @Test
    void subjectiveShouldProduceRecommendationNotTeacherFinalByDefault() {
        ScoringContext context = TestScoringContexts.subjectiveRecommendationOnly();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.recommendedScore()).isEqualTo(8.0);
        assertThat(result.finalScore()).isNull();
        assertThat(result.needsTeacherReview()).isTrue();
        assertThat(result.scoreSource()).isEqualTo(ScoreSource.RECOMMENDED);
    }

    static final class TestScoringContexts {
        private TestScoringContexts() {
        }

        static ScoringContext multipleChoiceAveragePartial() {
            return new ScoringContext(
                    QuestionType.MULTIPLE_CHOICE,
                    10.0,
                    Map.of(
                            "mode", "AVERAGE_PARTIAL",
                            "correctAnswers", List.of("A", "B", "C", "D"),
                            "wrongOptionPenalty", 0.0
                    ),
                    List.of("A", "B"),
                    false,
                    Map.of()
            );
        }

        static ScoringContext fillBlankPerSlot() {
            return new ScoringContext(
                    QuestionType.FILL_BLANK,
                    10.0,
                    Map.of(
                            "blanks", List.of(
                                    Map.of("answers", List.of("spring"), "score", 4.0),
                                    Map.of("answers", List.of("boot"), "score", 6.0)
                            ),
                            "ignoreCase", true
                    ),
                    List.of("Cloud", "boot"),
                    false,
                    Map.of()
            );
        }

        static ScoringContext subjectiveRecommendationOnly() {
            return new ScoringContext(
                    QuestionType.SUBJECTIVE,
                    10.0,
                    Map.of(
                            "keywords", List.of(
                                    Map.of("term", "strategy", "weight", 3),
                                    Map.of("term", "扩展", "weight", 3),
                                    Map.of("term", "复用", "weight", 2)
                            ),
                            "minLength", 10
                    ),
                    "该设计可提升 strategy 扩展与复用能力",
                    false,
                    Map.of()
            );
        }

        static ScoringContext fillBlankWithOverrideAnswers() {
            return new ScoringContext(
                    QuestionType.FILL_BLANK,
                    10.0,
                    Map.of(
                            "blanks", List.of(
                                    Map.of("answers", List.of("spring"), "score", 4.0),
                                    Map.of("answers", List.of("boot"), "score", 6.0)
                            ),
                            "ignoreCase", true
                    ),
                    List.of("Cloud", "boot"),
                    false,
                    Map.of("blank-1", List.of("cloud"))
            );
        }
    }
}
