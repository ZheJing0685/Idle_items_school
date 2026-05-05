# Tasks

- [ ] Task 1: 生成 `01-项目概览与架构.md`
  - 包含：项目愿景与标语、技术栈版本表、前后端分层架构图（文本 Mermaid）、完整目录结构树、核心业务流程概述
  - 输出路径：`docs/code-wiki/01-项目概览与架构.md`

- [ ] Task 2: 生成 `02-后端模块职责.md`
  - 包含：每个 Java 包的职责说明、每个类的关键方法签名与用途、安全架构（JWT/角色/过滤器）、缓存策略、定时任务
  - 输出路径：`docs/code-wiki/02-后端模块职责.md`

- [ ] Task 3: 生成 `03-前端模块职责.md`
  - 包含：每个目录的职责说明、每个页面/组件的功能描述、Pinia Store 状态管理、API 服务层、路由守卫逻辑、Composables
  - 输出路径：`docs/code-wiki/03-前端模块职责.md`

- [ ] Task 4: 生成 `04-数据模型与数据库.md`
  - 包含：全部 14 个 Entity 的字段表（字段名/类型/说明/约束）、枚举值说明、表间关系图（文本）、Flyway 迁移脚本 V1-V16 概览
  - 输出路径：`docs/code-wiki/04-数据模型与数据库.md`

- [ ] Task 5: 生成 `05-API 接口文档.md`
  - 包含：按模块分组的全部 REST API 端点（方法/路径/权限/说明）、统一响应格式、错误码表
  - 输出路径：`docs/code-wiki/05-API 接口文档.md`

- [ ] Task 6: 生成 `06-依赖关系.md`
  - 包含：后端 Maven 依赖清单（含版本）、前端 NPM 依赖清单（含版本）、模块间依赖关系图（文本）
  - 输出路径：`docs/code-wiki/06-依赖关系.md`

- [ ] Task 7: 生成 `07-运行与部署.md`
  - 包含：本地开发环境要求、前后端启动步骤、Docker Compose 部署、多环境配置差异、CI/CD 流水线说明
  - 输出路径：`docs/code-wiki/07-运行与部署.md`

# Task Dependencies

- Task 1 无依赖，可独立执行
- Task 2 依赖 Task 1（需要架构上下文）
- Task 3 依赖 Task 1（需要架构上下文）
- Task 4 无依赖，可独立执行
- Task 5 无依赖，可独立执行
- Task 6 无依赖，可独立执行
- Task 7 无依赖，可独立执行
- Task 2 和 Task 3 可并行执行
- Task 4、5、6、7 可并行执行
