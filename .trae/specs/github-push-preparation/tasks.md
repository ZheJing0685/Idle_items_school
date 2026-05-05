# Tasks

- [ ] Task 1: 更新根目录 `.gitignore` 文件
  - [ ] SubTask 1.1: 补充用户上传目录忽略规则（`backend/uploads/`、`uploads/`）
  - [ ] SubTask 1.2: 补充测试报告目录忽略规则（`frontend/tests/reports/`）
  - [ ] SubTask 1.3: 补充前端环境配置文件忽略规则（`frontend/.env.development`、`frontend/.env.production`、`frontend/.env.staging`）
  - [ ] SubTask 1.4: 补充运行时产物忽略规则（`outputs/`）
  - [ ] SubTask 1.5: 补充临时笔记文件忽略规则（`分析.txt`、`已修复清单.txt`）
  - [ ] SubTask 1.6: 补充 `.trae/` 目录下 specs 和 rules 的忽略规则
  - [ ] SubTask 1.7: 补充 `doc/` 目录忽略规则（与 `docs/` 重复的临时规划文档）
  - [ ] SubTask 1.8: 补充 `sql/idle_items_school.sql` 忽略规则（可能含敏感数据的完整数据库导出）
  - [ ] SubTask 1.9: 补充 `package-lock.json` 的处理说明（大型项目建议保留，但需注意）

- [ ] Task 2: 从 Git 跟踪中移除不应追踪的文件
  - [ ] SubTask 2.1: 执行 `git rm --cached -r backend/uploads/` 移除后端上传文件
  - [ ] SubTask 2.2: 执行 `git rm --cached -r uploads/` 移除根目录上传文件
  - [ ] SubTask 2.3: 执行 `git rm --cached -r frontend/tests/reports/` 移除测试报告
  - [ ] SubTask 2.4: 执行 `git rm --cached frontend/.env.development frontend/.env.production frontend/.env.staging` 移除环境配置
  - [ ] SubTask 2.5: 执行 `git rm --cached -r outputs/` 移除运行时产物
  - [ ] SubTask 2.6: 执行 `git rm --cached "分析.txt" "已修复清单.txt"` 移除临时笔记
  - [ ] SubTask 2.7: 执行 `git rm --cached -r doc/` 移除临时规划文档
  - [ ] SubTask 2.8: 执行 `git rm --cached sql/idle_items_school.sql` 移除数据库导出

- [ ] Task 3: 清理配置文件中的硬编码凭据
  - [ ] SubTask 3.1: 修改 `docker-compose.yml`，将 `MYSQL_ROOT_PASSWORD: root` 和 `SPRING_DATASOURCE_PASSWORD: root` 替换为环境变量引用，并添加 `.env` 文件说明
  - [ ] SubTask 3.2: 修改 `backend/src/main/resources/application-dev.yml`，将 `jwt.secret` 的默认值从硬编码密钥改为占位符
  - [ ] SubTask 3.3: 修改 `backend/src/main/resources/application-staging.yml`，将 `jwt.secret` 的默认值从硬编码密钥改为占位符
  - [ ] SubTask 3.4: 创建 `docker-compose.override.yml.example` 作为本地开发覆盖配置模板

- [ ] Task 4: 提交所有变更并推送到 GitHub
  - [ ] SubTask 4.1: 执行 `git add .gitignore` 暂存更新后的忽略规则
  - [ ] SubTask 4.2: 执行 `git add` 暂存清理后的配置文件变更
  - [ ] SubTask 4.3: 使用规范的提交信息执行 `git commit`
  - [ ] SubTask 4.4: 执行 `git push origin main` 推送到远程仓库

- [ ] Task 5: 验证推送结果
  - [ ] SubTask 5.1: 执行 `git status` 确认工作区干净
  - [ ] SubTask 5.2: 检查 GitHub 仓库页面确认敏感文件已移除
  - [ ] SubTask 5.3: 确认 `.env.example` 仍保留在仓库中

# Task Dependencies

- Task 1 → Task 2（先更新 .gitignore，再移除跟踪）
- Task 2 → Task 3（移除跟踪后再清理配置文件）
- Task 3 → Task 4（所有清理完成后提交推送）
- Task 4 → Task 5（推送后验证）
