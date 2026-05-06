# 作业与考试共享题目能力设计

## 1. 目标

在 `teaching-platform` 当前前后端分离、按业务域分包的架构下，为 **homework** 与 **exam** 建立一套共享的题目能力方案，使二者在以下方面保持一致：

- 教师端题目配置方式
- 题型定义
- 题目快照结构
- 学生作答数据结构
- 调用现有 `grading` 模块的适配方式
- 自动评分 / 建议分 / 人工确认分的语义

同时，二者继续保留各自业务特性：

- homework 保留作业说明、附件、开始/截止时间、查重和作业提交流程
- exam 保留考试说明、开始/结束时间、考试时长、计时、开始考试 / 继续考试 / 交卷 / 结果可见性等考试语义

本次设计的重点不是重做评分内核，而是让 homework 与 exam 在“题目能力”上共用一条清晰的实现路径，并且直接复用仓库中已经存在的 `grading` 模块。

本次设计还建立在以下两个前提之上：

- 题库模块中已经存在教师端“配置题目”的成熟轮子
- 实验模块中已经存在学生端“完成题目 / 作答”的成熟轮子

因此，本次设计不是从零发明一套新的题目配置器和新的答题器，而是要求优先识别、抽取并复用现有成熟实现。

## 2. 非目标

- 不把 `lab / homework / exam` 合并成一个通用 activity 或 assignment 实体
- 不新建第二套评分模块，不替代现有 `backend/src/main/java/com/opencode/teachingplatform/grading`
- 不在第一版中重构实验模块的步骤工作台与实验步骤存储结构
- 不在第一版中统一实验、作业、考试的提交状态机
- 不在第一版中引入新的考试监考、切屏检测、随机组卷或 AI 题目生成能力
- 不在第一版中新增前端测试框架或重建整套教师端页面骨架
- 不从零重写一套与题库模块重复的题目配置器
- 不从零重写一套与实验模块重复的学生答题器

## 3. 范围边界

### 3.1 正式范围

本 spec 的正式范围仅包括：

- 教师端 homework / exam 题目配置能力统一
- homework / exam 后端题目快照与答案 payload 统一
- homework / exam 对现有 `grading` 模块的统一适配

### 3.2 参考范围

`lab` 不属于本次正式接入范围，但作为共享题目能力的参考基线使用，尤其参考以下能力：

- 结构化题型编辑思路
- 题目快照与评分结果的字段语义
- 教师端题目配置台中已验证的交互模式

### 3.3 后续扩展范围

如果本次 homework / exam 方案稳定，后续才评估 lab 中“标准题目部分”如何选择性接入共享能力层；实验要求、实验内容、步骤工作台、实验小结等实验外壳能力不在本次统一范围内。

## 4. 背景与现状

### 4.1 当前前端现状

homework 与 exam 都已经具备自己的教师与学生页面壳：

- `frontend/src/views/teacher/Homeworks.vue`
- `frontend/src/views/teacher/HomeworkSubmissions.vue`
- `frontend/src/views/student/Homeworks.vue`
- `frontend/src/views/student/HomeworkDetail.vue`
- `frontend/src/views/teacher/exams/List.vue`
- `frontend/src/views/teacher/exams/Results.vue`
- `frontend/src/views/student/exams/List.vue`
- `frontend/src/views/student/exams/Detail.vue`

但教师端题目配置目前没有统一：

- homework 在 `Homeworks.vue` 内嵌“题库题 ID + 内联题”的局部配置逻辑
- exam 在 `teacher/exams/List.vue` 内嵌题目池选择与分值排序逻辑

这意味着 homework / exam 都已经有“能用”的配置壳，但没有一套可复用的共享题目配置组件层。

同时，仓库里已经存在两类应被优先复用的现有资产：

- **题库侧配置题目能力**：题型编辑、题干 / 选项 / 答案 / 评分配置等编辑轮子已经存在，不应重新发明同类 UI 与交互
- **实验侧学生答题能力**：学生完成实验时的题目作答轮子已经存在，不应在 homework / exam 再从头造一套独立答题器

### 4.2 当前后端现状

当前仓库已经具备：

- `homework/` 业务域：作业创建、更新、挂题、提交、逐题评分、整份评分
- `exam/` 业务域：考试创建、更新、开始考试、交卷、自动评分、人工评分
- `grading/` 模块：统一评分能力中心

其中：

- homework 已有 `questionSnapshotJson` 思路
- exam 已有题目挂接表，但快照使用还不彻底
- homework 与 exam 调用评分时都已依赖 `grading`

因此，本次设计不需要再发明新的评分中心，而是需要统一题目能力输入输出和调用 `grading` 的边界。

## 5. 设计结论

### 5.1 总体结论

采用 **“两个独立业务壳 + 一套共享题目能力层 + 现有 grading 评分内核”** 的方案：

- `homework/`：保留作业业务壳
- `exam/`：保留考试业务壳
- `grading/`：继续作为唯一评分内核
- 新增一个轻量共享层，仅承载题目配置、题目快照、答案 payload 与 grading 适配逻辑

这个“共享题目能力层”优先通过**抽取和复用现有题库 / 实验模块里的成熟轮子**实现，而不是先设计全新组件再强行迁移旧实现。

### 5.2 共享的部分

共享层只负责：

- 统一题型枚举与字段映射
- 统一题目快照结构
- 统一教师端题目配置结果结构
- 统一学生作答 payload 结构
- 统一将题目与答案适配到 `grading` 模块的调用逻辑
- 统一评分结果在业务域内落库前的中间表示

### 5.3 不共享的部分

以下内容继续保留在各自业务域：

- homework 的附件、查重、截止时间、作业说明、提交流程
- exam 的考试窗口、时长、计时、开考、继续考试、交卷与成绩发布流程
- teacher 端业务页面壳与页面级流程文案
- submission 主表、状态机和最终结果聚合逻辑

## 6. 前端设计

### 6.1 目标

教师端 homework 与 exam 的题目配置不再各自维护一套逻辑，而是复用统一的共享组件层。

这里的“共享组件层”优先来源于当前题库模块已存在的配置题目能力，而不是新建一套平行实现。

### 6.2 组件边界

建议新增以下共享组件：

#### A. `QuestionEditorDialog`

建议路径：

- `frontend/src/components/question-config/QuestionEditorDialog.vue`

职责：

- 直接复用或抽取现有题库“新增题目弹窗”的能力
- 支持新增内联题或基于已有题目数据编辑
- 输出统一的题目配置结果结构

不负责：

- 直接感知 homework 或 exam 的业务字段
- 直接决定如何提交到某个业务接口

#### B. `QuestionPickerDialog`

建议路径：

- `frontend/src/components/question-config/QuestionPickerDialog.vue`

职责：

- 从题库中筛选题目
- 支持多选
- 为选中的题目配置排序、分值
- 输出统一的已配置题目列表

该组件如果可以由现有题库页中的表格、筛选和选题交互裁剪获得，应优先裁剪抽取，而不是全新实现。

#### C. `QuestionConfigList`

建议路径：

- `frontend/src/components/question-config/QuestionConfigList.vue`

职责：

- 展示当前业务下已配置题目
- 支持删除、排序调整、分值编辑、打开编辑弹窗

### 6.3 前端共享类型

建议新增：

- `frontend/src/types/question-config.ts`
- `frontend/src/types/question-answer.ts`

#### `ConfiguredQuestionItem`

该结构作为教师端共享题目配置的统一输出：

- `localId`
- `questionBankId`
- `sourceType`（`BANK` / `INLINE`）
- `questionType`
- `stem`
- `sortOrder`
- `score`
- `options`
- `answerJson`
- `scoringConfigJson`

#### `UnifiedQuestionAnswerPayload`

该结构作为学生端统一答题 payload：

- `questionRefId`
- `questionType`
- `answerText`
- `answerJson`
- `selectedOptions`
- `attachmentPath`

### 6.4 页面接入策略

#### homework 教师页

保留：

- 标题、班级、说明、附件、开始时间、截止时间等作业字段

替换：

- 当前页面内零散的题目接线表单，改为共享组件：
  - 新增题目 → `QuestionEditorDialog`
  - 从题库选题 → `QuestionPickerDialog`
  - 当前题目列表 → `QuestionConfigList`

这里的目标不是让 homework 自己重新实现一套题目编辑器，而是让 homework 页面成为“业务字段壳 + 复用题目配置器”的组合页面。

#### exam 教师页

保留：

- 标题、班级、开始时间、结束时间、时长、成绩可见性等考试字段

替换：

- 当前页面内嵌题目池 / 已选题列表逻辑，改为与 homework 一致的共享题目配置区

这里同样要求 exam 直接复用题库配置轮子，而不是延续当前页面内部自维护的一套题目池实现。

### 6.6 学生端复用原则

学生端 homework / exam 的答题交互设计，应优先复用实验模块中已经存在的答题轮子和数据组织方式，尤其是：

- 题型驱动的作答控件组织方式
- 题目切换 / 当前题上下文保持方式
- 结构化答案 payload 的生成方式

本次 spec 不要求 homework / exam 在页面布局上复刻实验工作台，但要求在“题目答题器核心能力”上优先复用实验模块现有实现，而不是重新写一套新的题型控件。

### 6.5 前端页面不做的事

- 不让共享弹窗直接请求 homework 或 exam 接口
- 不让共享弹窗感知考试时间窗、作业附件或业务状态
- 不在本次 spec 中要求把实验步骤页强行改造成和 homework / exam 完全一样的结构

## 7. 后端设计

### 7.1 总体原则

不新建评分模块，不替代 `grading`。后端只新增一个轻量共享层，用于整理 homework / exam 共同需要的题目快照、答案 payload 与 grading 适配。

同样地，不应从零新建与题库模块、实验模块重复的题目配置和答题语义；共享层要承担的是“抽公共模型与适配边界”，不是复制旧能力并平行存在。

### 7.2 共享层建议路径

建议新增：

- `backend/src/main/java/com/opencode/teachingplatform/assessment/model/`
- `backend/src/main/java/com/opencode/teachingplatform/assessment/service/`

这里的 `assessment` 仅表示“共享题目能力”，不是新的业务域。

### 7.3 共享模型

#### A. `QuestionSnapshot`

统一描述发布后稳定的题目快照，建议至少包含：

- `questionBankId`
- `sourceType`
- `questionType`
- `stem`
- `sortOrder`
- `score`
- `optionsJson`
- `answerJson`
- `scoringConfigJson`
- `reusableFromBank`

用途：

- homework 题目挂接
- exam 题目挂接
- 调用 `grading` 时的题目定义来源

#### B. `QuestionAnswerPayload`

统一描述学生针对某一道题的提交结果，建议至少包含：

- `questionRefId`
- `questionType`
- `answerText`
- `answerJson`
- `selectedOptions`
- `attachmentPath`

#### C. `GradingResultView`

统一描述从 `grading` 返回后、业务域正式落库前的中间结果，建议至少包含：

- `autoScore`
- `suggestedScore`
- `finalScore`
- `judgeDetail`
- `scoreSource`
- `correct`
- `normalizedAnswerJson`

### 7.4 共享服务

#### `QuestionSnapshotFactory`

职责：

- 从题库题构造快照
- 从内联题构造快照
- 避免 homework / exam 各自重复拼接快照 JSON

#### `AnswerPayloadNormalizer`

职责：

- 将单选、多选、判断、文本等不同前端输入收敛为统一 `QuestionAnswerPayload`

#### `GradingAdapter`

职责：

- 把 `QuestionSnapshot + QuestionAnswerPayload` 适配成 `grading` 模块需要的输入
- 调用现有 `grading` 模块
- 将结果转成统一 `GradingResultView`

不负责：

- 改变 submission 状态
- 决定业务何时允许评分
- 直接写入 homework / exam 表

## 8. homework 接入设计

### 8.1 保留内容

homework 继续保留：

- `Homework`
- `HomeworkSubmission`
- 查重逻辑
- 作业说明、附件、时间窗
- 教师整份作业评分与教师评语语义

### 8.2 接入方向

homework 的题目配置、题目快照和逐题答案落库，改为统一使用共享层生成和消费的结构。

第一版中，homework 需要至少达到：

- 教师端题目配置与 exam 一致
- 后端挂题与内联题快照结构一致
- 学生提交时的逐题答案 payload 一致
- 调用 `grading` 的适配路径一致

其中，学生逐题答案的组织方式优先借鉴实验模块已有实现，而不是让 homework 继续长期维持“页面内各自发明 payload”的状态。

### 8.3 本次不强制要求

- 不要求第一版立即支持实验级的 autosave / 过程日志
- 不要求第一版立即实现独立 homework 题目工作台页面
- 不要求第一版立即统一 homework 与 exam 的发布状态机

## 9. exam 接入设计

### 9.1 保留内容

exam 继续保留：

- `Exam`
- `ExamSubmission`
- 开始考试 / 继续考试 / 交卷 / 结果查看语义
- 开始时间、结束时间、durationMinutes
- 成绩可见性与成绩发布时间控制

### 9.2 接入方向

exam 的题目挂接与作答结构统一到共享层，但考试特有的时序与状态流保持在 `exam/` 域内。

第一版中，exam 需要至少达到：

- 教师端题目配置与 homework 共享同一套组件输出
- 发布时固化考试题目快照
- 交卷时使用统一答案 payload 适配 `grading`
- 评分结果字段映射与 homework 保持一致语义

其中，教师端题目配置优先复用题库现有轮子，学生端题型作答能力优先复用实验现有轮子，但考试计时、开始 / 继续 / 交卷流程仍由 exam 页面壳独立掌控。

### 9.3 考试特有校验仍留在 exam 域

以下逻辑不进入共享层：

- 是否允许开始考试
- 是否允许继续考试
- 时间窗是否合法
- 是否允许查看结果
- 是否需要人工确认后才开放成绩

## 10. 与 grading 模块的关系

### 10.1 评分模块定位

`backend/src/main/java/com/opencode/teachingplatform/grading` 继续作为唯一评分内核。

### 10.2 共享层与 grading 的依赖关系

- `assessment -> grading`
- `homework -> assessment`
- `exam -> assessment`

禁止：

- `grading -> homework`
- `grading -> exam`
- `grading` 感知 submission 状态机或业务流程

### 10.3 统一后的责任分配

`grading` 负责：

- 题型评分策略
- 评分上下文解释
- 评分结果计算

共享层负责：

- 题目快照与答案结构适配
- grading 输入输出桥接

业务域负责：

- 何时调用评分
- 如何落库
- 如何更新 submission 状态
- 如何聚合总分与结果视图

## 11. 数据与兼容策略

### 11.1 homework

在不破坏现有作业壳的前提下，逐步将旧的作业题目接线逻辑替换为统一快照结构。

### 11.2 exam

exam 需要优先补齐“快照化”能力，避免题库变更影响历史考试。

### 11.3 lab

lab 暂不迁移数据结构，不参与本次兼容改造。

## 12. 风险与约束

### 12.1 过度抽象风险

如果把考试时间窗、作业附件、实验要求等业务语义硬塞进共享层，会让共享层失控并反向污染三个业务域。

### 12.2 题目快照不彻底风险

如果 exam 继续依赖实时题库读取而不是稳定快照，后续修改题库会影响历史考试回看与评分一致性。

### 12.3 前端共享组件耦合过深风险

如果共享弹窗直接感知 homework / exam 的业务字段与页面状态，组件很快会退化为“看似共享、实际不可复用”的伪抽象。

### 12.4 枚举与契约不一致风险

当前前后端在成绩可见性等枚举上已经存在不一致苗头；本次统一时必须同步梳理 homework / exam 的前后端契约，避免共享层建立在不一致类型之上。

### 12.5 重复造轮子风险

如果忽视题库模块已经存在的配置题目轮子和实验模块已经存在的学生答题轮子，重新从零设计一套共享组件，会产生：

- 旧实现和新实现长期并存
- 同类题型交互行为不一致
- plan 阶段范围膨胀
- 后续维护成本翻倍

因此，本次方案必须把“复用现有成熟轮子”作为显式约束写入实施方向。

## 13. 实施顺序建议

### 第一阶段

- 抽前端共享题目配置类型与组件
- 先让 homework / exam 教师页统一使用相同的配置输出结构

### 第二阶段

- 抽后端 `QuestionSnapshot` / `QuestionAnswerPayload` / `GradingAdapter`
- homework / exam 改为通过共享层调用 `grading`

### 第三阶段

- 补齐 exam 快照化与成绩发布收口
- 逐步整理 homework / exam 的前后端契约一致性

### 第四阶段

- 在 homework / exam 方案稳定后，再评估 lab 哪些“标准题目部分”可以选择性接入共享能力层

## 14. 验收标准

当以下条件同时满足时，本次设计视为落地成功：

1. homework 与 exam 教师端都通过共享组件完成新增题目和从题库选题配置
2. homework 与 exam 使用统一的教师端题目配置结果结构
3. homework 与 exam 后端都使用统一的题目快照结构
4. homework 与 exam 学生提交都能映射为统一答案 payload
5. homework 与 exam 都通过同一种适配方式调用现有 `grading` 模块
6. homework 与 exam 各自的业务壳特性仍然保留，没有被错误抽象进共享层
7. 本次设计不会要求重建 `lab` 的步骤工作台或替换现有实验业务外壳
8. 教师端题目配置共享能力优先抽取自现有题库模块，而不是平行新建
9. 学生端题型作答核心能力优先复用现有实验模块，而不是平行新建
