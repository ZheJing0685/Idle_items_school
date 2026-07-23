# 闲物校园 - 项目文档索引

## 概述

**项目名称**: 闲物校园 (Idle Items School)  
**描述**: 面向高校学生的闲置物品交易平台，支持物品发布、订单管理、即时聊天、纠纷处理、实名认证、碳减排追踪等功能。  
**架构**: 前后端分离  
- 后端: Spring Boot 3.5 + MySQL 8.0 + Redis 7 + WebSocket  
- 前端: Vue 3 + TypeScript + Vite 8 + Pinia + Element Plus  

## 文档清单

| 文档 | 说明 |
|------|------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | 系统架构：整体分层、安全机制、配置管理、部署方式 |
| [INTERFACES.md](./INTERFACES.md) | API接口目录：所有 REST 端点完整列表（含认证要求） |
| [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) | 开发者指南：开发流程、环境搭建、测试、构建、排错 |

## 模块说明

| 模块 | 路径 | 说明 |
|------|------|------|
| [admin](./模块/admin.md) | `module/admin` | 后台管理：用户/物品/订单/纠纷审批、操作日志、统计分析、批量操作 |
| [auth](./模块/auth.md) | `module/auth` | 认证授权：注册、登录、Token刷新、密码重置、邮箱验证码 |
| [carbon](./模块/carbon.md) | `module/carbon` | 碳减排追踪：记录学生闲置交易的环保贡献 |
| [category](./模块/category.md) | `module/category` | 分类管理：树形分类、变更日志、用户反馈 |
| [chat](./模块/chat.md) | `module/chat` | 即时通讯：买卖双方聊天会话、WebSocket推送 |
| [dispute](./模块/dispute.md) | `module/dispute` | 纠纷处理：买家/卖家申诉、管理员仲裁 |
| [file](./模块/file.md) | `module/file` | 文件服务：分片上传、图片处理、敏感词检测 |
| [item](./模块/item.md) | `module/item` | 核心交易：物品CRUD、搜索推荐、收藏、浏览量统计 |
| [notification](./模块/notification.md) | `module/notification` | 消息通知：站内通知、邮件推送 |
| [order](./模块/order.md) | `module/order` | 订单生命周期：创建→支付→发货→确认收货→评价 |
| [system](./模块/system.md) | `module/system` | 系统配置：字典管理、系统参数 |
| [user](./模块/user.md) | `module/user` | 用户中心：个人信息、实名认证、角色管理 |

## 专有概念

| 概念 | 说明 |
|------|------|
| [Session 机制](./专有概念/Session机制.md) | 基于Redis的无状态JWT Session管理 |
| [交易状态机](./专有概念/交易状态机.md) | 物品和订单的完整生命周期状态流转 |
| [安全框架](./专有概念/安全框架.md) | JWT认证、XSS防护、限流、角色权限 |
| [错误处理](./专有概念/错误处理.md) | BusinessException + ErrorHandler 统一异常流 |
| [分片上传](./专有概念/分片上传.md) | 大文件分片上传与断点续传机制 |

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.14 |
| ORM | Spring Data JPA + Hibernate | — |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7-alpine |
| 迁移 | Flyway | — |
| 认证 | jjwt | 0.12.6 |
| 安全 | OWASP Java Encoder | 1.2.3 |
| 文档 | Knife4j (OpenAPI 3) | 4.5.0 |
| 监控 | Micrometer + Prometheus | — |
| 前端框架 | Vue.js | 3.5.32 |
| 类型 | TypeScript | ~5.8.0 |
| 构建 | Vite | 8.1.5 |
| 状态管理 | Pinia | 3.0.4 |
| 路由 | vue-router | 4.6.4 |
| UI组件 | Element Plus | 2.13.7 |
| HTTP | Axios | 1.15.0 |
| PWA | vite-plugin-pwa | 1.3.0 |
| 测试 (FE) | Vitest + Playwright | 4.1.10 / 1.59.1 |
| 测试 (BE) | JUnit 5 + MockMvc | — |
| CI/CD | GitHub Actions + Qodana | — |
| 容器化 | Docker + docker-compose | — |
