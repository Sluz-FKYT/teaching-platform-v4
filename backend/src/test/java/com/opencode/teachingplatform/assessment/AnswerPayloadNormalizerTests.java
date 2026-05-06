package com.opencode.teachingplatform.assessment;

import com.opencode.teachingplatform.assessment.model.QuestionAnswerPayload;
import com.opencode.teachingplatform.assessment.service.AnswerPayloadNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AnswerPayloadNormalizerTests {

    @Test
    void shouldNormalizeBlankFieldsAndSelectedOptions() {
        QuestionAnswerPayload payload = AnswerPayloadNormalizer.normalize(
                21L,
                "MULTIPLE_CHOICE",
                "   ",
                null,
                List.of(" A ", "", "B", "A"),
                " /uploads/answer.txt "
        );

        assertEquals(21L, payload.questionRefId());
        assertEquals("MULTIPLE_CHOICE", payload.questionType());
        assertNull(payload.answerText());
        assertNull(payload.answerJson());
        assertIterableEquals(List.of("A", "B"), payload.selectedOptions());
        assertEquals("/uploads/answer.txt", payload.attachmentPath());
    }
}
