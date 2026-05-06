# 01 项目总览

## 1. 项目定位

本项目是一个面向教师与学生双角色的一体化教学平台重构项目，当前阶段不是仅做视觉美化，而是以前端原型为导向，对前端页面、后端 API、数据库模型和测试链路进行协同重构。

## 2. 答辩主线

课程答辩建议围绕以下逻辑展开：

1. 业务复杂性决定需要体系结构设计，而不是简单 CRUD。
2. 系统通过前后端分离与分层架构承载多角色、多闭环业务。
3. 数据库设计为实验、作业、考试和统计分析提供真实数据支撑。
4. 权限、答题、统计分析三个关键模块体现了系统的架构价值。

## 3. 当前系统覆盖范围

- 公共基础：登录、个人资料、修改密码
- 教师端：班级、学生、资料、题库、实验、作业、考试、分析、查重
- 学生端：工作台、资料、实验、作业、考试、成绩

## 4. 证据边界

### 4.1 数据库事实

数据库设计与表结构以 `backend/src/main/resources/db/migration/` 为准，特别是：

- `V1__init_schema.sql`
- `V2__seed_data.sql`
- `V3__profile_and_dashboard_contracts.sql`
- `V4__question_bank_teacher_scope.sql`
- `V5__qa_pending_grade_samples.sql`

### 4.2 页面与流程证据

页面与展示逻辑以 `design/` 中的解释层材料为主：

- `design/02-screen-evidence-index.md`
- `design/03-screen-to-route-contract.md`
- `design/04-domain-and-api-impact-map.md`

### 4.3 实现证据

关键模块的真实接口与行为来自后端控制器、服务层与实体定义，例如：

- `auth/controller/AuthController.java`
- `lab/controller/LabController.java`
- `homework/controller/HomeworkController.java`
- `exam/controller/ExamController.java`
- `analysis/controller/AnalysisController.java`

## 5. 推荐在 PPT 中突出的问题

- 为什么教学平台需要多角色权限隔离
- 为什么实验 / 作业 / 考试不能抽象成一个过于简单的提交引擎
- 为什么统计分析需要聚合型后端接口，而不是仅靠前端拼接
- 为什么数据库结构必须围绕真实业务闭环设计
