# Checklist

## .gitignore 完整性检查

- [ ] `.gitignore` 包含 `backend/uploads/` 忽略规则
- [ ] `.gitignore` 包含 `uploads/` 忽略规则
- [ ] `.gitignore` 包含 `frontend/tests/reports/` 忽略规则
- [ ] `.gitignore` 包含 `frontend/.env.development` 忽略规则
- [ ] `.gitignore` 包含 `frontend/.env.production` 忽略规则
- [ ] `.gitignore` 包含 `frontend/.env.staging` 忽略规则
- [ ] `.gitignore` 保留 `frontend/.env.example`（使用 `!.env.example` 排除）
- [ ] `.gitignore` 包含 `outputs/` 忽略规则
- [ ] `.gitignore` 包含 `doc/` 忽略规则
- [ ] `.gitignore` 包含 `sql/idle_items_school.sql` 忽略规则
- [ ] `.gitignore` 包含临时笔记文件忽略规则

## Git 跟踪清理检查

- [ ] `backend/uploads/` 目录下的文件已从 Git 索引中移除（`git ls-files backend/uploads/` 返回空）
- [ ] `uploads/` 目录下的文件已从 Git 索引中移除（`git ls-files uploads/` 返回空）
- [ ] `frontend/tests/reports/` 目录下的文件已从 Git 索引中移除
- [ ] `frontend/.env.development` 已从 Git 索引中移除
- [ ] `frontend/.env.production` 已从 Git 索引中移除
- [ ] `frontend/.env.staging` 已从 Git 索引中移除
- [ ] `frontend/.env.example` 仍保留在 Git 跟踪中
- [ ] `outputs/` 目录下的文件已从 Git 索引中移除
- [ ] 本地文件在 `git rm --cached` 后仍然存在（未被物理删除）

## 敏感信息清理检查

- [ ] `docker-compose.yml` 中不再包含硬编码密码 `root`
- [ ] `docker-compose.yml` 使用 `${MYSQL_ROOT_PASSWORD}` 等环境变量引用
- [ ] `application-dev.yml` 中 `jwt.secret` 不再包含硬编码密钥字符串
- [ ] `application-staging.yml` 中 `jwt.secret` 不再包含硬编码密钥字符串
- [ ] `application-prod.yml` 中 `jwt.secret` 仅使用 `${JWT_SECRET}` 环境变量（已满足，无需修改）
- [ ] `application-test.yml` 中的测试密钥可接受（仅用于测试环境）

## 提交与推送检查

- [ ] 提交信息遵循 Conventional Commits 规范
- [ ] `git status` 显示工作区干净（无未提交变更）
- [ ] `git push origin main` 成功推送到远程仓库
- [ ] GitHub 仓库页面确认敏感文件已不可见

## 最佳实践文档检查

- [ ] 分支规范已文档化（main / develop / feature / hotfix）
- [ ] 提交信息规范已文档化（Conventional Commits）
- [ ] 隐私保护最佳实践已文档化
- [ ] 常见错误规避指南已文档化
