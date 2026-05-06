# 将当前素材包接入本地 `ppt-master` 的实施方案

本文档用于说明如何把当前 `ppt-source-pack/` 中已经整理好的内容，接入你本地的 `ppt-master` 仓库：

- `C:/Users/15030/Desktop/opencode/日常使用/ppt测试文件夹/ppt-master`

目标不是一步到位生成最终成品，而是先得到一份结构正确、技术图可替换、后续容易精修的 PPT 初稿。

---

## 1. 当前已知的 `ppt-master` 工作方式

根据你本地仓库与官方说明，`ppt-master` 的典型工作流是：

1. 准备源内容
2. 在 `projects/` 下初始化项目
3. 导入 `sources/`
4. 确认模板 / 设计规格
5. 生成 SVG 页面
6. 导出为可编辑 PPTX

关键入口说明：

- 项目容器目录：`projects/`
- 项目初始化脚本：`skills/ppt-master/scripts/project_manager.py`
- 导出脚本：`skills/ppt-master/scripts/svg_to_pptx.py`
- 图像生成脚本：`skills/ppt-master/scripts/image_gen.py`

---

## 2. 本次最推荐的接入策略

不要直接把整个素材包目录原样丢给 `ppt-master`，而是采用 **“单项目、单主输入文档、图片分组接入”** 的方式。

### 推荐方案

在 `ppt-master/projects/` 下创建一个独立项目，例如：

- `projects/teaching-platform-defense/`

然后只把以下内容作为首轮生成输入：

### 主输入文件

- `ppt-source-pack/ppt-master-input.md`

### 技术图素材

- `ppt-source-pack/exports/*.png`

### 页面截图占位素材

- `design/screenshots/` 中计划放进 PPT 的几张图

也就是说，首轮生成时应以：

- 一份主 markdown
- 一批技术图 png
- 一批界面占位图

作为输入，而不是让 `ppt-master` 自己重新“理解整个仓库”。

---

## 3. 推荐的本地项目目录结构

建议在 `ppt-master/projects/teaching-platform-defense/` 中形成如下结构：

```text
projects/
└─ teaching-platform-defense/
   ├─ sources/
   │  └─ ppt-master-input.md
   ├─ images/
   │  ├─ architecture-overview.png
   │  ├─ er-core-domain.png
   │  ├─ swimlane-lab-process.png
   │  ├─ swimlane-homework-process.png
   │  ├─ swimlane-exam-process.png
   │  ├─ auth-routing-flow.png
   │  ├─ layered-pattern-overview.png
   │  ├─ unified-response-pattern.png
   │  ├─ component-auth-analytics-answering.png
   │  ├─ refined-login.png
   │  ├─ main-teacher-dashboard.png
   │  ├─ refined-student-dashboard.png
   │  ├─ missing-student-lab-detail-answer.png
   │  ├─ main-student-exam-taking-page.png
   │  └─ main-teacher-analytics.png
   ├─ templates/
   ├─ svg_output/
   ├─ svg_final/
   ├─ notes/
   └─ design_spec.md
```

---

## 4. 为什么建议这样接入

### 原因 1：减少模型重新理解成本

我们已经在 `teaching-platform` 仓库里完成了内容建模、图表梳理、页序设计。如果再让 `ppt-master` 从零阅读整个项目目录，它很容易跑偏，重新做一套“看起来像 PPT，但不是你想要的答辩逻辑”的结构。

### 原因 2：让 AI 负责排版，不负责猜事实

技术事实、数据库关系、业务流程和模块架构这些内容，已经在素材包中被明确整理好了。`ppt-master` 更适合负责：

- 版式
- 层次
- 风格
- 页面节奏
- 图文布局

而不是负责重新发明你的技术内容。

### 原因 3：方便后续替换占位图

界面图当前有一部分仍然是占位图或原型图。把它们独立放进 `images/` 后，后续只需要替换图片文件，不需要重写主输入文档。

---

## 5. 首轮生成时的模板建议

这次是课程答辩型技术汇报，不建议选过于商业化或花哨的模板。

### 推荐优先级

1. **无模板 / 自由设计，但风格目标明确为 Academic / Tech / Consulting-lite**
2. **科技蓝商务**（如果你想要更稳、更规整的商务科技风）
3. **重庆大学**（如果你想要更偏学校答辩氛围）

### 我的推荐

如果你这次主要面对的是课程老师，而不是企业汇报场景，我更推荐：

- **优先选择：无模板，但约束成学术答辩风格**

原因：

- 更容易容纳 ER 图、流程图、架构图
- 不容易被模板本身压住内容
- 方便你后续替换学校相关元素或课程信息

如果你希望第一版就更整洁保守，那就选：

- **科技蓝商务**

---

## 6. 首轮生成时的设计规格建议

当 `ppt-master` 进入设计规格确认阶段时，建议你沿用下面这组设定：

### 推荐 Eight Confirmations 内容

- **Canvas format**: PPT 16:9
- **Page count**: 14 页左右
- **Target audience**: 软件设计与体系结构课程老师 / 答辩评审
- **Style objective**: 学术答辩 + 技术架构型汇报
- **Color scheme**: 深蓝 + 白 + 少量橙/青强调色
- **Icon usage**: 少量线性图标，避免过多装饰
- **Typography**: 中文清晰优先，标题有力量感，正文可读性优先
- **Image strategy**: 技术图优先；界面截图次之；AI 生图仅用于封面或章节过渡页

---

## 7. 首轮生成时哪些页最适合直接保留现有图

以下页面适合直接使用当前 `ppt-source-pack/exports/*.png`：

- 系统总体架构
- 核心 ER 图
- 三条泳道图
- 权限协作图
- 分层架构图
- 统一响应模式图
- 关键模块关系图

这些图本身已经适合放入 PPT，不需要 `ppt-master` 再“生成同类技术图”。

---

## 8. 哪些页应保留为“图片占位后续替换”

以下页面建议首轮先用占位图，后续你再手动替换：

- 登录页
- 教师工作台
- 学生工作台
- 实验作答页
- 考试作答页
- 教师分析页
- 代码结构截图
- `SecurityConfig` / `ApiResponse` / `request.ts` / `ExamScoringService` 的局部代码截图

原因是这些页后续很可能需要你补“更像证据”的图片，而不是只保留现在的原型或示意图。

---

## 9. 不建议让 `ppt-master` 首轮自由发挥的部分

首轮不要让它自己重做：

- ER 图
- 业务泳道图
- 系统总体架构图
- 设计模式关系图

因为这些图现在已经由你当前仓库的真实内容决定了，AI 若自由重做，很容易生成一版“看起来像图，但不够贴项目实际”的内容。

最稳的做法是：

- 这些图直接作为现成图片使用
- `ppt-master` 负责排版、说明文字和整体风格统一

---

## 10. 推荐的实际接入顺序

### 第一步：在 `ppt-master/projects/` 下初始化项目

建议项目名：

- `teaching-platform-defense`

### 第二步：把主输入文档放进 `sources/`

源文件主入口使用：

- `ppt-source-pack/ppt-master-input.md`

### 第三步：把技术图和界面图放进 `images/`

分两类放：

- 技术图：来自 `ppt-source-pack/exports/*.png`
- 界面占位图：来自 `design/screenshots/*.png`

### 第四步：首轮先生成“结构正确”的初稿

目标不是一步到位，而是先检查：

- 页序是否正确
- 图文比例是否合适
- 技术图是否被缩太小
- 标题层级是否稳定

### 第五步：二轮替换图片与细修

二轮重点再处理：

- 最新截图替换
- 代码截图替换
- 必要的排版微调
- 录屏页 / 视频页补充

---

## 11. 结论

你现在最好的做法不是重新写内容，而是：

- 以 `ppt-source-pack/ppt-master-input.md` 作为唯一主文案入口
- 以 `ppt-source-pack/exports/*.png` 作为技术图入口
- 以 `design/screenshots/*.png` 作为首轮界面占位入口
- 在 `ppt-master/projects/teaching-platform-defense/` 中组织一次干净的首轮生成

这样做的好处是：

- 初稿可控
- 后续替换方便
- 不会让 AI 重复理解整个项目导致跑偏
