package com.opencode.teachingplatform.grading.strategy;

import com.opencode.teachingplatform.grading.model.ScoringContext;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class AbstractObjectiveScoringSupport {

    protected double scoreOf(ScoringContext context) {
        return context.questionScore() == null ? 0D : context.questionScore();
    }

    protected String normalizeToken(Object value) {
        return value == null ? "" : String.valueOf(value).trim().toLowerCase(Locale.ROOT);
    }

    protected String normalizeText(Object value) {
        return normalizeToken(value).replace('，', ',');
    }

    protected Set<String> normalizeSet(Object value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                String normalized = normalizeToken(item);
                if (!normalized.isBlank()) {
                    result.add(normalized);
                }
            }
            return result;
        }
        String normalized = normalizeToken(value);
        if (!normalized.isBlank()) {
            result.add(normalized);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> castKeywordConfigs(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> castBlankConfigs(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    protected List<String> castAnswerList(Object value) {
        return value instanceof List<?> list ? (List<String>) list : List.of();
    }

    protected int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return 0;
    }

    protected double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text);
        }
        return 0D;
    }

    protected double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
