package com.opencode.teachingplatform.assessment.model;

public record QuestionSnapshot(
        Long questionBankId,
        String sourceType,
        String questionType,
        String stem,
        Integer sortOrder,
        Double score,
        String optionsJson,
        String answerJson,
        String scoringConfigJson,
        Boolean reusableFromBank
) {
}
