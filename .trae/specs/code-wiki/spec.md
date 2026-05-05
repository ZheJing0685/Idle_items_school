# Code Wiki 文档生成 Spec

## Why

项目缺少一份结构化的、面向开发者的技术文档（Code Wiki），新成员加入或进行代码审查时需要逐文件阅读才能理解项目全貌。需要生成一套完整的 Code Wiki 文档，涵盖项目架构、模块职责、关键类/函数、依赖关系和运行方式。

## What Changes

- 在 `docs/code-wiki/` 目录下生成以下 Markdown 文档：
  - `01-项目概览与架构.md` — 项目愿景、技术栈、整体架构图（文本）、目录结构说明
  - `02-后端模块职责.md` — Spring Boot 后端各层（Controller/Service/Repository/Entity/Config/Filter/Aspect/Task/Util）的职责、类清单与关键方法说明
  - `03-前端模块职责.md` — Vue 3 前端各层（Views/Components/Store/API/Router/Composables/Styles）的职责、组件清单与关键逻辑说明
  - `04-数据模型与数据库.md` — 全部 Entity 字段说明、数据库表关系、Flyway 迁移脚本概览
  - `05-API 接口文档.md` — 全部 REST API 端点清单（方法/路径/权限/说明）
  - `06-依赖关系.md` — 前后端依赖清单（含版本）、模块间依赖关系
  - `07-运行与部署.md` — 本地开发、Docker 部署、CI/CD 流水线、多环境配置说明

## Impact

- Affected specs: 无（新建文档，不影响现有功能）
- Affected code: 仅新增 `docs/code-wiki/` 目录及 7 个 Markdown 文件

## ADDED Requirements

### Requirement: 项目整体架构文档
系统 SHALL 提供一份包含项目愿景、技术栈版本、前后端分层架构图（文本形式）、完整目录结构树的文档。

#### Scenario: 开发者阅读架构文档
- **WHEN** 开发者打开 `01-项目概览与架构.md`
- **THEN** 能在 5 分钟内理解项目技术栈、分层方式和目录组织

### Requirement: 后端模块职责文档
系统 SHALL 提供后端每个包（controller/service/repository/entity/config/filter/aspect/task/util/cache/security/common/annotation/dto）的职责说明，列出每个类的关键方法和用途。

#### Scenario: 开发者定位后端代码
- **WHEN** 开发者需要修改某个后端功能
- **THEN** 能通过文档快速定位到对应的类和方法

### Requirement: 前端模块职责文档
系统 SHALL 提供前端每个目录（views/components/store/api/router/composables/styles）的职责说明，列出每个组件/页面的关键功能。

#### Scenario: 开发者定位前端代码
- **WHEN** 开发者需要修改某个前端页面
- **THEN** 能通过文档快速定位到对应的 Vue 组件和 Store

### Requirement: 数据模型文档
系统 SHALL 提供全部 Entity 的字段说明（含类型、约束、枚举值），以及数据库表间关系和 Flyway 迁移脚本概览。

#### Scenario: 开发者理解数据模型
- **WHEN** 开发者需要理解某个实体的字段含义
- **THEN** 能通过文档查到字段类型、约束和关联关系

### Requirement: API 接口文档
系统 SHALL 提供全部 REST API 端点的清单，包含 HTTP 方法、路径、权限要求和功能说明。

#### Scenario: 前后端联调
- **WHEN** 前端开发者需要调用某个 API
- **THEN** 能通过文档查到端点路径、请求方式和权限要求

### Requirement: 依赖关系文档
系统 SHALL 提供前后端完整依赖清单（含版本号）和模块间依赖关系说明。

#### Scenario: 依赖升级评估
- **WHEN** 开发者需要升级某个依赖
- **THEN** 能通过文档了解当前版本和影响范围

### Requirement: 运行与部署文档
系统 SHALL 提供本地开发环境搭建、Docker 部署、CI/CD 流水线和多环境配置的完整说明。

#### Scenario: 新成员搭建开发环境
- **WHEN** 新成员需要在本地运行项目
- **THEN** 能按照文档步骤完成环境搭建并启动前后端
