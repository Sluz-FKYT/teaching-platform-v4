# 实验基础信息升级与实验指导下载设计

## 目标

将实验模块从“单一实验说明字段”升级为完整的实验基础信息模型，并打通教师端创建/编辑/展示与学生端查看/下载链路，满足以下目标：

1. 教师端创建实验时不再只填写单一“实验说明”，而是分别维护**实验要求**与**实验内容**。
2. 教师端创建/编辑实验时支持选择**实验类型**，后端继续使用整数编码，前端负责映射中文标签。
3. 教师端可为实验上传并绑定一个**实验指导文件**。
4. 教师端实验列表与相关展示区同步展示实验类型、实验要求摘要与实验指导状态。
5. 学生端实验详情页可查看实验要求、实验内容，并提供“下载实验指导”按钮。
6. 数据库层继续遵守当前已确认的约束：**相对旧版 `t_experiment` 只允许新增，不允许修改旧列定义**。

## 背景与现状

当前实验模块存在明显的“库表有字段、前后端没接通”的问题：

1. 教师端 `frontend/src/views/teacher/Labs.vue` 的创建/编辑弹窗只有 `title + classId + description + status + summaryRequired`，其中 `description` 被展示成“实验说明”。
2. 学生端 `frontend/src/views/student/LabDetail.vue` 也只展示 `detail.description`，没有区分实验要求与实验内容。
3. 后端实体 `backend/src/main/java/com/opencode/teachingplatform/lab/entity/Lab.java` 和实验表 `t_experiment` 实际已经具备：
   - `experiment_requirement`
   - `experiment_content`
   - `experiment_type`
   - `material_id`
4. 但当前公开 DTO 和视图组装仍只透出单一 `description`，并且 `setDescription()` 会把 `experimentContent` 同步覆盖成同一份文本，导致“两个库字段被前后端当成一个字段使用”。
5. 文件上传/下载现成能力已经存在于 `material` 域和 `LocalFileStorageService`，实验模块最自然的复用方式是通过 `materialId` 关联一个资料条目，而不是在 `lab` 域新增一套文件系统。

## 设计结论

本次采用以下总体方案：

- 实验基础信息模型升级为：`title + description(实验要求) + experimentContent(实验内容) + experimentType(实验类型编码) + materialId(实验指导)`。
- 实验类型继续使用**整数编码**保存在后端与数据库，前端统一映射为中文标签。
- 实验指导文件通过现有 `materials` 上传链路生成资料记录，再由实验的 `materialId` 引用。
- 教师端创建/编辑、教师端展示、学生端详情展示与下载全部围绕这套模型对齐。
- 数据库层不得修改旧 `t_experiment` 的旧列定义，只能通过新增列承载扩展能力。

该方案对应用户确认的“方案 1（复用 `materialId` 关联单个实验指导文件）+ 实验类型采用整数编码、前端映射标签”。

---

## 一、数据模型设计

### 1. 基础字段语义

本次统一实验基础信息语义如下：

- `title`：实验名称
- `description`：实验要求（继续映射旧列 `experiment_requirement`）
- `experimentContent`：实验内容（映射 `experiment_content`）
- `experimentType`：实验类型整数编码（映射 `experiment_type`）
- `materialId`：实验指导文件关联到的资料 ID（映射 `material_id`）
- `status`：草稿 / 已发布 / 已关闭
- `summaryRequired`：实验小结是否必填

### 2. 实验类型编码

后端与数据库继续使用 `int experiment_type`，前端统一维护标签映射：

| 编码 | 标签 |
|---|---|
| `1` | 基础实验 |
| `2` | 验证实验 |
| `3` | 综合实验 |
| `4` | 设计实验 |

说明：

- 该映射由前端负责展示，不要求数据库变更类型。
- 后端只存储和返回整数编码，不引入新的字符串枚举列。

### 3. 实验指导文件模型

实验指导文件不在 `lab` 域内新造附件表，而是复用现有资料链路：

1. 教师上传实验指导文件
2. 生成一个 `course_material` 记录
3. 将实验的 `materialId` 指向该记录
4. 学生端通过该关联显示下载按钮

该模型默认一个实验只绑定一个实验指导文件，允许后续替换，但不支持多文件管理。

---

## 二、数据库约束与演进原则

本次改造必须遵守如下数据库前提：

> 以旧版 `t_experiment` 为基线，旧列定义只允许保留，不允许修改；新增能力只能通过新增列实现。

### 1. 允许继续使用的旧列

- `experiment_id`
- `experiment_no`
- `experiment_name`
- `experiment_type`
- `instruction_type`
- `experiment_requirement`
- `experiment_content`
- `state`

### 2. 已允许的新增列

当前仓库已通过新增方式补入的列包括：

- `status`
- `start_at`
- `end_at`
- `material_id`
- `class_id`
- `created_by`
- `score_visibility_mode`
- `score_released`
- `summary_required`
- `created_at`
- `updated_at`

### 3. 本次设计要求

后续实现不得再次把以下旧列做类型/列名重定义：

- `experiment_id`
- `experiment_no`
- `instruction_type`
- `experiment_name`

前后端实现必须适配数据库现实，而不是让数据库去适配新实体幻想。

---

## 三、教师端创建 / 编辑设计

### 1. 页面范围

当前教师端创建/编辑集中在：

- `frontend/src/views/teacher/Labs.vue`

本次不拆出独立详情页，继续在当前创建/编辑弹窗内完成升级。

### 2. 表单结构

创建/编辑弹窗调整为以下分组：

1. **基础信息**
   - 实验名称
   - 所属班级
   - 实验类型
   - 发布状态

2. **实验内容**
   - 实验要求
   - 实验内容

3. **实验指导**
   - 当前已绑定文件
   - 上传 / 替换实验指导文件
   - 清空指导文件（可选）

4. **提交要求**
   - 实验小结是否必填

### 3. 上传交互

教师端上传实验指导时的设计要求：

- 上传成功后获取资料记录 ID
- 将该 ID 写回当前实验表单的 `materialId`
- 弹窗内展示当前文件名、上传状态与替换入口
- 编辑实验时应能回填当前已绑定的实验指导文件状态

### 4. 提交 payload

创建/更新实验 payload 升级为：

```ts
{
  title,
  description,        // 实验要求
  experimentContent,  // 实验内容
  experimentType,     // int
  classId,
  status,
  materialId,
  summaryRequired,
}
```

---

## 四、教师端展示设计

### 1. 实验列表展示

教师端实验列表卡片应同步展示：

- 实验名称
- 所属班级
- 实验类型标签
- 实验要求摘要
- 是否已绑定实验指导文件
- 实验小结要求状态
- 步骤数与发布状态

### 2. 展示原则

- 不再只渲染一个 `description` 作为“实验说明”。
- `description` 按“实验要求”语义展示。
- `experimentContent` 可在列表中做摘要显示，避免卡片过长。
- “已上传实验指导”需要有明确状态标签，便于教师快速核对。

---

## 五、学生端详情与下载设计

### 1. 页面范围

学生端实验详情入口位于：

- `frontend/src/views/student/LabDetail.vue`

### 2. 展示结构

学生端实验详情页新增或调整为：

- 实验名称
- 实验类型标签
- 实验要求
- 实验内容
- 实验指导下载按钮
- 当前题目目录
- 实验小结状态与已保存进度

### 3. 下载按钮规则

- 仅在实验已关联实验指导文件时显示
- 按钮文案明确为“下载实验指导”
- 点击后直接复用现有资料下载链路

### 4. 为什么不新增实验下载接口

因为现有资料模块已经具备：

- 文件存储
- 元信息管理
- 下载 URL / 下载接口

所以实验指导下载的最小正确实现，是让实验详情接口返回足够的资料信息，然后前端直接使用 materials 现有下载能力。

---

## 六、后端接口设计

### 1. 创建 / 更新实验请求

`LabRequests.CreateLabRequest` 与 `UpdateLabRequest` 需要增加：

- `experimentContent`
- `experimentType`

并继续保留：

- `description`
- `materialId`
- `summaryRequired`

### 2. 服务层写入规则

`LabService.applyLab(...)` 需要从“只写 description”升级为：

- 写实验要求到 `experiment_requirement`
- 写实验内容到 `experiment_content`
- 写实验类型到 `experiment_type`
- 写资料关联到 `material_id`

且不能再让 `setDescription()` 顺手覆盖 `experimentContent`。

### 3. 教师端返回视图

`toTeacherLabView()` 至少补回：

- `experimentContent`
- `experimentType`
- `materialId`
- 实验指导文件名/下载信息（若决定直接回传）

### 4. 学生端返回视图

`getStudentLabDetail()` / `toStudentLabView()` 至少补回：

- `experimentContent`
- `experimentType`
- `materialId`
- 实验指导文件的最小展示信息（推荐：`materialName` + `downloadUrl`）

---

## 七、materials 复用方案

### 1. 复用点

现成文件链路位于：

- 后端：`material` 模块 + `LocalFileStorageService`
- 前端：`api/materials.ts`

### 2. 推荐做法

本次实验指导文件绑定采用如下流程：

1. 教师端选择文件
2. 调用现有 materials 上传接口
3. 拿到新 material 的 `id`
4. 写入实验的 `materialId`
5. 教师端和学生端展示该资料的文件名与下载入口

### 3. 约束

- 一个实验一次只绑定一个实验指导文件
- 编辑时允许替换绑定对象
- 不在本次实现多文件管理、版本管理或实验专属附件列表

---

## 八、验收标准

本次改造完成后，必须满足：

1. 教师端创建实验时可分别填写“实验要求”和“实验内容”。
2. 教师端创建/编辑实验时可选择实验类型。
3. 教师端创建/编辑实验时可上传并绑定一个实验指导文件。
4. 教师端实验列表页能看到实验类型和实验指导状态。
5. 学生端实验详情页能看到实验要求、实验内容和实验类型。
6. 学生端在有指导文件时能点击“下载实验指导”。
7. 后端创建/更新/查询实验接口能完整透传这些字段。
8. 数据库层继续满足“旧 `t_experiment` 只增不改”。
9. 实验模块相关后端测试通过。
10. 前端 build 通过。

---

## 九、范围边界

本次改造明确包含：

- 教师端实验创建 / 编辑基础信息升级
- 教师端实验列表展示同步调整
- 学生端实验详情页信息展示与实验指导下载按钮
- 后端实验 DTO / service / view model 升级
- 实验指导文件复用现有 materials 链路

本次改造明确不包含：

- 实验步骤页题型系统重构
- 多实验指导文件管理
- 实验指导版本历史
- 新造实验专属文件存储系统

---

## 设计自检

- 本文没有保留 TBD / TODO 占位。
- 实验要求与实验内容已明确分离。
- 实验类型已明确继续使用整数编码，前端负责标签映射。
- 实验指导文件实现路径已明确为复用 `materialId`，没有引入第二套附件系统。
- 数据库约束已显式写入设计，不会在实现阶段再次改坏旧表定义。
