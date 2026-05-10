# Implementation Tasks

## Task Overview

| ID | Task | Dependencies | Estimated |
|----|------|--------------|-----------|
| T1 | 移除数据字典中的 SUPER_ADMIN | None | 5min |
| T2 | 简化前端 isAdmin 和 getRoleText | None | 10min |
| T3 | 更新前端测试 | T2 | 5min |
| T4 | 更新 README 角色权限表 | None | 5min |

---

## Tasks

### Task T1: 移除数据字典中的 SUPER_ADMIN

**Files:**
- Modify: `backend/src/main/resources/db/migration/V19__extend_system_configs_and_create_dict_tables.sql:141-144`

**Steps:**

1. 删除 `SUPER_ADMIN` 的数据字典插入语句，使 USER_ROLE 类型只保留 USER 和 ADMIN

2. 验证 SQL 语法正确

**Verification:**
- [ ] V19.sql 中不再包含 `'SUPER_ADMIN'` 字符串
- [ ] USER_ROLE 字典数据仅剩 USER 和 ADMIN 两行

---

### Task T2: 简化前端 isAdmin 和 getRoleText

**Files:**
- Modify: `frontend/src/store/modules/user.js:17-19`
- Modify: `frontend/src/views/admin/Admin.vue:303-313`

**Steps:**

1. 在 `user.js` 中将 isAdmin 计算属性从 `user.value?.role === 'ADMIN' || user.value?.role === 'SUPER_ADMIN'` 简化为 `user.value?.role === 'ADMIN'`

2. 在 `Admin.vue` 中将 getRoleText() 方法中的混乱映射简化为清晰的单行逻辑

**Verification:**
- [ ] isAdmin 只检查 `=== 'ADMIN'`
- [ ] getRoleText() 对 ADMIN 显示"管理员"，其他值同样显示"管理员"

---

### Task T3: 更新前端测试

**Files:**
- Modify: `frontend/tests/unit/store/userStore.test.js:113-116`

**Steps:**

1. 将 SUPER_ADMIN 测试用例改为验证 USER 角色返回非管理员（或直接移除该用例，因为 ADMIN 用例已覆盖相同逻辑）

**Verification:**
- [ ] 测试文件中不再引用 `SUPER_ADMIN`
- [ ] 所有测试通过

---

### Task T4: 更新 README 角色权限表

**Files:**
- Modify: `README.md:452-459`

**Steps:**

1. 删除"超级管理员"行
2. 将管理员权限范围扩展为"内容审核、用户管理、数据统计、系统配置"

**Verification:**
- [ ] README 中不再出现"超级管理员"
- [ ] 管理员权限描述涵盖全部后台功能

---

## Parallel Tasks

- T1、T2、T4 互不依赖，可并行执行
- T3 依赖 T2

## Notes

- 所有变更为纯代码清理，不涉及数据迁移
- 后端 Java 枚举（User.Role）和数据库 ENUM 已正确，无需改动
- 无需编写新迁移脚本，直接修改 V19.sql 的 INSERT 语句即可
