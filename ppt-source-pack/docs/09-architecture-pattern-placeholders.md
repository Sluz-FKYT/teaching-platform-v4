# 09 体系结构与设计模式图片建议及占位说明

## 1. 这一部分为什么需要额外图片

在 PPT 后半段讲“体系结构、设计模式”时，如果只放文字，老师会觉得偏空；但如果直接贴源码截图，又容易过密、难讲。因此更适合准备“结构化示意图 + 代码结构占位图 + 局部源码证据图”三类图片。

## 2. 推荐加入的图片

### 2.1 系统总体架构图

- 建议图片：`exports/architecture-overview.png`
- 用途：说明前端、后端、数据库、设计解释层之间的总体关系
- 适合放在“总体架构”页

### 2.2 代码结构图（占位）

- 建议图片：`exports/code-structure-overview.png`
- 用途：展示前端与后端按目录和业务域分层组织
- 说明：当前图是结构化示意图，后续你可以自己替换为 IDE 截图或文件树截图

### 2.3 权限与路由协作图

- 建议图片：`exports/auth-routing-flow.png`
- 用途：讲前端路由守卫、Pinia 登录态、后端认证接口、JWT 校验之间的协作
- 适合放在“权限模块”页后半部分或“设计模式”页

### 2.4 统一响应与异常处理图

- 建议图片：`exports/unified-response-pattern.png`
- 用途：说明前端请求封装、统一响应结构、后端全局异常处理之间的关系
- 对应代码证据：
  - `frontend/src/utils/request.ts`
  - `backend/src/main/java/.../common/api/ApiResponse.java`
  - `backend/src/main/java/.../common/exception/GlobalExceptionHandler.java`

### 2.5 分层与模块协作图

- 建议图片：`exports/layered-pattern-overview.png`
- 用途：讲 Controller / Service / Repository / Entity / Frontend 之间的职责分离
- 适合放在“设计模式”页

### 2.6 关键模块关系图

- 建议图片：`exports/component-auth-analytics-answering.png`
- 用途：讲权限、答题、分析三个关键模块之间的横向联系

## 3. 后续你可自行替换的证据型图片

下列图片建议后续你自己替换为真实截图：

- IDE 中的后端包结构截图
- IDE 中的前端 `router` / `stores` / `api` 结构截图
- `SecurityConfig`、`ApiResponse`、`GlobalExceptionHandler`、`LabService`、`ExamService` 的局部代码截图
- 运行中的教师端 / 学生端真实页面截图

## 4. 占位策略

当前这批图片的目标是：

- 先让 PPT 结构完整、可讲
- 让老师看到“架构与模式”不是空口描述
- 后续你可替换其中一部分为更有说服力的真实代码截图或运行截图

## 5. 推荐在 PPT 中的搭配方式

- 总体架构页：总体架构图 + 一句总结
- 设计模式页：代码结构图 + 分层模式图 + 统一响应图
- 权限页：权限协作图 + 局部代码截图占位
- 关键模块总结页：关键模块关系图
