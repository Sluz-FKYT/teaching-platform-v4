# 12 体系结构与设计模式页正式文案

本文件用于直接支撑 PPT 中“主架构”与“设计模式/实现模式”两页内容，可作为后续 `ppt-master` 输入文案，也可直接作为答辩讲稿底稿。

---

## 第 1 页：系统主架构设计

### 页标题

系统主架构设计

### 上屏文案

- 系统整体采用 **B/S 架构**
- 前端与后端采用 **分离式协作**
- 后端整体采用 **模块化单体架构**
- 后端内部采用 **分层架构**

### 配图建议

- 主图：`exports/architecture-overview.png`
- 辅图 1：`exports/code-structure-overview.png`
- 辅图 2：`exports/layered-pattern-overview.png`

### 证据路径

- B/S 架构：`frontend/package.json`、`frontend/src/router/index.ts`、`docker-compose.yml`
- 前后端分离：`frontend/src/utils/request.ts`、`frontend/src/api/*.ts`、`backend/src/main/java/**/*Controller.java`
- 模块化单体：`teaching-platform/AGENTS.md`、`backend/src/main/java/com/opencode/teachingplatform/`
- 分层架构：`backend/src/main/java/com/opencode/teachingplatform/**/{controller,service,repository,entity,dto}`

### 备注讲稿

从系统整体访问形态看，这是一个典型的 B/S 应用，用户通过浏览器访问教学平台功能。前端和后端在工程上是分离的，前端负责页面展示、状态管理和交互，后端负责业务处理、权限控制与数据持久化，并通过 API 契约协作。后端没有拆成多个微服务，而是作为一个单体应用部署，但内部按认证、实验、作业、考试、分析等业务域模块化组织，因此更准确地说，它属于模块化单体架构。在模块内部，又采用了典型的分层架构，把控制层、业务层、数据访问层和实体对象层分离开来，从而提高可维护性与可测试性。

---

## 第 2 页：设计模式与工程实现模式

### 页标题

设计模式与工程实现模式

### 上屏文案

- **Repository 模式**
- **DTO 模式**
- **门面式封装思想（Facade-like）**
- **策略化设计思想（Strategy-like）**
- **统一响应包装模式**

### 配图建议

- 主图：`exports/unified-response-pattern.png`
- 辅图 1：`exports/component-auth-analytics-answering.png`
- 辅图 2：`exports/auth-routing-flow.png`
- 可替换占位：后续补 `SecurityConfig`、`ApiResponse`、`request.ts`、`ExamScoringService` 局部代码截图

### 证据路径

- Repository 模式：`backend/src/main/java/com/opencode/teachingplatform/**/repository/*.java`
- DTO 模式：`backend/src/main/java/com/opencode/teachingplatform/**/dto/*Requests.java`
- 门面式封装：`frontend/src/utils/request.ts`、`frontend/src/api/*.ts`、`backend/src/main/java/com/opencode/teachingplatform/common/api/ApiResponse.java`
- 策略化思想：`backend/src/main/java/com/opencode/teachingplatform/exam/service/ExamScoringService.java`、`lab/service/LabAutoGradingService.java`、`plagiarism/service/LocalPlagiarismService.java`
- 统一响应包装：`backend/src/main/java/com/opencode/teachingplatform/common/api/ApiResponse.java`、`frontend/src/utils/request.ts`、`frontend/src/types/common.ts`

### 备注讲稿

在设计模式层面，这个项目更适合从工程实现模式来理解。第一，后端通过 Repository 模式封装数据访问逻辑，使 Service 不直接操作底层持久化细节。第二，系统大量使用 DTO 模式，将请求对象与数据库实体分离，降低接口层与实体层耦合。第三，前端请求封装和后端统一响应都体现了门面式封装思想，通过统一入口屏蔽底层交互细节。第四，在考试评分、实验自动评分和查重算法中，可以看到明显的策略化设计思想，也就是把算法逻辑从主业务流程中拆分出来。第五，系统采用统一响应包装模式，后端统一返回 `code、message、data、timestamp`，前端也按统一结构解析，从而保证接口风格一致。

### 建议放在页脚的小字说明

本页中的“设计模式”既包括经典设计模式，也包括工程实践中可稳定识别的实现模式与封装思想。
