# Verification Checklist

## Pre-Implementation

- [x] spec.md is complete and approved
- [x] tasks.md is complete with clear verification steps
- [ ] User has reviewed the specification
- [ ] Change ID is assigned: `simplify-admin-roles`

## Implementation

### Code Quality

- [ ] SQL 语法正确，不破坏已有数据字典
- [ ] 前端代码遵循现有风格（Composition API + Pinia）
- [ ] 所有测试通过
- [ ] 无 linting 错误引入

### Functionality

- [ ] SUPER_ADMIN 引用已从所有源码中移除
- [ ] isAdmin 计算属性正确判断 ADMIN 角色
- [ ] getRoleText() 正确显示"管理员"
- [ ] 数据字典 USER_ROLE 仅包含 USER 和 ADMIN

### Testing

- [ ] 前端单元测试通过
- [ ] SUPER_ADMIN 测试用例已移除或更新
- [ ] 管理员 ADMIN 测试用例保持正常

### Documentation

- [ ] README 角色权限表已更新
- [ ] 不再出现"超级管理员"文档引用

## Post-Implementation

### Verification

- [ ] 全文搜索 `SUPER_ADMIN` 确认无残留（排除 findings.md 和 .specs/ 目录）
- [ ] 前端 build 无报错
- [ ] 后端编译通过

### Cleanup

- [ ] 临时文件已移除
- [ ] 调试代码已移除

### Commit

- [ ] 变更以清晰的消息提交

## Sign-off

- [ ] Developer sign-off
- [ ] Reviewer sign-off

---

**Change ID:** `simplify-admin-roles`
**Date Completed:** YYYY-MM-DD
**Status:** Draft
