# 03 数据库设计摘要

## 1. 数据库真源

数据库结构以 `backend/src/main/resources/db/migration` 为唯一真源。其中：

- `V1__init_schema.sql`：定义核心业务表结构
- `V2__seed_data.sql`：提供演示账号、班级、实验、作业、考试、成绩等样例数据
- `V3__profile_and_dashboard_contracts.sql`：补强资料与工作台所需字段
- `V4__question_bank_teacher_scope.sql`：强化题库与教师归属关系
- `V5__qa_pending_grade_samples.sql`：补充待批改场景样例

## 2. 核心实体分组

### 2.1 用户与组织

- `sys_user`
- `class_room`
- `class_member`

### 2.2 教学资源

- `course_material`
- `question_bank`

### 2.3 实验闭环

- `lab`
- `lab_step`
- `lab_submission`
- `lab_step_answer`

### 2.4 作业闭环

- `homework`
- `homework_question`
- `homework_submission`
- `homework_answer`

### 2.5 考试闭环

- `exam`
- `exam_question`
- `exam_submission`
- `exam_answer`

### 2.6 横切能力

- `score_record`
- `plagiarism_task`
- `audit_log`

## 3. 设计特点

### 3.1 一类业务，一套主表 + 明细表

实验、作业、考试三条闭环都没有被强行压缩成一个“统一提交表”，而是分别设计了自己的主表、题目/步骤表、提交表和答案表。这样做更贴合各自业务状态机与评分方式。

### 3.2 评分与统计分离

提交表负责保存事务态信息，`score_record` 负责沉淀可供统计分析消费的评分结果，便于后续 dashboard 聚合。

### 3.3 查重能力独立建模

查重使用 `plagiarism_task` 独立建模，而不是把查重字段散落在所有业务表中，这样更利于复核与扩展。

## 4. 示例数据带来的答辩价值

`V2__seed_data.sql` 已经提供了完整演示链路：

- 教师账号 `t9001`
- 学生账号 `20260001`
- 班级 `SE2026-1`
- 已发布实验、作业、考试
- 已批改实验与作业
- 已提交待评分考试
- 成绩记录与查重任务

这说明系统不是空壳，而是具备真实业务闭环的可演示数据基础。

## 5. PPT 展示建议

- 数据库设计页不要直接堆所有字段
- 优先按领域分组展示表结构
- ER 图重点画关系，不强求把全部字段都放进去
- 讲表结构时，始终回到“支撑哪条业务闭环”
