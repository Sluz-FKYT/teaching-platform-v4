# Student Lab Answer Workbench Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the student lab answer page into a viewport-locked dual-scroll workbench with per-question interaction components, structured answer JSON persistence, and backward compatibility for the current `answerText` scoring path.

**Architecture:** The safest path is to upgrade the backend lab answer contract first so the student page can fetch normalized answer metadata and save `answerPayloadJson` without guessing raw teacher JSON. Then refactor the student page around a typed answer draft model, split question-type renderers into focused UI units, and finally wire the viewport-locked layout and full-stack verification path.

**Tech Stack:** Vue 3 `<script setup>`, Element Plus, existing Axios wrapper in `frontend/src/utils/request.ts`, student views in `frontend/src/views/student/`, Spring Boot 3.3 controllers/services/entities under `backend/src/main/java/com/opencode/teachingplatform/lab/`, H2 `test` profile, Playwright CLI for UI verification.

---

## File Structure

### Create
- `teaching-platform/frontend/src/views/student/components/lab-answer/types.ts`
- `teaching-platform/frontend/src/views/student/components/lab-answer/answerPayload.ts`
- `teaching-platform/frontend/src/views/student/components/lab-answer/useLabAnswerDrafts.ts`
- `teaching-platform/frontend/src/views/student/components/lab-answer/QuestionAnswerShell.vue`
- `teaching-platform/frontend/src/views/student/components/lab-answer/ChoiceAnswerEditor.vue`
- `teaching-platform/frontend/src/views/student/components/lab-answer/FillBlankInlineEditor.vue`
- `teaching-platform/frontend/src/views/student/components/lab-answer/TextAnswerEditor.vue`
- `teaching-platform/frontend/src/views/student/components/lab-answer/CodeLabAnswerEditor.vue`

### Modify
- `teaching-platform/frontend/src/views/student/LabSteps.vue`
- `teaching-platform/frontend/src/views/student/CodeAnswerEditor.vue`
- `teaching-platform/frontend/src/api/labs.ts`
- `teaching-platform/frontend/src/types/lab.ts`
- `teaching-platform/frontend/src/layout/index.vue`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/dto/LabRequests.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/entity/LabStepAnswer.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/controller/LabController.java`

### Test / Verify
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/`
- `teaching-platform/frontend/package.json`
- `teaching-platform/.playwright-cli/` (generated artifacts only)

---

## Task 1: Expand lab answer backend contracts for structured metadata and answer JSON

**Files:**
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/dto/LabRequests.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/entity/LabStepAnswer.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/controller/LabController.java`
- Test: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/`

- [ ] **Step 1: Add DTO fields for student-facing structured metadata and saved payload JSON**

Extend the lab detail response DTOs so each student step can expose typed metadata instead of only `answerText`.

Add fields conceptually equivalent to:

```java
List<ChoiceOptionDto> options,
List<BlankMetaDto> blanks,
String answerPayloadJson,
String editorLanguage
```

Keep them optional so non-applicable question types stay lean.

- [ ] **Step 2: Keep save request backward compatible while making JSON first-class**

Retain `answerText` on `SaveStepAnswerRequest`, but ensure `answerPayloadJson` is part of the intended save path, not just a dead field.

Target shape:

```java
public record SaveStepAnswerRequest(
    @NotBlank String answerText,
    String answerPayloadJson
) {}
```

If request validation needs relaxing for generated answerText edge cases, do it deliberately and update tests accordingly.

- [ ] **Step 3: Persist `answerPayloadJson` into `LabStepAnswer.answerJson`**

Upgrade `LabService.saveAnswer(...)` so it writes both the compatibility text answer and the structured JSON answer.

Implementation target:

```java
answer.setAnswerText(request.answerText());
answer.setAnswerJson(request.answerPayloadJson());
```

Preserve existing timestamp, status, and answer-log behavior.

- [ ] **Step 4: Project student-facing question metadata from lab-step config**

In `getStudentLabDetail(...)`, derive normalized student metadata from `questionType`, `answerConfigJson`, `questionSnapshotJson`, and `editorLanguage`.

Projection rules:
- SINGLE / MULTIPLE / TRUE_FALSE → `options`
- FILL_BLANK → `blanks`
- CODE → `editorLanguage`
- all question types → `answerPayloadJson` if a saved answer exists

Do not expose raw teacher-only config blobs directly if a normalized view is enough.

- [ ] **Step 5: Add backend tests for save + detail projection**

Add or update lab service/controller tests to verify:
- saving a choice answer writes both `answerText` and `answerJson`
- student lab detail returns `options` for objective questions
- student lab detail returns `blanks` for fill-blank questions
- historical rows with `answerJson = null` still return `answerText`

- [ ] **Step 6: Run backend verification**

Run: `./mvnw.cmd test`

Expected: PASS with lab-related tests covering the upgraded detail and save contract.

---

## Task 2: Define student-side typed answer contracts and conversion helpers

**Files:**
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/types.ts`
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/answerPayload.ts`
- Modify: `teaching-platform/frontend/src/types/lab.ts`
- Modify: `teaching-platform/frontend/src/api/labs.ts`

- [ ] **Step 1: Add student detail types for structured metadata**

Extend `frontend/src/types/lab.ts` so `StudentLabStepItem` can describe normalized metadata returned by Task 1.

Target additions:

```ts
interface StudentChoiceOption {
  key: string
  label: string
}

interface StudentBlankMeta {
  index: number
  token: string
}
```

And on each item:

```ts
options?: StudentChoiceOption[]
blanks?: StudentBlankMeta[]
answerPayloadJson?: string
editorLanguage?: string
```

- [ ] **Step 2: Define the page-internal typed answer model**

Create `types.ts` with a focused UI contract:

```ts
export type LabAnswerDraft =
  | { kind: 'single'; selectedKey: string | null }
  | { kind: 'multiple'; selectedKeys: string[] }
  | { kind: 'judge'; value: 'TRUE' | 'FALSE' | null }
  | { kind: 'fill'; blanks: Array<{ index: number; answer: string }> }
  | { kind: 'text'; text: string }
  | { kind: 'code'; code: string; language: string | null }
```

- [ ] **Step 3: Implement serialize / hydrate helpers**

Create `answerPayload.ts` with helper functions such as:

```ts
export function hydrateDraftFromStep(item: StudentLabStepItem): LabAnswerDraft
export function buildAnswerPayloadJson(draft: LabAnswerDraft): string
export function buildCompatibilityAnswerText(draft: LabAnswerDraft): string
```

Rules:
- SINGLE → `A`
- MULTIPLE → `A,C`
- TRUE_FALSE → `TRUE` or `FALSE`
- FILL_BLANK → comma-joined answer order by blank index
- TEXT → raw text
- CODE → raw code

- [ ] **Step 4: Keep API call signatures aligned with the new types**

Ensure `saveStudentLabAnswer(...)` in `api/labs.ts` uses the expanded `SaveLabAnswerPayload` shape without introducing page-level Axios logic.

- [ ] **Step 5: Run frontend type verification**

Run: `npm run build`

Expected: PASS with no type errors from the upgraded student lab DTOs.

---

## Task 3: Build the student answer draft state manager

**Files:**
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/useLabAnswerDrafts.ts`
- Modify: `teaching-platform/frontend/src/views/student/LabSteps.vue`

- [ ] **Step 1: Replace string-only answer state with typed draft state**

Move student answer state out of `Record<number, string>` into a composable keyed by step ID.

Target API:

```ts
const {
  ensureDraft,
  getDraft,
  patchDraft,
  buildSavePayload,
  hydrateAllDrafts,
} = useLabAnswerDrafts()
```

- [ ] **Step 2: Support old and new saved answers during hydration**

Hydration order must be:
1. `answerPayloadJson`
2. fallback parse from `answerText`
3. empty state by question type

- [ ] **Step 3: Centralize save payload generation**

`buildSavePayload(stepId)` should always return both fields:

```ts
{
  answerText: buildCompatibilityAnswerText(draft),
  answerPayloadJson: buildAnswerPayloadJson(draft),
}
```

- [ ] **Step 4: Wire page save calls through the new draft manager**

Update `LabSteps.vue` so `saveCurrentAnswer()` and auto-save on navigation consume the composable instead of reading raw strings from local refs.

- [ ] **Step 5: Run frontend verification**

Run: `npm run build`

Expected: PASS before any major template/UI replacement starts.

---

## Task 4: Extract per-question answer renderer components

**Files:**
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/QuestionAnswerShell.vue`
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/ChoiceAnswerEditor.vue`
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/FillBlankInlineEditor.vue`
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/TextAnswerEditor.vue`
- Create: `teaching-platform/frontend/src/views/student/components/lab-answer/CodeLabAnswerEditor.vue`
- Modify: `teaching-platform/frontend/src/views/student/CodeAnswerEditor.vue`

- [ ] **Step 1: Create a shared shell for question header + body framing**

`QuestionAnswerShell.vue` should own the consistent wrapper for:
- question number / type / score
- prompt section
- answer section heading
- validation or helper text area

This keeps visual rhythm stable while different answer editors swap inside.

- [ ] **Step 2: Build one objective editor for single / multiple / judge**

`ChoiceAnswerEditor.vue` should render:
- radio-like rows for single choice
- checkbox-like rows for multiple choice
- two fixed rows for TRUE / FALSE

Clicking the row must update the typed draft, not a text field.

- [ ] **Step 3: Build the inline fill-blank renderer**

`FillBlankInlineEditor.vue` should:
- split prompt text by `【填空n】` token
- replace tokens with inline underlined inputs
- keep input order aligned with `blanks[].index`
- emit typed blank answers back to the draft manager

- [ ] **Step 4: Build focused text and code answer editors**

`TextAnswerEditor.vue` handles SHORT_ANSWER / TEXT.

`CodeLabAnswerEditor.vue` wraps the existing code editor behavior so code questions participate in the same shell and draft API.

Reuse `CodeAnswerEditor.vue` where practical rather than rewriting the editor from scratch.

- [ ] **Step 5: Run component compilation verification**

Run: `npm run build`

Expected: PASS with all new answer editor components compiling before the page layout is swapped.

---

## Task 5: Rebuild `LabSteps.vue` into a viewport-locked dual-scroll workbench

**Files:**
- Modify: `teaching-platform/frontend/src/views/student/LabSteps.vue`
- Modify: `teaching-platform/frontend/src/layout/index.vue`

- [ ] **Step 1: Attach the student page to the locked workbench height contract**

Reuse the existing layout strategy from locked workbench pages so the student answer page is bounded by viewport height instead of normal document flow.

The page root needs a stable modifier class comparable to:

```html
<section class="lab-steps-page workbench-page--locked">
```

Only do this if it matches the existing layout lock pattern already used elsewhere.

- [ ] **Step 2: Reorganize the template into left navigation + right answer pane**

The new top-level structure should conceptually be:

```html
<div class="lab-answer-workbench">
  <aside class="lab-answer-workbench__nav">...</aside>
  <main class="lab-answer-workbench__main">...</main>
</div>
```

Keep the experiment summary and bottom actions in the right pane rather than below the whole page.

- [ ] **Step 3: Make the left navigation body the only scroll owner on the left**

The progress summary stays visible, while the question list body receives `overflow-y: auto` and `min-height: 0`.

- [ ] **Step 4: Make the right answer body the only scroll owner on the right**

The right pane should use a column layout where:
- header/meta stays fixed in the pane
- answer body scrolls
- footer actions stay sticky at the bottom

- [ ] **Step 5: Replace old inline template branches with the new answer components**

Remove the current `el-input`-driven branches for SINGLE / MULTIPLE / FILL and mount the extracted components from Task 4.

- [ ] **Step 6: Preserve navigation and save semantics**

Ensure the existing behaviors still work after layout replacement:
- switching questions
- saving current question
- previous / next navigation
- summary editing
- submit lab

- [ ] **Step 7: Run frontend verification**

Run: `npm run build`

Expected: PASS with the new layout and answer components integrated.

---

## Task 6: Add full-stack regression coverage and manual verification

**Files:**
- Modify: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/`
- Verify: `teaching-platform/frontend/`
- Verify: `teaching-platform/backend/`
- Verify: `teaching-platform/.playwright-cli/` (artifacts)

- [ ] **Step 1: Add backend regression tests for structured save + legacy fallback**

Cover at least:
- structured payload save writes `answerJson`
- detail API exposes options/blanks/language
- historical legacy answer rows without `answerJson` still load cleanly

- [ ] **Step 2: Run backend tests under H2 test profile**

Run: `./mvnw.cmd test`

Expected: PASS.

- [ ] **Step 3: Run frontend production build**

Run: `npm run build`

Expected: PASS.

- [ ] **Step 4: Run local end-to-end verification against H2-backed app**

Recommended startup sequence:

Backend:
`cmd /c "set SPRING_PROFILES_ACTIVE=test && set SERVER_PORT=18080 && mvnw.cmd spring-boot:run"`

Frontend:
`cmd /c "set VITE_API_TARGET=http://127.0.0.1:18080 && npm run dev -- --host 127.0.0.1 --port 18081"`

Verify with Playwright CLI:
- open student lab steps page
- confirm browser does not own the main page scroll
- confirm left nav scrolls independently
- confirm right pane scrolls independently
- answer one single-choice question by clicking
- answer one multiple-choice question by clicking
- answer one fill-blank question through inline blanks
- answer one code question
- save current question
- refresh and verify structured answers rehydrate correctly
- submit the lab

- [ ] **Step 5: Capture and review generated artifacts**

Store screenshots / traces only under Playwright artifact directories such as `.playwright-cli/`.

- [ ] **Step 6: Final verification checkpoint**

Expected outcome:
- no frontend build errors
- backend tests pass
- dual-scroll layout works
- each question type shows the intended interaction model
- saved answers survive refresh via structured payload hydration

---

## Spec Coverage Check

- Dual-scroll viewport-locked workbench → Task 5
- Per-question interaction styles → Tasks 4 and 5
- Inline fill-blank inputs in prompt text → Task 4
- Structured answer draft state on frontend → Tasks 2 and 3
- Structured student metadata from backend detail API → Task 1
- `answerPayloadJson` persistence with `answerText` compatibility → Tasks 1, 2, and 3
- Real verification with build, backend tests, and Playwright → Task 6

## Placeholder Scan

- No `TBD`, `TODO`, or “implement later” placeholders remain.
- No task depends on “similar to previous task” shortcuts.
- Each verification step includes a concrete command and expected outcome.

## Type Consistency Check

- Student detail metadata uses `options`, `blanks`, `answerPayloadJson`, `editorLanguage` consistently across Tasks 1 and 2.
- Frontend draft naming uses `LabAnswerDraft`, `buildAnswerPayloadJson`, and `buildCompatibilityAnswerText` consistently across Tasks 2 and 3.
- Backend save path consistently refers to `answerText` + `answerPayloadJson` → `answerJson` across Tasks 1 and 6.
