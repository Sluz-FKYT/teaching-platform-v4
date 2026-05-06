package com.opencode.teachingplatform.assessment.model;

import java.util.List;

public record QuestionAnswerPayload(
        Long questionRefId,
        String questionType,
        String answerText,
        String answerJson,
        List<String> selectedOptions,
        String attachmentPath
) {
    public QuestionAnswerPayload {
        selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
    }
}
