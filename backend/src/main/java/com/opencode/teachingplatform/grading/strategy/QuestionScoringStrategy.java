package com.opencode.teachingplatform.grading.strategy;

import com.opencode.teachingplatform.grading.enums.QuestionType;
import com.opencode.teachingplatform.grading.model.ScoringContext;
import com.opencode.teachingplatform.grading.model.ScoringResult;

/**
 * 评分策略接口。
 *
 * <p>这是评分模块最核心的扩展点：每种题型只要实现 supports + evaluate，
 * 就能被 {@code ScoringEngine} 自动纳入统一评分链路中。</p>
 */
public interface QuestionScoringStrategy {
    boolean supports(QuestionType type);

    ScoringResult evaluate(ScoringContext context);
}
