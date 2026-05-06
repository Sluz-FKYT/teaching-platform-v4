# Lab Metadata and Guidance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade lab metadata from a single description field to requirement + content + experiment type + guidance file binding, and expose the guidance download path to students without violating the legacy `t_experiment` schema constraint.

**Architecture:** Keep the database anchored to the legacy `t_experiment` column definitions and only rely on additive columns already introduced by migration. Extend the backend lab DTO/service/view pipeline first so requirement/content/type/material metadata are real API fields, then upgrade the teacher lab management page to create/edit/display them, and finally expose the new metadata and download action on the student lab detail page by reusing the existing materials upload/download chain.

**Tech Stack:** Vue 3 `<script setup>`, Element Plus, existing Axios wrapper in `frontend/src/utils/request.ts`, teacher/student lab views under `frontend/src/views/**/Lab*.vue`, Spring Boot 3.3 lab/material modules, Flyway migrations, H2 `test` profile, Maven Wrapper, frontend `npm run build`.

---

## File Structure

### Modify
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/dto/LabRequests.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/entity/Lab.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- `teaching-platform/backend/src/main/resources/db/migration/V9__experiment_module_redesign.sql`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabServiceTests.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabTeacherControllerTests.java`
- `teaching-platform/frontend/src/types/lab.ts`
- `teaching-platform/frontend/src/api/labs.ts`
- `teaching-platform/frontend/src/api/materials.ts` (only if a tiny helper is needed; otherwise leave untouched)
- `teaching-platform/frontend/src/views/teacher/Labs.vue`
- `teaching-platform/frontend/src/views/student/LabDetail.vue`

### Verify
- `teaching-platform/backend/`
- `teaching-platform/frontend/`

---

## Task 1: Lock the database and backend entity contract to the legacy experiment schema

**Files:**
- Modify: `teaching-platform/backend/src/main/resources/db/migration/V9__experiment_module_redesign.sql`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/entity/Lab.java`
- Test: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabServiceTests.java`

- [ ] **Step 1: Keep the failing legacy-schema compatibility test in place**

Use the existing `experimentTablePreservesLegacyColumnsAndOnlyAddsNewColumns()` test in `LabServiceTests` as the red test proving that `t_experiment` must still expose:

```text
EXPERIMENT_ID
EXPERIMENT_NO (INTEGER)
EXPERIMENT_NAME
EXPERIMENT_TYPE
INSTRUCTION_TYPE (CHAR/VARCHAR)
EXPERIMENT_REQUIREMENT
EXPERIMENT_CONTENT
STATE
```

- [ ] **Step 2: Ensure `V9` only adds columns instead of redefining legacy ones**

`V9__experiment_module_redesign.sql` must treat `t_experiment` as a legacy table and only use `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` for the modern additions:

```sql
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS material_id BIGINT NULL;
ALTER TABLE t_experiment ADD COLUMN IF NOT EXISTS summary_required BOOLEAN NOT NULL DEFAULT FALSE;
```

Do not rename `experiment_id`, do not change `experiment_no` to varchar, and do not convert `instruction_type` to int.

- [ ] **Step 3: Align the `Lab` entity with the real table**

Map the entity back to legacy-compatible columns:

```java
@Id
@Column(name = "experiment_id")
private Long id;

@Column(name = "experiment_no", nullable = false)
private Integer experimentNo;

@Column(name = "instruction_type", length = 10)
private String instructionType = "1";
```

And keep additive fields (`status`, `material_id`, `summary_required`, etc.) mapped as extra columns.

- [ ] **Step 4: Keep default initialization compatible with legacy column types**

When a new lab is created, default values should stay compatible with old column shapes:

```java
if (experimentNo == null || experimentNo <= 0) {
    experimentNo = Math.toIntExact(id);
}
if (instructionType == null || instructionType.isBlank()) {
    instructionType = "1";
}
```

- [ ] **Step 5: Run the schema compatibility test to verify green**

Run:

```bash
./mvnw.cmd -Dtest=LabServiceTests#experimentTablePreservesLegacyColumnsAndOnlyAddsNewColumns test
```

Expected: PASS.

---

## Task 2: Extend backend lab DTOs and views for requirement/content/type/guidance metadata

**Files:**
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/dto/LabRequests.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- Test: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabServiceTests.java`
- Test: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabTeacherControllerTests.java`

- [ ] **Step 1: Add request fields for experiment content and type**

Upgrade create/update requests from:

```java
title, description, classId, status, materialId, scoreVisibilityMode, summaryRequired
```

to include:

```java
String experimentContent,
Integer experimentType,
```

while keeping `description` as the API field for experiment requirement.

- [ ] **Step 2: Stop collapsing requirement and content into one field**

Update `LabService.applyLab(...)` and the entity write path so:

- `description` writes `experiment_requirement`
- `experimentContent` writes `experiment_content`
- `experimentType` writes `experiment_type`
- `materialId` continues to write `material_id`

Do not keep the old side effect where setting description overwrites experiment content.

- [ ] **Step 3: Return lab metadata to teacher-facing views**

`toTeacherLabView(...)` should return at least:

```java
"description",          // requirement
"experimentContent",
"experimentType",
"materialId",
```

and, if easy and stable, also the bound material title/file name.

- [ ] **Step 4: Return lab metadata to student-facing detail views**

`getStudentLabDetail(...)` / student lab views should return:

```java
"description",          // requirement
"experimentContent",
"experimentType",
"materialId",
```

and the minimal download metadata needed by the frontend (recommended: material title or file name if already easy to resolve).

- [ ] **Step 5: Add failing tests for the new response fields**

Cover at least:
- teacher lab list/detail includes `experimentContent` and `experimentType`
- student lab detail includes `experimentContent`, `experimentType`, and `materialId`
- requirement and content no longer mirror each other automatically

- [ ] **Step 6: Run focused backend verification**

Run:

```bash
./mvnw.cmd "-Dtest=LabServiceTests,LabTeacherControllerTests,LabStudentControllerTests" test
```

Expected: PASS.

---

## Task 3: Upgrade frontend lab types and API contracts

**Files:**
- Modify: `teaching-platform/frontend/src/types/lab.ts`
- Modify: `teaching-platform/frontend/src/api/labs.ts`

- [ ] **Step 1: Extend lab types with the new metadata fields**

Add to the relevant lab interfaces:

```ts
experimentContent?: string
experimentType?: number | null
materialId?: number | null
materialName?: string | null
materialFileName?: string | null
```

Keep `description` but treat it as the requirement field in page usage.

- [ ] **Step 2: Extend create/update payload contracts**

Upgrade `CreateLabPayload` / `UpdateLabPayload` to include:

```ts
experimentContent: string
experimentType: number | null
materialId?: number | null
```

- [ ] **Step 3: Add a shared frontend experiment-type label helper**

Define a reusable mapping such as:

```ts
export const LAB_EXPERIMENT_TYPE_LABELS: Record<number, string> = {
  1: '基础实验',
  2: '验证实验',
  3: '综合实验',
  4: '设计实验',
}
```

This should live in `types/lab.ts` or a nearby lab helper if that keeps the file cleaner.

- [ ] **Step 4: Verify the frontend type layer compiles**

Run:

```bash
npm run build
```

Expected: PASS before UI files are modified.

---

## Task 4: Upgrade teacher lab create/edit form and list display

**Files:**
- Modify: `teaching-platform/frontend/src/views/teacher/Labs.vue`

- [ ] **Step 1: Expand the teacher lab form model**

The reactive form should move from:

```ts
{ title, description, classId, status, materialId, summaryRequired }
```

to:

```ts
{
  title,
  description,        // requirement
  experimentContent,
  experimentType,
  classId,
  status,
  materialId,
  summaryRequired,
}
```

- [ ] **Step 2: Add experiment type selector to the dialog**

Use an `el-select` bound to integer values `1/2/3/4`, but display the mapped Chinese labels.

- [ ] **Step 3: Split “实验说明” into two real fields**

Replace the old single textarea with:
- 实验要求 (`description`)
- 实验内容 (`experimentContent`)

The labels shown to the teacher must match those names explicitly.

- [ ] **Step 4: Add guidance file upload/binding UI**

Within the dialog, provide:
- current bound file display
- upload button
- replace button (same upload action)
- optional clear action

The upload flow must reuse the existing materials upload API and write the returned material ID into `createForm.materialId`.

- [ ] **Step 5: Update edit-mode hydration**

`openEditDialog(row)` must hydrate:
- `description`
- `experimentContent`
- `experimentType`
- `materialId`
- displayed material file state

- [ ] **Step 6: Update teacher lab list display**

Each row/card should show:
- experiment type label
- requirement summary
- whether guidance is bound

Do not leave the UI reading as if `description` were still just “实验说明”.

- [ ] **Step 7: Run frontend verification**

Run:

```bash
npm run build
```

Expected: PASS with the upgraded teacher lab page.

---

## Task 5: Add student lab detail metadata display and guidance download entry

**Files:**
- Modify: `teaching-platform/frontend/src/views/student/LabDetail.vue`

- [ ] **Step 1: Show experiment requirement/content/type in the student detail header area**

Upgrade the page so it clearly distinguishes:
- 实验类型
- 实验要求
- 实验内容

Do not continue rendering only a single `description` paragraph as the entire experiment context.

- [ ] **Step 2: Add the guidance download button**

If the lab has a bound material/file, show a button labeled:

```text
下载实验指导
```

If no guidance file is bound, do not show a fake disabled action unless it improves clarity.

- [ ] **Step 3: Reuse the materials download path rather than inventing a new lab download API**

Use the existing materials download behavior the repo already trusts.

- [ ] **Step 4: Preserve the existing student task flow**

The current “开始作答 / 继续作答 / 查看作答与反馈” flow must remain intact. The metadata block and download button are enhancements, not a redesign of the task path.

- [ ] **Step 5: Run frontend verification**

Run:

```bash
npm run build
```

Expected: PASS with the upgraded student lab detail page.

---

## Task 6: Final regression verification

**Files:**
- Verify: `teaching-platform/backend/`
- Verify: `teaching-platform/frontend/`

- [ ] **Step 1: Run backend lab regression tests**

Run:

```bash
./mvnw.cmd "-Dtest=LabServiceTests,LabStudentControllerTests,LabTeacherControllerTests" test
```

Expected: PASS.

- [ ] **Step 2: Run frontend production build**

Run:

```bash
npm run build
```

Expected: PASS.

- [ ] **Step 3: Manually verify the teacher create/edit flow**

Check:
- create lab with requirement/content/type/guidance
- edit lab and confirm values are rehydrated
- list display reflects type + guidance state

- [ ] **Step 4: Manually verify the student detail flow**

Check:
- student sees requirement/content/type
- guidance download button appears when bound
- clicking the guidance button downloads the uploaded file

- [ ] **Step 5: Database constraint checkpoint**

Confirm the implementation did not reintroduce any migration that changes legacy `t_experiment` column names/types. The database must still satisfy the “only add, never modify old columns” rule.

---

## Spec Coverage Check

- Teacher create/edit split into requirement + content → Task 4
- Experiment type integer code with frontend labels → Tasks 2, 3, 4, 5
- Guidance upload via materials/materialId → Tasks 2 and 4
- Teacher list display updates → Task 4
- Student detail metadata + guidance download → Task 5
- Database legacy-schema constraint remains enforced → Tasks 1 and 6

## Placeholder Scan

- No `TBD`, `TODO`, or “implement later” placeholders remain.
- No task says “similar to previous task” without concrete instructions.
- Verification steps include explicit commands and expected outcomes.

## Type Consistency Check

- `description` is treated consistently as experiment requirement.
- `experimentContent`, `experimentType`, and `materialId` are used consistently across backend DTOs, service views, and frontend payload/types.
- Experiment guidance remains a single `materialId` binding throughout the plan; no hidden multi-file model is introduced.
