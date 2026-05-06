# 06 关键模块深挖：答题与提交闭环

## 1. 模块目标

答题模块的核心不是“上传一个答案”这么简单，而是围绕学生任务执行、系统过程记录、教师评分反馈构建完整闭环。

## 2. 为什么重点讲实验与考试

实验与考试在仓库中具有更清晰的接口、状态和数据结构，适合作为“答题模块”的代表：

- 实验体现“分步骤作答 + 保存草稿 + 自动评分 + 教师评分”
- 考试体现“开始考试 + 倒计时 + 交卷 + 客观题自动评分 + 主观题教师评分”

## 3. 实验作答链路

### 3.1 接口入口

`LabController` 中与学生作答直接相关的接口包括：

- `GET /api/v1/student/labs`
- `GET /api/v1/student/labs/{id}`
- `POST /api/v1/student/labs/{id}/answers/{stepId}`
- `POST /api/v1/student/labs/{id}/submit`

### 3.2 服务层行为

`LabService` 明确实现了：

- 查询学生实验详情
- 保存步骤答案 `saveAnswer`
- 正式提交 `submitLab`
- 教师评分 `gradeReport`
- 成绩回写 `score_record`

### 3.3 数据库表

- `lab`
- `lab_step`
- `lab_submission`
- `lab_step_answer`
- `score_record`

## 4. 考试作答链路

### 4.1 接口入口

`ExamController` 中与学生考试相关的接口包括：

- `GET /api/v1/exams`
- `GET /api/v1/exams/{id}`
- `POST /api/v1/exams/{id}/start`
- `POST /api/v1/exams/{id}/submit`

教师评分相关接口包括：

- `GET /api/v1/teacher/exams/{id}/results`
- `GET /api/v1/teacher/exam-submissions/{id}`
- `POST /api/v1/teacher/exam-submissions/{id}/grade`

### 4.2 服务层行为

`ExamService` 明确支持：

- 考试列表按教师 / 学生双视角返回
- 学生开始考试时创建考试提交记录
- 对进行中考试返回剩余时间
- 交卷后写入答案与得分
- 教师查看提交详情并评分

### 4.3 数据库表

- `exam`
- `exam_question`
- `exam_submission`
- `exam_answer`
- `score_record`

## 5. 答辩建议表达

建议强调三点：

1. 系统不仅记录最终分数，也记录作答过程与中间状态。
2. 实验、作业、考试没有被粗暴统一，而是保留了各自业务语义。
3. 提交、批改、计分和分析能够形成可追踪的数据闭环。
