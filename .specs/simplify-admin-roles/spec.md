# 统一精简管理员角色 Spec

## Why

当前系统中存在"管理员"和"超级管理员"的概念混淆：README、数据字典预定义了 SUPER_ADMIN，但后端枚举、数据库 ENUM、权限注解均只支持单一 ADMIN 角色，前端则对两者混合处理。这种不一致导致：
- SUPER_ADMIN 用户实际无法通过 Spring Security 的 `hasAuthority("ADMIN")` 检查
- 代码维护者容易对角色体系产生误解
- 新增权限逻辑时需要额外处理不存在的角色分支

业务场景下单一管理员角色即可覆盖全部后台操作，无需额外分级，因此统一精简为仅保留管理员角色。

## What Changes

- [ ] 移除数据字典中的 SUPER_ADMIN 角色条目
- [ ] 简化前端 isAdmin 判断逻辑，移除 SUPER_ADMIN 分支
- [ ] 修正前端管理后台 getRoleText() 角色显示映射
- [ ] 更新前端测试，移除 SUPER_ADMIN 测试用例
- [ ] 更新 README 角色权限表，合并管理员与超级管理员

## Impact

- **Systems:** 后端（数据字典）、前端（Store、Admin 页面、测试）、文档（README）
- **Files:**
  - `backend/.../V19__extend_system_configs_and_create_dict_tables.sql`
  - `frontend/src/store/modules/user.js`
  - `frontend/src/views/admin/Admin.vue`
  - `frontend/tests/unit/store/userStore.test.js`
  - `README.md`
- **Users:** 无影响。系统中本不存在 SUPER_ADMIN 用户，所有变更均为代码清理，不涉及数据迁移或功能变更。

## ADDED Requirements

无新增需求。

## MODIFIED Requirements

### 1. 数据字典移除 SUPER_ADMIN

**Before:** 字典类型 USER_ROLE 包含 USER、ADMIN、SUPER_ADMIN 三个条目

**After:** 仅保留 USER（普通用户）和 ADMIN（管理员）两个条目

**Scenario:** 管理员角色选择/展示时不再出现"超级管理员"选项

**Acceptance Criteria:**
- [ ] dict_items 表中 USER_ROLE 类型下没有 item_value = 'SUPER_ADMIN' 的条目

### 2. 前端 isAdmin 判断逻辑简化

**Before:** `user.value?.role === 'ADMIN' || user.value?.role === 'SUPER_ADMIN'`

**After:** `user.value?.role === 'ADMIN'`

**Scenario:** 判断当前用户是否为管理员

**Acceptance Criteria:**
- [ ] isAdmin 仅当 role 为 'ADMIN' 时返回 true
- [ ] isAdmin 在 role 为其他值时返回 false

### 3. 前端 getRoleText() 映射修正

**Before:** 存在混乱的三路映射（ADMIN→超级管理员、admin→管理员、ROLE_ADMIN→系统管理员）

**After:** 统一映射为 ADMIN→管理员，且保持友好降级

**Scenario:** 管理后台顶部显示当前用户角色文本

**Acceptance Criteria:**
- [ ] role 为 'ADMIN' 时显示"管理员"
- [ ] role 为其他值时也显示"管理员"（降级）

### 4. README 角色权限表合并

**Before:** 管理员和超级管理员分两行展示

**After:** 合并为一行"管理员"，权限范围涵盖"内容审核、用户管理、数据统计、系统配置"

**Scenario:** 查阅文档时角色权限清晰无混淆

**Acceptance Criteria:**
- [ ] 角色权限表中不再出现"超级管理员"行
- [ ] 管理员权限描述涵盖原先两者的全部功能

## REMOVED Requirements

### 1. 移除 SUPER_ADMIN 测试用例

**Reason:** SUPER_ADMIN 角色已不存在，对应的测试用例失去意义

**Migration:** 将原 SUPER_ADMIN 测试用例改为验证 'ADMIN' 角色的正常路径

---

**Change ID:** `simplify-admin-roles`
**Date:** 2026-05-09
**Status:** Draft
