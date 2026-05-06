package com.opencode.teachingplatform.grading.strategy;

import com.opencode.teachingplatform.grading.enums.QuestionType;
import com.opencode.teachingplatform.grading.enums.ScoreSource;
import com.opencode.teachingplatform.grading.model.ScoringContext;
import com.opencode.teachingplatform.grading.model.ScoringResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FillBlankScoringStrategy extends AbstractObjectiveScoringSupport implements QuestionScoringStrategy {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.FILL_BLANK;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        List<Map<String, Object>> blanks = castBlankConfigs(context.scoringConfig().get("blanks"));
        List<String> answers = castAnswerList(context.studentAnswer());
        double total = 0D;
        List<String> details = new ArrayList<>();
        for (int i = 0; i < blanks.size(); i++) {
            Map<String, Object> blank = blanks.get(i);
            String actual = i < answers.size() ? normalizeToken(answers.get(i)) : "";
            Set<String> expected = normalizeSet(blank.get("answers"));
            expected.addAll(normalizeSet(context.extraAcceptedAnswers().get("blank-" + (i + 1))));
            double blankScore = asDouble(blank.get("score"));
            boolean matched = expected.contains(actual);
            if (matched) {
                total += blankScore;
            }
            details.add("blank " + (i + 1) + ": " + (matched ? "matched" : "mismatch"));
        }
        total = round(Math.min(total, scoreOf(context)));
        return new ScoringResult(total, null, total, ScoreSource.AUTO, String.join("; ", details), false, true);
    }
}
