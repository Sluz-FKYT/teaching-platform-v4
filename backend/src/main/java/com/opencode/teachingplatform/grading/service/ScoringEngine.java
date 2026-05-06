package com.opencode.teachingplatform.grading.service;

import com.opencode.teachingplatform.grading.model.ScoringContext;
import com.opencode.teachingplatform.grading.model.ScoringResult;
import com.opencode.teachingplatform.grading.strategy.QuestionScoringStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * 通用评分引擎。
 *
 * <p>这里实现的是典型的策略分发：Spring 会把所有 {@link QuestionScoringStrategy}
 * 实现注入进来，运行时根据题型选择第一个支持该题型的策略来计算分数。</p>
 */
public class ScoringEngine {

    private final List<QuestionScoringStrategy> strategies;

    public ScoringEngine(List<QuestionScoringStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 根据题型选择合适的评分策略并执行。
     */
    public ScoringResult evaluate(ScoringContext context) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(context.questionType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported question type: " + context.questionType()))
                .evaluate(context);
    }
}
