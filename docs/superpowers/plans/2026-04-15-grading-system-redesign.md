# Grading System Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reusable grading core for homework, exam, and lab while upgrading homework to question-based submissions, adding strategy-based scoring, recommendation flows, and score visibility controls.

**Architecture:** Keep `lab` / `homework` / `exam` as separate business modules inside the modular monolith, and introduce a shared `grading` capability module used for single-question scoring only. Extend `question` as the unified question bank for homework and exam, keep `lab_step` independent, and preserve `score_record` as the final aggregated score fact table.

**Tech Stack:** Spring Boot 3, Spring Data JPA, Flyway, H2/MySQL, Vue 3, Vite, Pinia, Vue Router, Element Plus, Playwright CLI, JUnit/Spring tests.

---

## File Structure

### Backend new or heavily changed files

- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/enums/QuestionType.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/enums/ScoreSource.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/enums/ScoringDecisionMode.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/model/ScoringContext.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/model/ScoringResult.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/QuestionScoringStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/SingleChoiceScoringStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/MultipleChoiceScoringStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/TrueFalseScoringStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/FillBlankScoringStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/SubjectiveRecommendationStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/service/ScoringEngine.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/service/ScoreRecordWriter.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/question/entity/QuestionAttachment.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/HomeworkQuestion.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/HomeworkAnswer.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/common/enums/ScoreVisibilityMode.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/question/entity/QuestionBank.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamQuestion.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamAnswer.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamSubmission.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/Homework.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/HomeworkSubmission.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/entity/LabStep.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/entity/LabStepAnswer.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/question/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/analysis/*`
- Create: `backend/src/main/resources/db/migration/Vxxx__grading_redesign.sql`

### Backend test files

- Create: `backend/src/test/java/com/opencode/teachingplatform/grading/ScoringEngineTests.java`
- Create: `backend/src/test/java/com/opencode/teachingplatform/homework/HomeworkQuestionFlowTests.java`
- Modify: `backend/src/test/java/com/opencode/teachingplatform/exam/ExamServiceTests.java`
- Modify: `backend/src/test/java/com/opencode/teachingplatform/lab/*`
- Create: `backend/src/test/java/com/opencode/teachingplatform/analysis/ScoreVisibilityTests.java`

### Frontend new or heavily changed files

- Create: `frontend/src/api/questions.ts`
- Modify: `frontend/src/api/homeworks.ts`
- Modify: `frontend/src/api/exams.ts`
- Modify: `frontend/src/api/teacher.ts`
- Create: `frontend/src/components/questions/QuestionSelector.vue`
- Create: `frontend/src/components/questions/QuestionEditor.vue`
- Create: `frontend/src/components/questions/ScoringConfigEditor.vue`
- Create: `frontend/src/components/grading/AnswerReviewCard.vue`
- Create: `frontend/src/components/grading/SubmissionGradingPanel.vue`
- Create: `frontend/src/components/grading/ScoreVisibilitySettings.vue`
- Create: `frontend/src/components/answers/SubjectiveAnswerEditor.vue`
- Create: `frontend/src/components/answers/AttachmentUploader.vue`
- Modify: `frontend/src/views/teacher/homeworks/*`
- Modify: `frontend/src/views/teacher/exams/*`
- Modify: `frontend/src/views/student/homeworks/*`
- Modify: `frontend/src/views/student/exams/*`
- Modify: `frontend/src/types/*`

---

### Task 1: Add Core Database Structures And Enums

**Files:**
- Create: `backend/src/main/resources/db/migration/Vxxx__grading_redesign.sql`
- Create: `backend/src/main/java/com/opencode/teachingplatform/common/enums/ScoreVisibilityMode.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/question/entity/QuestionAttachment.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/HomeworkQuestion.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/HomeworkAnswer.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/question/entity/QuestionBank.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/Homework.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/entity/HomeworkSubmission.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamQuestion.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamAnswer.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/entity/LabStep.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/entity/LabStepAnswer.java`
- Test: `backend/src/test/java/com/opencode/teachingplatform/grading/SchemaSmokeTests.java`

- [ ] **Step 1: Write the failing schema smoke test**

```java
package com.opencode.teachingplatform.grading;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SchemaSmokeTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void gradingRedesignTablesShouldExist() {
        Integer homeworkQuestionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'HOMEWORK_QUESTION'",
                Integer.class
        );
        Integer homeworkAnswerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'HOMEWORK_ANSWER'",
                Integer.class
        );
        Integer questionAttachmentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'QUESTION_ATTACHMENT'",
                Integer.class
        );

        assertThat(homeworkQuestionCount).isEqualTo(1);
        assertThat(homeworkAnswerCount).isEqualTo(1);
        assertThat(questionAttachmentCount).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw.cmd -Dtest=SchemaSmokeTests test`
Expected: FAIL because the new tables do not exist yet.

- [ ] **Step 3: Write the Flyway migration and entity updates**

```sql
CREATE TABLE homework_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  homework_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  sort_order INT NOT NULL,
  question_score DOUBLE NOT NULL,
  question_snapshot_json JSON NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE homework_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  homework_submission_id BIGINT NOT NULL,
  homework_question_id BIGINT NOT NULL,
  answer_text TEXT NULL,
  answer_json JSON NULL,
  auto_score DOUBLE NULL,
  suggested_score DOUBLE NULL,
  final_score DOUBLE NULL,
  score_source VARCHAR(32) NOT NULL,
  judge_detail TEXT NULL,
  teacher_comment TEXT NULL,
  accepted_auto_score BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE question_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_type VARCHAR(32) NOT NULL,
  owner_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(512) NOT NULL,
  media_type VARCHAR(128) NULL,
  sort_order INT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

```java
public enum ScoreVisibilityMode {
    IMMEDIATE,
    AFTER_TEACHER_CONFIRM,
    AFTER_DEADLINE,
    MANUAL_RELEASE
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw.cmd -Dtest=SchemaSmokeTests test`
Expected: PASS, with Flyway applying the new grading redesign migration successfully.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/db/migration backend/src/main/java/com/opencode/teachingplatform/question/entity/QuestionBank.java backend/src/main/java/com/opencode/teachingplatform/question/entity/QuestionAttachment.java backend/src/main/java/com/opencode/teachingplatform/homework/entity backend/src/main/java/com/opencode/teachingplatform/exam/entity backend/src/main/java/com/opencode/teachingplatform/lab/entity backend/src/main/java/com/opencode/teachingplatform/common/enums/ScoreVisibilityMode.java backend/src/test/java/com/opencode/teachingplatform/grading/SchemaSmokeTests.java
git commit -m "feat: add grading redesign schema foundation"
```

---

### Task 2: Build The Shared Grading Module With Strategy Pattern

**Files:**
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/enums/QuestionType.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/enums/ScoreSource.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/enums/ScoringDecisionMode.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/model/ScoringContext.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/model/ScoringResult.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/QuestionScoringStrategy.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/strategy/*.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/service/ScoringEngine.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/grading/service/ScoreRecordWriter.java`
- Test: `backend/src/test/java/com/opencode/teachingplatform/grading/ScoringEngineTests.java`

- [ ] **Step 1: Write failing strategy tests for objective, partial, and subjective recommendation scoring**

```java
@SpringBootTest
class ScoringEngineTests {

    @Autowired
    ScoringEngine scoringEngine;

    @Test
    void multipleChoiceShouldSupportAveragePartialScore() {
        ScoringContext context = TestScoringContexts.multipleChoiceAveragePartial();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.autoScore()).isEqualTo(5.0);
        assertThat(result.finalScore()).isEqualTo(5.0);
        assertThat(result.scoreSource()).isEqualTo(ScoreSource.AUTO);
    }

    @Test
    void fillBlankShouldSupportPerBlankScore() {
        ScoringContext context = TestScoringContexts.fillBlankPerSlot();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.autoScore()).isEqualTo(6.0);
        assertThat(result.finalScore()).isEqualTo(6.0);
    }

    @Test
    void subjectiveShouldProduceRecommendationNotTeacherFinalByDefault() {
        ScoringContext context = TestScoringContexts.subjectiveRecommendationOnly();

        ScoringResult result = scoringEngine.evaluate(context);

        assertThat(result.recommendedScore()).isEqualTo(8.0);
        assertThat(result.finalScore()).isNull();
        assertThat(result.needsTeacherReview()).isTrue();
        assertThat(result.scoreSource()).isEqualTo(ScoreSource.RECOMMENDED);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw.cmd -Dtest=ScoringEngineTests test`
Expected: FAIL because `ScoringEngine`, strategies, and shared scoring types do not exist yet.

- [ ] **Step 3: Implement the shared strategy interface and engine**

```java
public interface QuestionScoringStrategy {
    boolean supports(QuestionType type);
    ScoringResult evaluate(ScoringContext context);
}

@Service
public class ScoringEngine {

    private final List<QuestionScoringStrategy> strategies;

    public ScoringEngine(List<QuestionScoringStrategy> strategies) {
        this.strategies = strategies;
    }

    public ScoringResult evaluate(ScoringContext context) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(context.questionType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported question type: " + context.questionType()))
                .evaluate(context);
    }
}
```

```java
public record ScoringResult(
        Double autoScore,
        Double recommendedScore,
        Double finalScore,
        ScoreSource scoreSource,
        String judgeDetail,
        boolean needsTeacherReview,
        boolean canAutoFinalize
) {}
```

- [ ] **Step 4: Implement the concrete scoring strategies**

```java
@Component
public class MultipleChoiceScoringStrategy implements QuestionScoringStrategy {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.MULTIPLE_CHOICE;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        // Resolve scoring mode from scoringConfigJson
        // Compute exact / average partial / custom partial
        return new ScoringResult(score, null, score, ScoreSource.AUTO, detail, false, true);
    }
}
```

```java
@Component
public class SubjectiveRecommendationStrategy implements QuestionScoringStrategy {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.SUBJECTIVE;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        // Keyword hit + length + structure + attachment completeness
        return new ScoringResult(null, recommended, null, ScoreSource.RECOMMENDED, detail, true, true);
    }
}
```

- [ ] **Step 5: Run tests to verify the shared engine works**

Run: `./mvnw.cmd -Dtest=ScoringEngineTests test`
Expected: PASS, covering objective auto-scoring, partial scoring, and subjective recommendation scoring.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/opencode/teachingplatform/grading backend/src/test/java/com/opencode/teachingplatform/grading/ScoringEngineTests.java
git commit -m "feat: add strategy-based grading core"
```

---

### Task 3: Upgrade Homework To Question-Based Submission And Grading

**Files:**
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/service/HomeworkService.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/controller/HomeworkController.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/dto/HomeworkRequests.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/homework/repository/HomeworkQuestionRepository.java`
- Create: `backend/src/main/java/com/opencode/teachingplatform/homework/repository/HomeworkAnswerRepository.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/question/*`
- Test: `backend/src/test/java/com/opencode/teachingplatform/homework/HomeworkQuestionFlowTests.java`

- [ ] **Step 1: Write the failing homework question-flow tests**

```java
@SpringBootTest
class HomeworkQuestionFlowTests {

    @Autowired
    HomeworkService homeworkService;

    @Test
    void teacherShouldBeAbleToAttachQuestionBankQuestionsAndInlineQuestions() {
        // create homework
        // attach one bank question and one inline question
        // assert two homework questions exist and inline question is not in bank by default
    }

    @Test
    void studentSubmissionShouldCreatePerQuestionAnswersAndRecommendationScores() {
        // submit subjective text answer and objective answer
        // assert homework_answer rows are created
        // assert objective final score and subjective recommendation score are both present
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw.cmd -Dtest=HomeworkQuestionFlowTests test`
Expected: FAIL because homework still uses whole-assignment submission and has no question-level flows.

- [ ] **Step 3: Implement homework question management APIs and repositories**

```java
public interface HomeworkQuestionRepository extends JpaRepository<HomeworkQuestion, Long> {
    List<HomeworkQuestion> findByHomeworkIdOrderBySortOrder(Long homeworkId);
}

public interface HomeworkAnswerRepository extends JpaRepository<HomeworkAnswer, Long> {
    List<HomeworkAnswer> findByHomeworkSubmissionId(Long homeworkSubmissionId);
}
```

```java
public record CreateInlineHomeworkQuestionRequest(
        String type,
        String stem,
        Integer score,
        String scoringConfigJson,
        boolean addToQuestionBank
) {}
```

- [ ] **Step 4: Refactor homework submission and grading to use per-question answers**

```java
@Transactional
public Map<String, Object> submitHomework(CurrentUser currentUser, Long homeworkId, SubmitHomeworkRequest request) {
    HomeworkSubmission submission = loadOrCreateSubmission(homeworkId, currentUser.id());
    submission.setSubmitStatus(SubmissionStatus.SUBMITTED);

    for (SubmitHomeworkAnswerItem item : request.answers()) {
        HomeworkQuestion question = loadHomeworkQuestion(item.homeworkQuestionId());
        ScoringResult scoring = scoringEngine.evaluate(buildHomeworkScoringContext(question, item, homework.isAutoGradingEnabled()));
        saveHomeworkAnswer(submission, question, item, scoring);
    }

    updateHomeworkSubmissionTotal(submission);
    return toSubmissionResponse(submission);
}
```

- [ ] **Step 5: Run tests to verify the homework question flow passes**

Run: `./mvnw.cmd -Dtest=HomeworkQuestionFlowTests test`
Expected: PASS, with homework now supporting bank questions, inline questions, and per-question scoring.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/opencode/teachingplatform/homework backend/src/main/java/com/opencode/teachingplatform/question backend/src/test/java/com/opencode/teachingplatform/homework/HomeworkQuestionFlowTests.java
git commit -m "feat: migrate homework to question-based grading"
```

---

### Task 4: Integrate Shared Grading Into Exam And Lab While Preserving Their Workflows

**Files:**
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamService.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamScoringService.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/service/LabAutoGradingService.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/controller/ExamController.java`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/controller/LabController.java`
- Modify: `backend/src/test/java/com/opencode/teachingplatform/exam/ExamServiceTests.java`
- Create: `backend/src/test/java/com/opencode/teachingplatform/lab/LabGradingFlowTests.java`

- [ ] **Step 1: Write the failing exam and lab integration tests**

```java
@Test
void examSubmissionShouldUseSharedScoringEngineForObjectiveAndSubjectiveQuestions() {
    // create exam with objective and subjective questions
    // submit answers
    // assert objective gets final score, subjective gets recommendation score
}

@Test
void labSubmissionShouldKeepStepFlowButUseSharedScoringResultModel() {
    // create lab step config
    // submit lab
    // assert lab_step_answer contains auto/suggested/final score semantics
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw.cmd -Dtest=ExamServiceTests,LabGradingFlowTests test`
Expected: FAIL because exam/lab still rely on legacy service-specific scoring contracts.

- [ ] **Step 3: Replace ad hoc exam/lab scoring branches with `ScoringEngine` calls**

```java
ScoringResult scoring = scoringEngine.evaluate(
        ScoringContextFactory.forExam(questionBank, answerJson, examQuestion.getQuestionScore(), exam.isAutoGradingEnabled())
);

examAnswer.setAutoScore(scoring.autoScore());
examAnswer.setSuggestedScore(scoring.recommendedScore());
examAnswer.setFinalScore(scoring.finalScore());
examAnswer.setScoreSource(scoring.scoreSource().name());
examAnswer.setJudgeDetail(scoring.judgeDetail());
```

```java
ScoringResult scoring = scoringEngine.evaluate(
        ScoringContextFactory.forLab(step, answer.getAnswerText(), lab.isAutoGradingEnabled())
);

answer.setAutoScore(scoring.autoScore());
answer.setSuggestedScore(scoring.recommendedScore());
answer.setScore(scoring.finalScore());
answer.setScoreSource(scoring.scoreSource().name());
answer.setAutoJudgeDetail(scoring.judgeDetail());
```

- [ ] **Step 4: Preserve workflow-specific status logic and move only score persistence to the shared writer**

```java
scoreRecordWriter.write(
        BusinessType.EXAM,
        exam.getId(),
        submission.getStudentId(),
        exam.getClassId(),
        submission.getTotalScore(),
        OffsetDateTime.now()
);
```

- [ ] **Step 5: Run the integration tests again**

Run: `./mvnw.cmd -Dtest=ExamServiceTests,LabGradingFlowTests test`
Expected: PASS, with exam/lab reusing the grading module without losing their workflow semantics.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/opencode/teachingplatform/exam backend/src/main/java/com/opencode/teachingplatform/lab backend/src/test/java/com/opencode/teachingplatform/exam/ExamServiceTests.java backend/src/test/java/com/opencode/teachingplatform/lab/LabGradingFlowTests.java
git commit -m "feat: integrate shared grading into exam and lab"
```

---

### Task 5: Add Teacher Confirmation And Score Visibility Workflows

**Files:**
- Modify: `backend/src/main/java/com/opencode/teachingplatform/homework/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/exam/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/lab/*`
- Modify: `backend/src/main/java/com/opencode/teachingplatform/analysis/service/AnalysisService.java`
- Create: `backend/src/test/java/com/opencode/teachingplatform/analysis/ScoreVisibilityTests.java`

- [ ] **Step 1: Write failing workflow tests for teacher confirmation and score visibility**

```java
@SpringBootTest
class ScoreVisibilityTests {

    @Test
    void subjectiveAnswersShouldRemainPendingReviewInManualConfirmationMode() {
        // submit homework with subjective answer under manual confirmation mode
        // assert final score not student-visible yet
    }

    @Test
    void autoGradingModeShouldWriteCurrentFinalScoreButStillAllowTeacherOverride() {
        // publish with auto grading enabled
        // assert final score exists and later teacher update persists
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw.cmd -Dtest=ScoreVisibilityTests test`
Expected: FAIL because current workflow does not separate score generation from student visibility.

- [ ] **Step 3: Implement teacher confirmation endpoints and score visibility configuration**

```java
@PostMapping("/teacher/homework-submissions/{submissionId}/answers/{answerId}/accept-recommendation")
public ApiResponse<?> acceptRecommendation(...) {
    return ApiResponse.ok(homeworkService.acceptRecommendation(...));
}

@PostMapping("/homeworks/{id}/score-visibility")
public ApiResponse<?> updateScoreVisibility(...) {
    return ApiResponse.ok(homeworkService.updateScoreVisibility(...));
}
```

- [ ] **Step 4: Update student-facing result queries to honor visibility rules**

```java
if (!scoreVisibilityPolicy.canStudentSeeScore(activity, submission)) {
    detail.put("totalScore", null);
    detail.put("questionScores", List.of());
    detail.put("scoreVisible", false);
} else {
    detail.put("scoreVisible", true);
}
```

- [ ] **Step 5: Run tests to verify the workflow passes**

Run: `./mvnw.cmd -Dtest=ScoreVisibilityTests test`
Expected: PASS, with manual-confirmation and auto-grading modes both honoring visibility policy.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/opencode/teachingplatform/homework backend/src/main/java/com/opencode/teachingplatform/exam backend/src/main/java/com/opencode/teachingplatform/lab backend/src/main/java/com/opencode/teachingplatform/analysis backend/src/test/java/com/opencode/teachingplatform/analysis/ScoreVisibilityTests.java
git commit -m "feat: add grading confirmation and score visibility workflow"
```

---

### Task 6: Implement Frontend Question Management, Grading Panels, And Student Answer Flows

**Files:**
- Create: `frontend/src/api/questions.ts`
- Modify: `frontend/src/api/homeworks.ts`
- Modify: `frontend/src/api/exams.ts`
- Create: `frontend/src/components/questions/QuestionSelector.vue`
- Create: `frontend/src/components/questions/QuestionEditor.vue`
- Create: `frontend/src/components/questions/ScoringConfigEditor.vue`
- Create: `frontend/src/components/grading/AnswerReviewCard.vue`
- Create: `frontend/src/components/grading/SubmissionGradingPanel.vue`
- Create: `frontend/src/components/grading/ScoreVisibilitySettings.vue`
- Create: `frontend/src/components/answers/SubjectiveAnswerEditor.vue`
- Create: `frontend/src/components/answers/AttachmentUploader.vue`
- Modify: `frontend/src/views/teacher/homeworks/*`
- Modify: `frontend/src/views/teacher/exams/*`
- Modify: `frontend/src/views/student/homeworks/*`
- Modify: `frontend/src/views/student/exams/*`
- Test: `frontend/.playwright-cli/grading-smoke.spec.ts`

- [ ] **Step 1: Write the failing frontend smoke test**

```ts
import { test, expect } from '@playwright/test'

test('teacher can add a bank question, create an inline question, and accept recommendation scores', async ({ page }) => {
  await page.goto('/teacher/homeworks')
  await page.getByRole('button', { name: '从题库选题' }).click()
  await page.getByRole('button', { name: '新建题目' }).click()
  await page.getByLabel('加入题库').uncheck()
  await page.getByRole('button', { name: '接受推荐分' }).click()
  await expect(page.getByText('评分已更新')).toBeVisible()
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npx playwright test frontend/.playwright-cli/grading-smoke.spec.ts`
Expected: FAIL because the new teacher question-management and grading UI does not exist yet.

- [ ] **Step 3: Implement teacher question management and grading components**

```vue
<template>
  <div class="submission-grading-panel">
    <AnswerReviewCard
      v-for="answer in answers"
      :key="answer.id"
      :answer="answer"
      @accept-recommendation="acceptRecommendation(answer.id)"
      @score-change="updateScore(answer.id, $event)"
    />
    <div class="panel-actions">
      <el-button @click="acceptAllRecommendations">一键接受全部推荐分</el-button>
      <el-button type="primary" @click="finalizeGrading">完成评分</el-button>
    </div>
  </div>
</template>
```

```vue
<template>
  <div class="subjective-answer-editor">
    <el-input v-model="text" type="textarea" :rows="8" />
    <AttachmentUploader v-model:file-list="attachments" multiple />
  </div>
</template>
```

- [ ] **Step 4: Wire API clients and student answer flows to the new backend endpoints**

```ts
export function acceptHomeworkRecommendation(submissionId: number, answerId: number) {
  return request.post(`/teacher/homework-submissions/${submissionId}/answers/${answerId}/accept-recommendation`)
}

export function submitHomeworkAnswers(homeworkId: number, payload: SubmitHomeworkPayload) {
  return request.post(`/student/homeworks/${homeworkId}/submit`, payload)
}
```

- [ ] **Step 5: Run the frontend smoke test again**

Run: `npx playwright test frontend/.playwright-cli/grading-smoke.spec.ts`
Expected: PASS, covering teacher question management and recommendation acceptance.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/api frontend/src/components frontend/src/views frontend/.playwright-cli/grading-smoke.spec.ts
git commit -m "feat: add grading and question management UI"
```

---

### Task 7: End-To-End Regression Verification And Documentation Sync

**Files:**
- Modify: `teaching-platform/README.md`
- Modify: `teaching-platform/design/04-domain-and-api-impact-map.md`
- Modify: `teaching-platform/docs/superpowers/specs/2026-04-15-grading-system-redesign-design.md`

- [ ] **Step 1: Add the final regression checklist to the docs**

```md
## Regression Checklist

- Homework supports bank question + inline question creation
- Subjective answers support text + multiple attachments
- Objective questions auto-score correctly
- Subjective questions generate recommendation scores
- Teacher can accept recommendation or override manually
- Student score visibility follows configured policy
- `score_record` is updated only from final effective scores
```

- [ ] **Step 2: Run backend regression suite**

Run: `./mvnw.cmd test`
Expected: PASS, including grading, homework, exam, lab, and analysis tests.

- [ ] **Step 3: Run frontend build and smoke tests**

Run: `npm run build`
Expected: PASS, with no TypeScript or Vite build errors.

Run: `npx playwright test frontend/.playwright-cli/grading-smoke.spec.ts`
Expected: PASS.

- [ ] **Step 4: Review manual teacher/student flows locally**

```text
Teacher:
1. Create homework
2. Add one bank question and one inline subjective question
3. Publish with and without auto grading
4. Open grading panel and accept recommendation

Student:
1. Submit objective + subjective answers
2. Upload multiple attachments on subjective question
3. Verify score visibility before and after teacher release
```

- [ ] **Step 5: Commit documentation and verification updates**

```bash
git add teaching-platform/README.md teaching-platform/design/04-domain-and-api-impact-map.md teaching-platform/docs/superpowers/specs/2026-04-15-grading-system-redesign-design.md teaching-platform/docs/superpowers/plans/2026-04-15-grading-system-redesign.md
git commit -m "docs: document grading redesign rollout and verification"
```

---

## Self-Review

### Spec coverage

- Shared `grading/` module: covered in Task 2 and Task 4
- Strategy pattern for question scoring: covered in Task 2
- Homework questionization: covered in Task 3
- Homework/exam bank + inline question creation: covered in Task 3 and Task 6
- Strong structured attachments: covered in Task 1, Task 3, and Task 6
- Teacher recommendation acceptance and manual override: covered in Task 5 and Task 6
- Score visibility decoupled from automatic grading: covered in Task 5 and Task 6
- Frontend grading panel and student answer UI: covered in Task 6

### Placeholder scan

- No `TBD`, `TODO`, or “implement later” markers remain in the plan.
- Code steps include actual example code, not only prose.

### Type consistency

- `QuestionType`, `ScoreSource`, `ScoringContext`, `ScoringResult`, and `ScoringEngine` names are consistent across tasks.
- Homework question/answer model names stay consistent across persistence, service, and frontend sections.

## Execution Handoff

Plan complete and saved to `teaching-platform/docs/superpowers/plans/2026-04-15-grading-system-redesign.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
