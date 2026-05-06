package com.opencode.teachingplatform.assessment.service;

import com.opencode.teachingplatform.assessment.model.QuestionAnswerPayload;

import java.util.LinkedHashSet;
import java.util.List;

public final class AnswerPayloadNormalizer {

    private AnswerPayloadNormalizer() {
    }

    public static QuestionAnswerPayload normalize(Long questionRefId,
                                                  String questionType,
                                                  String answerText,
                                                  String answerJson,
                                                  List<String> selectedOptions,
                                                  String attachmentPath) {
        return new QuestionAnswerPayload(
                questionRefId,
                questionType,
                normalizeText(answerText),
                normalizeText(answerJson),
                normalizeSelectedOptions(selectedOptions),
                normalizeText(attachmentPath)
        );
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<String> normalizeSelectedOptions(List<String> selectedOptions) {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String option : selectedOptions) {
            String trimmed = normalizeText(option);
            if (trimmed != null) {
                normalized.add(trimmed);
            }
        }
        return List.copyOf(normalized);
    }
}
