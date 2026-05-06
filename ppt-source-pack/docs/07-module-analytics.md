# 07 关键模块深挖：统计分析

## 1. 模块目标

统计分析模块的目标，不是简单显示几张图，而是把实验、作业、考试和查重产生的业务数据转化为教师决策支持与学生反馈。

## 2. 接口入口

`AnalysisController` 提供了教师与学生两侧的分析接口：

### 教师侧

- `GET /api/v1/analysis/dashboard`
- `GET /api/v1/analysis/teacher-overview`

### 学生侧

- `GET /api/v1/analysis/my-scores`
- `GET /api/v1/analysis/my-score-overview`
- `GET /api/v1/analysis/student-dashboard`

## 3. 服务层能力

`AnalysisService` 已经明确实现了以下聚合逻辑：

- 教学活动总数
- 待批改任务数
- 平均成绩
- 高风险查重数量
- 最近 7 天趋势统计
- 班级对比
- 按业务类型分解成绩
- 学生个人成绩总览与完成进度

## 4. 数据来源

统计分析依赖的不是单一表，而是多表聚合：

- `lab` / `lab_submission`
- `homework` / `homework_submission`
- `exam` / `exam_submission`
- `score_record`
- `plagiarism_task`
- `class_room`

## 5. 页面价值

### 5.1 教师视角

- 看见整体工作量
- 看见待批改任务压力
- 看见班级平均表现和趋势变化
- 看见高风险查重对象

### 5.2 学生视角

- 看见待完成任务
- 看见最近成绩与反馈
- 看见分类型表现与完成进度

## 6. 答辩建议表达

统计分析模块体现了系统从“事务处理平台”向“教学反馈平台”的扩展能力，是本系统体系结构价值最明显的模块之一。
