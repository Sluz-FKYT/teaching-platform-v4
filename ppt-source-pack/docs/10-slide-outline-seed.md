# 10 PPT 逐页提纲（Seed）

## 第 1 页 封面

- 标题：教学平台重构项目体系结构设计与实现汇报
- 副标题：软件设计与体系结构课程作业
- 图片建议：后续可用 AI 背景图或系统相关视觉占位图

## 第 2 页 项目背景与目标

- 系统是面向教师与学生双角色的一体化教学平台
- 当前阶段为原型主导的全栈重构，而非单纯页面美化
- 核心挑战是多角色、多业务闭环与统计反馈
- 参考文档：`docs/01-project-overview.md`

## 第 3 页 系统总体架构

- 展示前端、后端、数据库、设计解释层之间关系
- 强调前后端分离与分层架构
- 配图：`exports/architecture-overview.png`
- 参考文档：`docs/02-system-architecture.md`

## 第 4 页 数据库设计总览

- 按用户组织、教学资源、实验、作业、考试、统计五组说明数据库结构
- 强调 migration 是数据库事实真源
- 参考文档：`docs/03-database-design.md`、`docs/13-database-ui-summary-pages.md`

## 第 5 页 核心 ER 图

- 展示核心实体关系
- 重点讲三条业务闭环与横切能力表
- 配图：`exports/er-core-domain.png`
- 参考文档：`docs/03-database-design.md`、`docs/13-database-ui-summary-pages.md`

## 第 6 页 业务流程总览

- 引出实验、作业、考试三条业务闭环
- 强调教师、学生、系统三方协作
- 参考文档：`docs/04-processes.md`

## 第 7 页 实验 / 作业 / 考试泳道图

- 可拆成 2 页，也可放一页三图缩略版
- 配图：
  - `exports/swimlane-lab-process.png`
  - `exports/swimlane-homework-process.png`
  - `exports/swimlane-exam-process.png`

## 第 8 页 权限模块深挖

- 登录、当前用户、改资料、改密码
- 前端路由守卫与后端 JWT + 方法级授权协同
- 配图：`exports/auth-routing-flow.png`
- 参考文档：`docs/05-module-auth.md`

## 第 9 页 答题模块深挖

- 实验与考试作为代表场景
- 保存、提交、批改、计分形成闭环
- 配图：`exports/component-auth-analytics-answering.png`
- 参考文档：`docs/06-module-answering.md`

## 第 10 页 统计分析模块深挖

- 教师 dashboard、teacher overview、学生成绩总览
- 强调从事务数据到决策反馈的转化
- 参考文档：`docs/07-module-analytics.md`

## 第 11 页 界面展示与完成度

- 放登录、教师工作台、实验步骤、考试作答、成绩分析等截图
- 当前用占位图，后续替换为新截图或录屏封面
- 参考文档：`docs/08-ui-placeholder-notes.md`、`docs/13-database-ui-summary-pages.md`

## 第 12 页 系统主架构设计

- 讲 B/S 架构、前后端分离、模块化单体、分层架构
- 配图：
  - `exports/architecture-overview.png`
  - `exports/code-structure-overview.png`
  - `exports/layered-pattern-overview.png`
- 参考文档：
  - `docs/02-system-architecture.md`
  - `docs/12-architecture-and-pattern-pages.md`

## 第 13 页 设计模式与工程实现模式

- 讲 Repository、DTO、门面式封装、策略化思想、统一响应包装
- 配图：
  - `exports/unified-response-pattern.png`
  - `exports/component-auth-analytics-answering.png`
  - `exports/auth-routing-flow.png`
- 参考文档：
  - `docs/09-architecture-pattern-placeholders.md`
  - `docs/12-architecture-and-pattern-pages.md`

## 第 14 页 总结

- 系统已形成真实前后端、真实数据库、真实权限、真实分析闭环
- 架构设计不是空泛概念，而是用于支撑复杂教学业务
- 后续可继续补足更多页面证据与交互演示
- 参考文档：`docs/13-database-ui-summary-pages.md`
