# Homework And Exam Shared Question Capability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reuse the existing teacher question-configuration wheel from the question-bank module and the existing student answering wheel from the lab module so homework and exam share the same question configuration, answer payload, and grading-adapter path while keeping their domain-specific shells intact.

**Architecture:** Keep `homework/` and `exam/` as independent business modules and keep `grading/` as the only scoring core. Extract a lightweight shared question capability layer for snapshot models, answer payload normalization, and grading adaptation; on the frontend, extract reusable teacher question-config components from the current question-bank implementation and reuse the current student answering interaction patterns from the lab implementation without forcing homework or exam to adopt the lab page shell.

**Tech Stack:** Spring Boot 3.3, Spring Data JPA, Flyway, H2/MySQL, Vue 3 `<script setup>`, Vite, Element Plus, Vue Router, existing `request.ts` API wrapper, existing `grading` module, existing teacher question-bank UI, existing student lab answering UI.

---

## File Structure

### Create
- `teaching-platform/frontend/src/components/question-config/QuestionEditorDialog.vue`
- `teaching-platform/frontend/src/components/question-config/QuestionPickerDialog.vue`
- `teaching-platform/frontend/src/components/question-config/QuestionConfigList.vue`
- `teaching-platform/frontend/src/types/question-config.ts`
- `teaching-platform/frontend/src/types/question-answer.ts`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/model/QuestionSnapshot.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/model/QuestionAnswerPayload.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/model/GradingResultView.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/service/QuestionSnapshotFactory.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/service/AnswerPayloadNormalizer.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/service/GradingAdapter.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/QuestionSnapshotFactoryTests.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/AnswerPayloadNormalizerTests.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/GradingAdapterTests.java`

### Modify
- `teaching-platform/frontend/src/views/teacher/Questions.vue`
- `teaching-platform/frontend/src/views/teacher/Homeworks.vue`
- `teaching-platform/frontend/src/views/teacher/exams/List.vue`
- `teaching-platform/frontend/src/views/student/HomeworkDetail.vue`
- `teaching-platform/frontend/src/views/student/exams/Detail.vue`
- `teaching-platform/frontend/src/api/homeworks.ts`
- `teaching-platform/frontend/src/api/exams.ts`
- `teaching-platform/frontend/src/types/homework.ts`
- `teaching-platform/frontend/src/types/exam.ts`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/homework/service/HomeworkService.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamService.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamQuestion.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/homework/dto/HomeworkRequests.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/dto/ExamRequests.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/common/enums/ScoreVisibilityMode.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/homework/HomeworkQuestionFlowTests.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/exam/ExamServiceTests.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/exam/ExamTeacherControllerTests.java`

### Verify
- `teaching-platform/frontend/package.json`
- `teaching-platform/backend/pom.xml`

---

## Task 1: Define shared frontend question-config contracts

**Files:**
- Create: `teaching-platform/frontend/src/types/question-config.ts`
- Create: `teaching-platform/frontend/src/types/question-answer.ts`
- Modify: `teaching-platform/frontend/src/types/homework.ts`
- Modify: `teaching-platform/frontend/src/types/exam.ts`

- [ ] **Step 1: Write the shared teacher config types**

Create `question-config.ts` with a single source of truth for configured questions:

```ts
export type QuestionSourceType = 'BANK' | 'INLINE'

export interface ConfiguredQuestionItem {
  localId: string
  questionBankId?: number | null
  sourceType: QuestionSourceType
  questionType: string
  stem: string
  sortOrder: number
  score: number
  options?: Array<{ key: string; label: string }> | null
  answerJson?: string | null
  scoringConfigJson?: string | null
}
```

- [ ] **Step 2: Write the shared student answer types**

Create `question-answer.ts` with a normalized payload shared by homework and exam submissions:

```ts
export interface UnifiedQuestionAnswerPayload {
  questionRefId: number
  questionType: string
  answerText?: string | null
  answerJson?: string | null
  selectedOptions?: string[]
  attachmentPath?: string | null
}
```

- [ ] **Step 3: Update homework and exam types to reference the shared contracts**

In `types/homework.ts`, keep business-shell types but narrow the answer item structure to align with `UnifiedQuestionAnswerPayload`:

```ts
export interface SubmitHomeworkAnswerItem {
  homeworkQuestionId: number
  answerText?: string | null
  answerJson?: string | null
  selectedOptions?: string[]
  attachmentPath?: string | null
}
```

In `types/exam.ts`, keep exam business fields but make the answer list structurally parallel:

```ts
export interface SubmitExamPayload {
  answers: Array<{
    questionId: number
    answerText?: string | null
    answerJson?: string | null
    selectedOptions?: string[]
    attachmentPath?: string | null
  }>
}
```

- [ ] **Step 4: Run the frontend build to verify contract-only changes compile**

Run: `npm run build`

Expected: PASS with no type errors caused by the new shared config and answer contract files.

- [ ] **Step 5: Commit**

```bash
git add teaching-platform/frontend/src/types/question-config.ts teaching-platform/frontend/src/types/question-answer.ts teaching-platform/frontend/src/types/homework.ts teaching-platform/frontend/src/types/exam.ts
git commit -m "refactor: add shared question config contracts"
```

---

## Task 2: Extract reusable teacher question configuration UI from the existing question-bank flow

**Files:**
- Create: `teaching-platform/frontend/src/components/question-config/QuestionEditorDialog.vue`
- Create: `teaching-platform/frontend/src/components/question-config/QuestionPickerDialog.vue`
- Create: `teaching-platform/frontend/src/components/question-config/QuestionConfigList.vue`
- Modify: `teaching-platform/frontend/src/views/teacher/Questions.vue`

- [ ] **Step 1: Write the failing frontend integration expectation by documenting the desired props/events in code comments and using them in `Questions.vue`**

Add the intended component contract usage inside `Questions.vue` before implementing the components:

```vue
<QuestionEditorDialog
  v-model:visible="editorVisible"
  :initial-question="editingQuestion"
  @saved="handleQuestionSaved"
/>

<QuestionPickerDialog
  v-model:visible="pickerVisible"
  @confirm="handlePickedQuestions"
/>
```

This should initially fail the build because the new components do not exist yet.

- [ ] **Step 2: Run the frontend build to verify it fails for missing components**

Run: `npm run build`

Expected: FAIL with module resolution errors for the new `question-config` components.

- [ ] **Step 3: Implement `QuestionEditorDialog.vue` by extracting the existing teacher question-bank editing wheel rather than rewriting it**

Use the current typed editor logic already proven in `Questions.vue` and move it behind a dialog shell:

```vue
<script setup lang="ts">
import { computed } from 'vue'
import type { ConfiguredQuestionItem } from '@/types/question-config'

const props = defineProps<{
  visible: boolean
  initialQuestion?: ConfiguredQuestionItem | null
}>()

const emit = defineEmits<{
  'update:visible': [boolean]
  saved: [ConfiguredQuestionItem]
}>()
</script>
```

- [ ] **Step 4: Implement `QuestionPickerDialog.vue` by reusing table/filter/pick interaction already present in the teacher question-bank view**

Provide a dialog that outputs `ConfiguredQuestionItem[]` with score and sort order editing:

```vue
<script setup lang="ts">
import { ref } from 'vue'
import type { ConfiguredQuestionItem } from '@/types/question-config'

const emit = defineEmits<{
  'update:visible': [boolean]
  confirm: [ConfiguredQuestionItem[]]
}>()

const selectedRows = ref<ConfiguredQuestionItem[]>([])
</script>
```

- [ ] **Step 5: Implement `QuestionConfigList.vue` for shared rendering/edit/remove/reorder behavior**

Expose explicit events so homework and exam shells can own persistence:

```vue
<script setup lang="ts">
import type { ConfiguredQuestionItem } from '@/types/question-config'

defineProps<{ items: ConfiguredQuestionItem[] }>()
defineEmits<{
  edit: [ConfiguredQuestionItem]
  remove: [string]
  reorder: [ConfiguredQuestionItem[]]
  scoreChange: [{ localId: string; score: number }]
}>()
</script>
```

- [ ] **Step 6: Refactor `Questions.vue` to consume the extracted components and keep question-bank persistence inside the page shell**

Preserve the current backend API payload builder in `Questions.vue`; only move the UI wheel, not the page-level orchestration.

- [ ] **Step 7: Run the frontend build to verify the extracted question-config components compile cleanly**

Run: `npm run build`

Expected: PASS, and `Questions.vue` still works as the source implementation for the extracted wheel.

- [ ] **Step 8: Commit**

```bash
git add teaching-platform/frontend/src/components/question-config teaching-platform/frontend/src/views/teacher/Questions.vue
git commit -m "refactor: extract reusable teacher question config components"
```

---

## Task 3: Reuse the extracted teacher question configuration components in homework and exam shells

**Files:**
- Modify: `teaching-platform/frontend/src/views/teacher/Homeworks.vue`
- Modify: `teaching-platform/frontend/src/views/teacher/exams/List.vue`
- Modify: `teaching-platform/frontend/src/api/homeworks.ts`
- Modify: `teaching-platform/frontend/src/api/exams.ts`

- [ ] **Step 1: Replace homework inline question wiring controls with shared components**

Replace the local “questionId + sortOrder + score” editing region in `Homeworks.vue` with:

```vue
<QuestionConfigList
  :items="configuredQuestions"
  @edit="openQuestionEditor"
  @remove="removeConfiguredQuestion"
  @score-change="updateConfiguredQuestionScore"
/>

<QuestionEditorDialog
  v-model:visible="questionEditorVisible"
  :initial-question="editingConfiguredQuestion"
  @saved="upsertConfiguredQuestion"
/>

<QuestionPickerDialog
  v-model:visible="questionPickerVisible"
  @confirm="appendConfiguredQuestions"
/>
```

- [ ] **Step 2: Replace exam local question-pool UI with the same shared components**

Refactor `teacher/exams/List.vue` so the exam shell still owns exam fields, but no longer owns a private question-pool implementation.

- [ ] **Step 3: Keep the save APIs shell-specific**

Do not force homework and exam to call the same endpoint. Instead, keep shell-specific persistence methods that serialize `ConfiguredQuestionItem[]` into each domain payload:

```ts
const buildHomeworkPayload = () => ({
  ...form,
  questions: configuredQuestions.value.map(item => ({
    questionId: item.questionBankId,
    sortOrder: item.sortOrder,
    questionScore: item.score,
  })),
})
```

```ts
const buildExamPayload = () => ({
  ...form,
  questions: configuredQuestions.value.map(item => ({
    questionId: item.questionBankId,
    sortOrder: item.sortOrder,
    questionScore: item.score,
  })),
})
```

- [ ] **Step 4: Update frontend API helpers only where required to support page-shell save flow**

Keep `api/homeworks.ts` and `api/exams.ts` typed and module-specific. Do not merge them into one generic API client.

- [ ] **Step 5: Run the frontend build to verify homework and exam compile with shared teacher question-config UI**

Run: `npm run build`

Expected: PASS, with `Homeworks.vue` and `teacher/exams/List.vue` both compiling against the extracted components.

- [ ] **Step 6: Commit**

```bash
git add teaching-platform/frontend/src/views/teacher/Homeworks.vue teaching-platform/frontend/src/views/teacher/exams/List.vue teaching-platform/frontend/src/api/homeworks.ts teaching-platform/frontend/src/api/exams.ts
git commit -m "refactor: share teacher question config across homework and exam"
```

---

## Task 4: Add shared backend snapshot and answer payload models without touching grading core

**Files:**
- Create: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/model/QuestionSnapshot.java`
- Create: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/model/QuestionAnswerPayload.java`
- Create: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/model/GradingResultView.java`
- Create: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/QuestionSnapshotFactoryTests.java`
- Create: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/AnswerPayloadNormalizerTests.java`

- [ ] **Step 1: Write failing unit tests for snapshot and answer normalization contracts**

```java
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
        assertEquals(10.0, snapshot.score());
    }
}
```

- [ ] **Step 2: Run backend tests to verify the new shared models do not exist yet**

Run: `./mvnw.cmd -Dtest=QuestionSnapshotFactoryTests,AnswerPayloadNormalizerTests test`

Expected: FAIL because the `assessment` model and normalizer classes do not exist yet.

- [ ] **Step 3: Implement the shared immutable models**

```java
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
) {}
```

```java
public record QuestionAnswerPayload(
        Long questionRefId,
        String questionType,
        String answerText,
        String answerJson,
        List<String> selectedOptions,
        String attachmentPath
) {}
```

- [ ] **Step 4: Implement simple normalizers and factories with no domain-state coupling**

Ensure these classes contain only model conversion logic and zero `homework` or `exam` submission state transitions.

- [ ] **Step 5: Run backend tests to verify the shared model layer passes**

Run: `./mvnw.cmd -Dtest=QuestionSnapshotFactoryTests,AnswerPayloadNormalizerTests test`

Expected: PASS with clean shared model-level behavior.

- [ ] **Step 6: Commit**

```bash
git add teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment
git commit -m "refactor: add shared question snapshot and answer payload models"
```

---

## Task 5: Add a shared grading adapter that reuses the existing grading module

**Files:**
- Create: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/service/GradingAdapter.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/homework/service/HomeworkService.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamService.java`
- Create: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/GradingAdapterTests.java`

- [ ] **Step 1: Write a failing adapter test that proves the adapter bridges snapshot + answer to the existing grading engine**

```java
class GradingAdapterTests {

    @Test
    void shouldMapSnapshotAndAnswerPayloadIntoGradingResultView() {
        QuestionSnapshot snapshot = new QuestionSnapshot(
                12L,
                "BANK",
                "TRUE_FALSE",
                "Java is statically typed.",
                1,
                5.0,
                null,
                "{\"correct\":true}",
                "{\"decisionMode\":\"AUTO_FINAL\"}",
                true
        );
        QuestionAnswerPayload payload = new QuestionAnswerPayload(21L, "TRUE_FALSE", null, "{\"selectedOptions\":[\"true\"]}", List.of("true"), null);

        GradingResultView result = gradingAdapter.grade(snapshot, payload);

        assertEquals(5.0, result.autoScore());
        assertEquals("AUTO", result.scoreSource());
    }
}
```

- [ ] **Step 2: Run backend tests to verify adapter-specific compilation fails before implementation**

Run: `./mvnw.cmd -Dtest=GradingAdapterTests test`

Expected: FAIL because `GradingAdapter` does not exist yet.

- [ ] **Step 3: Implement `GradingAdapter` as a thin bridge, not a new scoring core**

```java
@Service
public class GradingAdapter {

    private final ScoringEngine scoringEngine;

    public GradingAdapter(ScoringEngine scoringEngine) {
        this.scoringEngine = scoringEngine;
    }

    public GradingResultView grade(QuestionSnapshot snapshot, QuestionAnswerPayload payload) {
        ScoringContext context = buildContext(snapshot, payload);
        ScoringResult result = scoringEngine.evaluate(context);
        return new GradingResultView(
                result.autoScore(),
                result.recommendedScore(),
                result.finalScore(),
                result.judgeDetail(),
                result.scoreSource().name(),
                result.isCorrect(),
                payload.answerJson()
        );
    }
}
```

- [ ] **Step 4: Refactor homework and exam services to consume the adapter instead of assembling grading input ad hoc**

Update only the grading call path. Do not merge the two services.

- [ ] **Step 5: Run focused backend tests for the adapter and the existing homework/exam scoring flows**

Run: `./mvnw.cmd -Dtest=GradingAdapterTests,HomeworkQuestionFlowTests,ExamServiceTests test`

Expected: PASS, proving the shared adapter sits in front of the existing `grading` core without changing domain shells.

- [ ] **Step 6: Commit**

```bash
git add teaching-platform/backend/src/main/java/com/opencode/teachingplatform/assessment/service/GradingAdapter.java teaching-platform/backend/src/main/java/com/opencode/teachingplatform/homework/service/HomeworkService.java teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamService.java teaching-platform/backend/src/test/java/com/opencode/teachingplatform/assessment/GradingAdapterTests.java teaching-platform/backend/src/test/java/com/opencode/teachingplatform/homework/HomeworkQuestionFlowTests.java teaching-platform/backend/src/test/java/com/opencode/teachingplatform/exam/ExamServiceTests.java
git commit -m "refactor: reuse grading through shared assessment adapter"
```

---

## Task 6: Make exam snapshots stable and align homework/exam payload contracts

**Files:**
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamQuestion.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamService.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/homework/dto/HomeworkRequests.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/dto/ExamRequests.java`
- Modify: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/exam/ExamTeacherControllerTests.java`

- [ ] **Step 1: Write a failing exam service test that proves exam questions must use a stored snapshot rather than live question-bank data**

```java
@Test
void publishedExamShouldReadQuestionTextFromStoredSnapshot() {
    Long examId = seedPublishedExamWithSnapshotQuestion("Original snapshot stem");
    mutateQuestionBankStem(examId, "Mutated bank stem");

    Map<String, Object> detail = examService.getExamDetail(teacherUser(), examId);
    List<Map<String, Object>> questions = (List<Map<String, Object>>) detail.get("questions");

    assertEquals("Original snapshot stem", questions.get(0).get("stem"));
}
```

- [ ] **Step 2: Run the focused exam test to verify it fails with current live-bank behavior**

Run: `./mvnw.cmd -Dtest=ExamTeacherControllerTests test`

Expected: FAIL because exam detail currently resolves question content from the live question bank.

- [ ] **Step 3: Add or fully use `questionSnapshotJson` in `ExamQuestion` and update `ExamService` to read from snapshot first**

Persist a stable question definition when saving exam questions and resolve `stem/options/answer/scoringConfig` from that snapshot during exam detail and grading flows.

- [ ] **Step 4: Align DTOs so homework and exam carry structurally parallel configured-question and answer payloads**

Keep field names domain-specific where necessary, but align shape and meaning.

- [ ] **Step 5: Re-run focused backend tests to verify the new contract and snapshot behavior**

Run: `./mvnw.cmd -Dtest=HomeworkQuestionFlowTests,ExamTeacherControllerTests,ExamServiceTests test`

Expected: PASS, proving exam snapshots are now stable and the homework/exam payload contracts are structurally aligned.

- [ ] **Step 6: Commit**

```bash
git add teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/entity/ExamQuestion.java teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamService.java teaching-platform/backend/src/main/java/com/opencode/teachingplatform/homework/dto/HomeworkRequests.java teaching-platform/backend/src/main/java/com/opencode/teachingplatform/exam/dto/ExamRequests.java teaching-platform/backend/src/test/java/com/opencode/teachingplatform/exam/ExamTeacherControllerTests.java
git commit -m "refactor: stabilize exam question snapshots and align payload contracts"
```

---

## Task 7: Reuse the existing lab answering wheel for homework and exam answering interactions

**Files:**
- Modify: `teaching-platform/frontend/src/views/student/HomeworkDetail.vue`
- Modify: `teaching-platform/frontend/src/views/student/exams/Detail.vue`
- Modify: `teaching-platform/frontend/src/types/homework.ts`
- Modify: `teaching-platform/frontend/src/types/exam.ts`

- [ ] **Step 1: Identify and lift the existing lab answering interaction patterns instead of recreating input controls from scratch**

Copy the interaction pattern, not the full page shell, from the current lab answering implementation:

```ts
// patterns to preserve
// - question type driven renderer
// - current question context persistence inside the page shell
// - structured answer payload generation
```

- [ ] **Step 2: Refactor `HomeworkDetail.vue` so question answers are generated through the shared payload shape**

Objective questions should no longer rely on ad hoc textarea-only behavior. Keep the homework page shell, but reuse the lab answering wheel’s question-type-driven input behavior.

- [ ] **Step 3: Refactor `student/exams/Detail.vue` to emit the same normalized payload structure while preserving exam-specific timer and submit flow**

Keep:
- exam timer
- start / resume / submit state machine

Reuse:
- type-driven answer controls
- structured answer payload builder

- [ ] **Step 4: Run the frontend build to verify homework and exam student pages compile after answer-wheel reuse**

Run: `npm run build`

Expected: PASS, with no new ad hoc answering contract introduced in either page.

- [ ] **Step 5: Commit**

```bash
git add teaching-platform/frontend/src/views/student/HomeworkDetail.vue teaching-platform/frontend/src/views/student/exams/Detail.vue teaching-platform/frontend/src/types/homework.ts teaching-platform/frontend/src/types/exam.ts
git commit -m "refactor: reuse lab answering patterns in homework and exam"
```

---

## Task 8: Verify the integrated homework and exam shared-question path end-to-end

**Files:**
- Verify only; no new source files required unless test fixes reveal missing contract fields

- [ ] **Step 1: Run the focused backend shared-question test suite**

Run: `./mvnw.cmd -Dtest=QuestionSnapshotFactoryTests,AnswerPayloadNormalizerTests,GradingAdapterTests,HomeworkQuestionFlowTests,ExamServiceTests,ExamTeacherControllerTests test`

Expected: PASS, proving snapshot, normalization, grading adaptation, and homework/exam domain integration all work together.

- [ ] **Step 2: Run the frontend production build**

Run: `npm run build`

Expected: PASS, proving teacher config reuse and student answering reuse both compile in production mode.

- [ ] **Step 3: Run one manual shell-level verification pass for homework and exam**

Manual checklist:

```text
1. Teacher opens homework page and adds one bank question through the shared picker dialog.
2. Teacher opens exam page and adds one inline question through the shared editor dialog.
3. Student opens homework detail and submits a structured answer payload.
4. Student opens exam detail, answers a question, and submits while timer shell remains intact.
5. Teacher opens exam results and homework submissions; both show grading-compatible result fields.
```

- [ ] **Step 4: Commit the verification checkpoint if fixes were required**

```bash
git add .
git commit -m "chore: verify shared question capability integration"
```

---

## Spec Coverage Check

- Shared teacher question configuration for homework and exam: covered by Tasks 1-3.
- Shared backend snapshot and answer payload models: covered by Tasks 4-6.
- Reuse existing `grading` module rather than create a new scoring core: covered by Task 5.
- Preserve homework and exam business shells: enforced throughout Tasks 3, 5, 6, and 7.
- Reuse existing question-bank teacher wheel and lab student answering wheel: explicitly covered by Tasks 2 and 7.
- Keep lab out of formal first-phase implementation scope: preserved throughout; lab is referenced only as a reuse source, not a migration target.

## Placeholder Scan

- No `TODO`, `TBD`, or “implement later” placeholders remain.
- Every task lists exact file paths.
- Every verification step includes a concrete command and expected outcome.
- Commits are included as checkpoints but should only be executed if the implementing user explicitly wants commits during execution.

## Scope Check

This plan stays within a single implementation stream: homework + exam first-phase shared question capability. It does not include a lab migration project, new grading-core development, or a full submission-state redesign.

Plan complete and saved to `teaching-platform/docs/superpowers/plans/2026-04-22-homework-exam-shared-question-capability-implementation-plan.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
