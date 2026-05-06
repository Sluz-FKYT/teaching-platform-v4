package com.opencode.teachingplatform.assessment;

import com.opencode.teachingplatform.assessment.model.QuestionSnapshot;
import com.opencode.teachingplatform.assessment.service.QuestionSnapshotFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionSnapshotFactoryTests {

    @Test
    void shouldBuildStableSnapshotFromBankQuestion() {
        QuestionSnapshot snapshot = QuestionSnapshotFactory.fromBankQuestion(
                12L,
                "SINGLE_CHOICE",
                "Which option is correct?",
                "[{\"key\":\"A\",\"label\":\"Alpha\"}]",
                "{\"correct\":\"A\"}",
                "{\"decisionMode\":\"AUTO_FINAL\"}",
                1,
                10.0
        );

        assertEquals(12L, snapshot.questionBankId());
        assertEquals("BANK", snapshot.sourceType());
        assertEquals("SINGLE_CHOICE", snapshot.questionType());
        assertEquals("Which option is correct?", snapshot.stem());
        assertEquals(1, snapshot.sortOrder());
        assertEquals(10.0, snapshot.score());
        assertEquals("[{\"key\":\"A\",\"label\":\"Alpha\"}]", snapshot.optionsJson());
        assertEquals("{\"correct\":\"A\"}", snapshot.answerJson());
        assertEquals("{\"decisionMode\":\"AUTO_FINAL\"}", snapshot.scoringConfigJson());
        assertTrue(snapshot.reusableFromBank());
    }
}
