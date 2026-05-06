package com.opencode.teachingplatform.grading.strategy;

import com.opencode.teachingplatform.grading.enums.QuestionType;
import com.opencode.teachingplatform.grading.enums.ScoreSource;
import com.opencode.teachingplatform.grading.model.ScoringContext;
import com.opencode.teachingplatform.grading.model.ScoringResult;
import org.springframework.stereotype.Component;

@Component
public class TrueFalseScoringStrategy extends AbstractObjectiveScoringSupport implements QuestionScoringStrategy {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.TRUE_FALSE;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        String expected = normalizeToken(context.scoringConfig().get("correctAnswer"));
        String actual = normalizeToken(context.studentAnswer());
        double score = expected.equals(actual) ? scoreOf(context) : 0D;
        return new ScoringResult(score, null, score, ScoreSource.AUTO,
                score > 0 ? "true/false matched" : "true/false mismatch", false, true);
    }
}
