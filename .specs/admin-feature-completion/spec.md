# 管理后台辅助功能完善 Spec

## Why

管理后台当前存在多个功能入口但未实现实际逻辑，用户点击后仅显示"开发中"提示，影响管理效率和用户体验。需要完善这些辅助功能以提升管理后台的完整性和实用性。

## What Changes

- [ ] 实现用户管理：编辑用户、添加用户、导出用户功能
- [ ] 实现物品管理：导出物品功能
- [ ] 完善日志管理：日志导出、高级过滤功能
- [ ] 实现批量操作：批量删除用户功能
- [ ] 补充纠纷管理：前端页面和API对接

## Impact

- **Systems:** 管理后台前端、后端管理API、数据库
- **Files:** 
  - 前端：UserManagement.vue, ItemManagement.vue, LogManagement.vue, admin.js, paths.js
  - 后端：AdminUserController, AdminItemController, AdminLogController, AdminBatchController
  - 新增：AdminDisputeController前端对接
- **Users:** 管理员用户将获得完整的管理功能，提升工作效率

## ADDED Requirements

### 1. 编辑用户功能

**Scenario:** 管理员需要修改用户信息（如角色、状态、备注等）

**Acceptance Criteria:**
- [ ] 点击编辑按钮弹出用户编辑对话框
- [ ] 对话框显示用户当前信息（用户名、邮箱、手机、角色、状态）
- [ ] 管理员可修改用户角色和状态
- [ ] 提交后调用后端API更新用户信息
- [ ] 更新成功后刷新用户列表
- [ ] 表单验证：必填字段校验、角色/状态选项校验

### 2. 添加用户功能

**Scenario:** 管理员需要手动创建新用户账户

**Acceptance Criteria:**
- [ ] 点击添加用户按钮弹出创建用户对话框
- [ ] 对话框包含用户名、邮箱、密码、角色、状态等字段
- [ ] 表单验证：用户名唯一性、邮箱格式、密码强度
- [ ] 提交后调用后端API创建用户
- [ ] 创建成功后刷新用户列表并显示成功提示
- [ ] 密码使用BCrypt加密存储

### 3. 导出用户功能

**Scenario:** 管理员需要导出用户数据进行分析或备份

**Acceptance Criteria:**
- [ ] 点击导出按钮触发用户数据导出
- [ ] 支持导出当前筛选条件下的所有用户
- [ ] 导出格式为CSV或Excel
- [ ] 导出字段包含：用户名、邮箱、手机、角色、状态、注册时间
- [ ] 敏感信息（密码、身份证）不导出
- [ ] 导出文件自动下载到本地

### 4. 导出物品功能

**Scenario:** 管理员需要导出物品数据进行分析或备份

**Acceptance Criteria:**
- [ ] 点击导出按钮触发物品数据导出
- [ ] 支持导出当前筛选条件下的所有物品
- [ ] 导出格式为CSV或Excel
- [ ] 导出字段包含：物品标题、价格、状态、分类、发布者、发布时间
- [ ] 导出文件自动下载到本地

### 5. 日志导出功能

**Scenario:** 管理员需要导出操作日志进行审计或分析

**Acceptance Criteria:**
- [ ] 日志管理页面提供导出按钮
- [ ] 支持导出当前筛选条件下的所有日志
- [ ] 导出格式为CSV或Excel
- [ ] 导出字段包含：操作时间、操作人、操作类型、目标、详情
- [ ] 导出文件自动下载到本地

### 6. 日志高级过滤功能

**Scenario:** 管理员需要按多种条件筛选操作日志

**Acceptance Criteria:**
- [ ] 日志管理页面支持按操作类型过滤（如：用户管理、物品管理、订单管理）
- [ ] 支持按日志类型过滤（如：创建、更新、删除、审核）
- [ ] 支持按时间范围过滤（开始日期、结束日期）
- [ ] 过滤条件实时生效，刷新日志列表
- [ ] 清除过滤按钮重置所有筛选条件

### 7. 批量删除用户功能

**Scenario:** 管理员需要批量删除多个用户

**Acceptance Criteria:**
- [ ] 用户列表支持多选（复选框）
- [ ] 选中多个用户后显示批量操作按钮
- [ ] 点击批量删除弹出确认对话框
- [ ] 确认后调用后端批量删除API
- [ ] 删除成功后刷新用户列表
- [ ] 删除失败时显示具体错误信息

### 8. 纠纷管理前端对接

**Scenario:** 管理员需要在前端管理纠纷案件

**Acceptance Criteria:**
- [ ] 管理后台侧边栏增加"纠纷管理"菜单项
- [ ] 纠纷管理页面显示纠纷列表（纠纷编号、物品、买卖双方、状态、创建时间）
- [ ] 支持按纠纷状态过滤（待处理、处理中、已解决、已关闭）
- [ ] 点击纠纷详情显示完整纠纷信息和处理记录
- [ ] 管理员可处理纠纷（同意退款、驳回、关闭）
- [ ] 处理后自动更新关联订单状态

## MODIFIED Requirements

### 1. 用户管理API扩展

**Before:** AdminUserController仅支持查看、状态更新、删除

**After:** AdminUserController支持完整CRUD操作

**Scenario:** 管理员需要完整管理用户生命周期

**Acceptance Criteria:**
- [ ] 新增POST `/api/admin/users` 创建用户端点
- [ ] 新增PUT `/api/admin/users/{id}` 编辑用户端点
- [ ] 新增GET `/api/admin/users/export` 导出用户端点
- [ ] 所有新端点需要管理员权限验证
- [ ] 创建和编辑用户时进行数据校验

### 2. 物品管理API扩展

**Before:** AdminItemController仅支持查看、审核、驳回、下架、删除

**After:** AdminItemController支持导出功能

**Scenario:** 管理员需要导出物品数据

**Acceptance Criteria:**
- [ ] 新增GET `/api/admin/items/export` 导出物品端点
- [ ] 支持按筛选条件导出
- [ ] 导出文件格式为CSV或Excel

### 3. 日志管理API扩展

**Before:** AdminLogController仅支持基础查询

**After:** AdminLogController支持高级过滤和导出

**Scenario:** 管理员需要更强大的日志分析能力

**Acceptance Criteria:**
- [ ] GET `/api/admin/logs` 增加type、logType、startDate、endDate参数
- [ ] 新增GET `/api/admin/logs/export` 导出日志端点
- [ ] 导出功能支持当前筛选条件

### 4. 批量操作API扩展

**Before:** AdminBatchController仅支持物品批量操作和用户状态批量更新

**After:** AdminBatchController支持用户批量删除

**Scenario:** 管理员需要批量清理用户数据

**Acceptance Criteria:**
- [ ] 新增POST `/api/admin/batch/users/delete` 批量删除用户端点
- [ ] 支持批量删除多个用户
- [ ] 删除前进行权限校验（不能删除超级管理员）
- [ ] 记录批量删除操作日志

## REMOVED Requirements

### 1. 移除"开发中"提示

**Reason:** 功能已实现，不再需要占位提示

**Migration:** 所有`ElMessage.info('功能开发中')`代码将被实际功能替换

---

**Change ID:** `admin-feature-completion`
**Date:** 2026-05-09
**Status:** Draft
