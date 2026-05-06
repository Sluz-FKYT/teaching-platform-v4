# 教学平台课程答辩 PPT 素材包

本目录用于集中沉淀后续课程答辩 PPT 所需的文字与图表源材料，目标不是直接替代 PPT，而是作为后续人工排版或 `ppt-master` 自动生成时的稳定输入。

## 使用边界

- **数据库事实真源**：`backend/src/main/resources/db/migration/`
- **页面与交互证据**：`design/` 下的截图、HTML 与设计说明
- **实现证据**：`backend/src/main/java/` 与 `frontend/src/` 中的真实模块代码
- **当前阶段约束**：`design/` 中页面素材只作为展示占位与结构参考，不作为数据库或业务事实真源

## 目录说明

- `docs/`：答辩文案底稿，按主题拆分
- `diagrams/`：PlantUML 源文件
- `exports/`：由 PlantUML 导出的 PNG / SVG 图片

## 推荐阅读顺序

1. `docs/01-project-overview.md`
2. `docs/02-system-architecture.md`
3. `docs/03-database-design.md`
4. `docs/04-processes.md`
5. `docs/05-module-auth.md`
6. `docs/06-module-answering.md`
7. `docs/07-module-analytics.md`
8. `docs/08-ui-placeholder-notes.md`

## 当前已准备内容

- 项目总览与答辩主线
- 系统架构与设计模式说明
- 数据库设计摘要与 ER 图
- 实验 / 作业 / 考试三条泳道流程图
- 权限、答题、统计分析三个关键模块说明
- 页面截图占位说明
