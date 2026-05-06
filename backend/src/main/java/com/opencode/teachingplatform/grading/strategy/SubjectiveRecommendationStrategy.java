package com.opencode.teachingplatform.grading.strategy;

import com.opencode.teachingplatform.grading.enums.QuestionType;
import com.opencode.teachingplatform.grading.enums.ScoreSource;
import com.opencode.teachingplatform.grading.model.ScoringContext;
import com.opencode.teachingplatform.grading.model.ScoringResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SubjectiveRecommendationStrategy extends AbstractObjectiveScoringSupport implements QuestionScoringStrategy {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.SUBJECTIVE;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        String text = normalizeText(context.studentAnswer());
        List<Map<String, Object>> keywords = castKeywordConfigs(context.scoringConfig().get("keywords"));
        int minLength = asInt(context.scoringConfig().getOrDefault("minLength", 0));
        double suggested = 0D;
        List<String> matched = new ArrayList<>();
        for (Map<String, Object> keyword : keywords) {
            String term = normalizeToken(keyword.get("term"));
            int weight = asInt(keyword.getOrDefault("weight", 0));
            if (!term.isBlank() && text.contains(term)) {
                suggested += weight;
                matched.add(term);
            }
        }
        if (minLength > 0 && text.length() < minLength) {
            suggested = Math.min(suggested, scoreOf(context) / 2D);
        }
        suggested = round(Math.min(suggested, scoreOf(context)));
        String detail = "matched=" + matched + "; length=" + text.length() + "/" + minLength;
        Double finalScore = context.autoGradingEnabled() ? suggested : null;
        ScoreSource source = context.autoGradingEnabled() ? ScoreSource.AUTO_ACCEPTED : ScoreSource.RECOMMENDED;
        return new ScoringResult(null, suggested, finalScore, source, detail, !context.autoGradingEnabled(), true);
    }
}
