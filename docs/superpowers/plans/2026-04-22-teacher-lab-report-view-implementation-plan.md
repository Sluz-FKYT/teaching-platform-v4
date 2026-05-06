# Teacher Lab Report View Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a teacher-side “实验报告” button on the existing lab report queue so it opens a pure report page in a new tab, backed by a new backend aggregation endpoint that renders the latest submission for a given `labId + studentId` pair.

**Architecture:** Keep the existing grading queue and grading detail pages unchanged. Extend the backend lab module with a dedicated read-only report-view DTO and endpoint that resolves the latest submission for a teacher-owned lab and student, then add a standalone frontend route/page outside the teacher layout shell to render the report as a plain document. Reuse the existing queue page only as the launch point by adding a new action button that opens the document page via `window.open`.

**Tech Stack:** Vue 3 `<script setup>`, Vue Router, Element Plus button usage on the existing queue page, typed API contracts in `frontend/src/api/labs.ts` and `frontend/src/types/lab.ts`, Spring Boot 3.3 controller/service/repository layers in the `lab` domain, Maven Wrapper tests, frontend `npm run build`.

---

## File Structure

### Create
- `teaching-platform/frontend/src/views/teacher/LabReportViewPage.vue`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabReportViewControllerTests.java`

### Modify
- `teaching-platform/frontend/src/router/index.ts`
- `teaching-platform/frontend/src/api/labs.ts`
- `teaching-platform/frontend/src/types/lab.ts`
- `teaching-platform/frontend/src/views/teacher/LabReports.vue`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/controller/LabController.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/dto/LabRequests.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/repository/LabSubmissionRepository.java`
- `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabServiceTests.java`

### Verify
- `teaching-platform/backend/`
- `teaching-platform/frontend/`

---

## Task 1: Add backend latest-submission report-view query and endpoint

**Files:**
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/dto/LabRequests.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/controller/LabController.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/repository/LabSubmissionRepository.java`
- Modify: `teaching-platform/backend/src/main/java/com/opencode/teachingplatform/lab/service/LabService.java`
- Test: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabServiceTests.java`
- Test: `teaching-platform/backend/src/test/java/com/opencode/teachingplatform/lab/LabReportViewControllerTests.java`

- [ ] **Step 1: Write the failing service test for latest-submission selection**

Add a focused test in `LabServiceTests.java` that creates:

```java
// same teacher-owned lab
// same student
// two submissions with different created/submitted times or ids
// only the newest submission should be reflected in report view
```

The assertion must prove all of these:

```java
assertThat(reportView.labId()).isEqualTo(lab.getId());
assertThat(reportView.studentId()).isEqualTo(student.getId());
assertThat(reportView.totalScore()).isEqualTo(newestSubmission.getTotalScore());
assertThat(reportView.steps()).extracting("answerText").containsExactly("newest answer");
```

- [ ] **Step 2: Write the failing controller test for the new endpoint**

Create `LabReportViewControllerTests.java` and add a test that calls:

```java
mockMvc.perform(get("/api/v1/teacher/labs/{labId}/report-view/{studentId}", labId, studentId)
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))))
```

Expected assertions:

```java
.andExpect(status().isOk())
.andExpect(jsonPath("$.data.labId").value(labId))
.andExpect(jsonPath("$.data.studentId").value(studentId))
.andExpect(jsonPath("$.data.courseName").value("软件设计与体系结构"))
.andExpect(jsonPath("$.data.steps.length()").value(1));
```

- [ ] **Step 3: Add the request/route shape in controller and DTO layer**

Add a new controller endpoint in `LabController.java`:

```java
@GetMapping("/teacher/labs/{labId}/report-view/{studentId}")
@PreAuthorize("hasRole('TEACHER')")
public ApiResponse<?> getTeacherLabReportView(@PathVariable Long labId, @PathVariable Long studentId) {
    CurrentUser currentUser = SecurityUtils.currentUser();
    return ApiResponse.ok(labService.getTeacherLabReportView(currentUser, labId, studentId));
}
```

If a request record is useful for future extension, add a minimal `TeacherLabReportViewQuery` record in `LabRequests.java`, but do not over-design query objects for this GET route.

- [ ] **Step 4: Add repository support for newest submission lookup**

In `LabSubmissionRepository.java`, add a latest-submission query for one student in one lab. Prefer a Spring Data method if the entity timestamps already support sorting cleanly:

```java
Optional<LabSubmission> findFirstByLabIdAndStudentIdOrderByIdDesc(Long labId, Long studentId);
```

If the entity already has a better stable time column and existing tests prove it, use that column in the ordering. Do not add a new table or migration for this feature.

- [ ] **Step 5: Implement the service aggregation DTO and read path**

In `LabService.java`, add a dedicated method:

```java
public TeacherLabReportView getTeacherLabReportView(CurrentUser currentUser, Long labId, Long studentId)
```

It must:

1. load the lab and verify `createdBy == currentUser.id()`
2. resolve the newest submission for `(labId, studentId)`
3. load lab steps ordered by `stepNo`
4. load step answers for that latest submission
5. assemble a read-only DTO for the plain report page

The top-level DTO should include at least:

```java
Long labId,
Long studentId,
String courseName,
String reportTitle,
String reportDate,
String className,
String studentNo,
String studentName,
String labTitle,
String purpose,
String experimentContent,
Double totalScore,
String teacherComment,
String summaryText,
List<TeacherLabReportViewStep> steps
```

Use the following fixed values/rules:

```java
courseName = "软件设计与体系结构";
reportTitle = "南昌航空大学实验报告";
reportDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日"));
```

Map purpose from `lab.getDescription()` and experiment content from `lab.getExperimentContent()`.

- [ ] **Step 6: Keep grading-empty behavior aligned with the spec**

When the latest submission has no final score yet, the report DTO should return:

```java
totalScore = null;
```

Do not coerce to `0.0`, and do not inject “未批改” display text into the DTO.

- [ ] **Step 7: Run focused backend verification**

Run:

```bash
./mvnw.cmd "-Dtest=LabServiceTests,LabReportViewControllerTests" test
```

Expected: PASS.

---

## Task 2: Add frontend typed contract and shell-free report route

**Files:**
- Modify: `teaching-platform/frontend/src/router/index.ts`
- Modify: `teaching-platform/frontend/src/api/labs.ts`
- Modify: `teaching-platform/frontend/src/types/lab.ts`

- [ ] **Step 1: Extend the lab types with a dedicated plain-report DTO**

In `frontend/src/types/lab.ts`, add explicit types instead of reusing the grading-detail type:

```ts
export interface TeacherLabReportViewStep {
  stepId: number
  stepNo: number
  stepTitle: string
  content: string
  questionType?: LabQuestionType
  answerText?: string | null
}

export interface TeacherLabReportView {
  labId: number
  studentId: number
  courseName: string
  reportTitle: string
  reportDate: string
  className?: string | null
  studentNo?: string | null
  studentName?: string | null
  labTitle: string
  purpose?: string | null
  experimentContent?: string | null
  totalScore?: number | null
  teacherComment?: string | null
  summaryText?: string | null
  steps: TeacherLabReportViewStep[]
}
```

Keep the existing `LabReportItem` and `LabReportDetail` types unchanged for the grading flow.

- [ ] **Step 2: Add a typed API function for the pure report page**

In `frontend/src/api/labs.ts`, add:

```ts
export const getTeacherLabReportView = (labId: number | string, studentId: number | string) =>
  request.get<TeacherLabReportView>(`/teacher/labs/${labId}/report-view/${studentId}`)
```

Also extend the imports at the top to include `TeacherLabReportView`.

- [ ] **Step 3: Add a shell-free report route before changing any page code**

In `frontend/src/router/index.ts`, add a teacher-protected route that does **not** use `Layout` as its component wrapper. Follow the existing router file style and put the new route at top level:

```ts
{
  path: '/teacher/labs/:labId/report/:studentId',
  name: 'teacher-lab-report-view',
  component: () => import('@/views/teacher/LabReportViewPage.vue'),
  meta: { role: 'TEACHER', title: '实验报告' }
}
```

The global `beforeEach` guard already honors `meta.role`, so do not duplicate auth logic inside the page.

- [ ] **Step 4: Run frontend type-level verification through build later, not now**

Do not stop for a partial build yet. The route points to a page that does not exist until Task 3, so continue directly into the page implementation.

---

## Task 3: Build the pure teacher lab report page

**Files:**
- Create: `teaching-platform/frontend/src/views/teacher/LabReportViewPage.vue`
- Modify: `teaching-platform/frontend/src/types/lab.ts`
- Modify: `teaching-platform/frontend/src/api/labs.ts`

- [ ] **Step 1: Create the page component with route-param loading**

Use `<script setup lang="ts">` and load by `labId` + `studentId`:

```ts
const route = useRoute()
const loading = ref(false)
const detail = ref<TeacherLabReportView | null>(null)

const labId = computed(() => Number(route.params.labId))
const studentId = computed(() => Number(route.params.studentId))

const fetchDetail = async () => {
  loading.value = true
  try {
    detail.value = await getTeacherLabReportView(labId.value, studentId.value)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
```

If either param is invalid, render an empty/error state instead of guessing.

- [ ] **Step 2: Render a document-style template with no teacher-shell controls**

The template should be plain and document-like, not a dashboard. Use a top-level structure like:

```vue
<template>
  <div class="lab-report-view" v-loading="loading">
    <el-empty v-if="!loading && !detail" description="未获取到实验报告" />
    <article v-else-if="detail" class="lab-report-sheet">
      <!-- title -->
      <!-- meta grid -->
      <!-- purpose -->
      <!-- experiment content -->
      <!-- steps -->
      <!-- summary -->
    </article>
  </div>
</template>
```

Do not add page-header, sidebar, toolbar, back button, score editor, or any teacher management summary cards.

- [ ] **Step 3: Implement the exact display rules from the spec**

Render the following sections and labels:

```text
南昌航空大学实验报告
日期
课程名称：软件设计与体系结构
班级
实验名称
学号
姓名
指导教师评定
签名
同组人
一、实验目的
二、实验内容
三、实验步骤
四、实验小结
```

Display rules:

- `指导教师评定` shows `detail.totalScore` only when not `null` / `undefined`
- when no score exists, show empty text
- step answers preserve newlines with `white-space: pre-wrap`
- summary section shows `detail.summaryText || ''`

- [ ] **Step 4: Add minimal document styles only**

Add scoped styles that keep the page printable/document-like:

```css
.lab-report-view {
  min-height: 100vh;
  background: #fff;
  padding: 16px 20px 32px;
}

.lab-report-sheet {
  color: #1f2937;
  line-height: 1.75;
}

.lab-report-answer {
  white-space: pre-wrap;
  word-break: break-word;
}
```

Avoid shadows, summary cards, colored dashboard chips, and teacher-console visuals.

- [ ] **Step 5: Run frontend build after the page exists**

Run:

```bash
npm run build
```

Expected: PASS.

---

## Task 4: Add the “实验报告” launch action to the queue page

**Files:**
- Modify: `teaching-platform/frontend/src/views/teacher/LabReports.vue`
- Modify: `teaching-platform/frontend/src/router/index.ts`
- Modify: `teaching-platform/frontend/src/types/lab.ts`

- [ ] **Step 1: Add a launch helper on the existing queue page**

In `LabReports.vue`, add a helper near the other action handlers:

```ts
const openReportView = (row: LabReportItem) => {
  if (!row.labId || !row.studentId) {
    return
  }
  const href = router.resolve({
    name: 'teacher-lab-report-view',
    params: { labId: row.labId, studentId: row.studentId },
  }).href
  window.open(href, '_blank', 'noopener,noreferrer')
}
```

Use `router.resolve(...)` so the route stays consistent with router config.

- [ ] **Step 2: Add the new button into each row action group**

Inside the existing `.report-row__actions` block, insert:

```vue
<el-button plain @click="openReportView(row)">实验报告</el-button>
```

Place it alongside the existing grading buttons instead of replacing them.

- [ ] **Step 3: Verify the queue page still compiles with existing row data**

Check that `LabReportItem` already includes `labId` and `studentId` in `frontend/src/types/lab.ts`. If either is optional in the type, keep the runtime guard from Step 1 instead of forcing non-null assertions.

- [ ] **Step 4: Re-run frontend build for the integrated path**

Run:

```bash
npm run build
```

Expected: PASS.

---

## Task 5: Run backend + frontend verification for the complete flow

**Files:**
- Verify: `teaching-platform/backend/`
- Verify: `teaching-platform/frontend/`

- [ ] **Step 1: Run focused backend tests for the new report view path**

Run:

```bash
./mvnw.cmd "-Dtest=LabServiceTests,LabReportViewControllerTests" test
```

Expected: PASS.

- [ ] **Step 2: Run the frontend production build**

Run:

```bash
npm run build
```

Expected: PASS.

- [ ] **Step 3: Perform a minimal route sanity check in code review terms**

Before claiming done, confirm these code-level invariants in the changed files:

```text
LabReports.vue contains an “实验报告” button
router/index.ts contains the shell-free teacher report route
LabReportViewPage.vue contains no page-header / sidebar / grading controls
labs.ts contains getTeacherLabReportView(...)
LabController.java contains GET /teacher/labs/{labId}/report-view/{studentId}
```

- [ ] **Step 4: Do not expand scope into export/print/history work**

Explicitly stop after the read-only report view works. Do not add PDF export, print buttons, student-side access, or history-version switching in this implementation batch.

---

## Self-Review

### Spec coverage

- Queue-page “实验报告” button: covered in Task 4.
- New-tab behavior: covered in Task 4 Step 1.
- Shell-free pure report page: covered in Task 2 Step 3 and Task 3.
- Backend latest-submission aggregation by `labId + studentId`: covered in Task 1.
- Fixed course name and empty score behavior: covered in Task 1 Step 5-6 and Task 3 Step 3.
- Keep grading pages unchanged: enforced by Task 4 and Task 5 Step 4.

No uncovered spec requirements remain.

### Placeholder scan

No `TODO` / `TBD` placeholders remain.

### Type consistency

- Backend endpoint path is consistently `GET /teacher/labs/{labId}/report-view/{studentId}`.
- Frontend route name is consistently `teacher-lab-report-view`.
- Frontend API function is consistently `getTeacherLabReportView`.
- Frontend DTO is consistently `TeacherLabReportView`.

No naming contradictions remain.

---

Plan complete and saved to `docs/superpowers/plans/2026-04-22-teacher-lab-report-view-implementation-plan.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using `executing-plans`, batch execution with checkpoints

Which approach?
