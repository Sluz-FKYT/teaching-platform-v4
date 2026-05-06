# 05 关键模块深挖：权限与认证

## 1. 模块目标

权限与认证模块的核心目标，是保证教师与学生在同一系统中访问到不同的页面入口、不同的接口集合和不同的业务能力。

## 2. 后端证据

### 2.1 认证入口

`AuthController` 提供以下接口：

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `PUT /api/v1/auth/profile`
- `POST /api/v1/auth/change-password`
- `POST /api/v1/auth/logout`

### 2.2 安全配置

`SecurityConfig` 明确了：

- 启用方法级安全控制
- `/api/v1/auth/login` 允许匿名访问
- 其他接口默认需要认证
- 使用 JWT 过滤器接入认证链路
- 会话策略为无状态（stateless）

## 3. 前端证据

前端通过路由守卫与登录态管理，完成按角色跳转与页面访问控制。答辩中可强调“前端负责交互层保护，后端负责业务层保护”，形成双层边界。

## 4. 数据库支撑

权限模块的数据库锚点为 `sys_user`：

- `username`
- `password_hash`
- `role`
- `status`
- `must_change_password`

同时 `class_room.teacher_user_id`、`class_member.student_user_id` 建立了角色与教学组织的关系。

## 5. 可在 PPT 中重点讲的设计价值

- 教师与学生角色边界清晰
- 页面入口与接口访问控制一致
- 认证、资料更新、改密形成完整账户安全链路
- 角色切换不是前端假控制，而是后端真实校验
