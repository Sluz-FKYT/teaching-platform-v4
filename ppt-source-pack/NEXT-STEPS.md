# PPT 素材包下一步使用说明

当前 `ppt-source-pack/` 已经具备较完整的内容层与图片层，可直接进入 PPT 生成阶段。

## 1. 最核心的总入口文件

- `ppt-master-input.md`
  - 用途：后续喂给 `ppt-master` 的主输入文档
  - 内容：已经按 Slide 1 ~ Slide 14 组织好标题、上屏要点、图片建议与备注

## 2. 正式页稿文档

- `docs/12-architecture-and-pattern-pages.md`
  - 主架构页 + 设计模式页
- `docs/13-database-ui-summary-pages.md`
  - 数据库页 + ER 图页 + 界面展示页 + 总结页

## 3. 提纲与映射

- `docs/10-slide-outline-seed.md`
  - 当前 PPT 逐页提纲
- `docs/11-asset-mapping.md`
  - 每页对应文档与图片映射表

## 4. 图片素材目录

- `exports/`
  - 已导出的 PNG / SVG 技术图
- `design/screenshots/`
  - 当前可用于界面展示页的占位截图

## 5. 推荐生成顺序

1. 以 `ppt-master-input.md` 为主输入
2. 优先使用 `exports/*.png` 作为技术图素材
3. 界面展示页暂时使用 `design/screenshots/*.png`
4. 后续再将占位截图替换为最新运行截图或录屏封面

## 6. 当前已完成范围

- 项目背景与目标
- 系统总体架构
- 数据库设计总览
- 核心 ER 图
- 业务流程与泳道图
- 权限模块
- 答题模块
- 统计分析模块
- 界面展示与完成度
- 主架构页
- 设计模式页
- 总结页

## 7. 后续最自然的动作

后续建议直接进入：

- 方案 A：基于 `ppt-master-input.md` 生成 PPT 初稿
- 方案 B：先补最新运行截图 / 代码截图，再生成 PPT
