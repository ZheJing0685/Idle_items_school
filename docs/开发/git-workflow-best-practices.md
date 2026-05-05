# Git 工作流最佳实践

## 一、分支规范

### 分支模型

| 分支 | 用途 | 命名规则 | 生命周期 |
|------|------|----------|----------|
| `main` | 生产环境代码，始终保持可部署 | `main` | 永久 |
| `develop` | 开发集成分支，日常开发合并到此 | `develop` | 永久 |
| `feature/*` | 新功能开发 | `feature/功能描述`（英文，kebab-case） | 完成后合并到 `develop` 并删除 |
| `hotfix/*` | 生产环境紧急修复 | `hotfix/问题描述` | 修复后合并到 `main` 和 `develop` 并删除 |
| `release/*` | 版本发布准备 | `release/v1.0.0` | 发布后合并到 `main` 并删除 |

### 分支操作流程

```bash
# 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-rating

# 开发完成后合并
git checkout develop
git pull origin develop
git merge --no-ff feature/user-rating
git push origin develop
git branch -d feature/user-rating
git push origin --delete feature/user-rating

# 紧急修复
git checkout main
git pull origin main
git checkout -b hotfix/login-crash
# ... 修复 ...
git checkout main
git merge --no-ff hotfix/login-crash
git tag -a v1.0.1 -m "Fix login crash"
git checkout develop
git merge --no-ff hotfix/login-crash
git branch -d hotfix/login-crash
```

### 分支保护规则（GitHub Settings）

- `main` 分支：禁止直接推送，必须通过 PR 合并
- `develop` 分支：建议禁止直接推送
- 所有 PR 必须至少 1 人 Code Review

---

## 二、提交信息规范

采用 **Conventional Commits** 规范。

### 格式

```
<type>(<scope>): <subject>

[body]

[footer]
```

### Type 类型

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(auth): add JWT refresh token` |
| `fix` | 修复 Bug | `fix(item): fix image upload 500 error` |
| `docs` | 文档变更 | `docs: update API documentation` |
| `style` | 代码格式（不影响逻辑） | `style: fix indentation in UserController` |
| `refactor` | 重构（非新功能、非修复） | `refactor(user): extract validation logic` |
| `perf` | 性能优化 | `perf(query): add index for item search` |
| `test` | 测试相关 | `test(auth): add unit tests for login` |
| `chore` | 构建/工具变更 | `chore: update .gitignore` |
| `ci` | CI/CD 配置 | `ci: add GitHub Actions workflow` |
| `revert` | 回滚 | `revert: revert "feat(auth): add OAuth"` |

### Scope 范围（本项目常用）

`auth`、`user`、`item`、`order`、`admin`、`api`、`frontend`、`backend`、`docker`、`db`

### 示例

```
feat(item): add item category filtering

- Add category dropdown to item list page
- Add backend API endpoint for category-based filtering
- Update ItemMapper with new query method

Closes #42
```

```
fix(auth): fix JWT token expiration not enforced

The token expiration check was missing in JwtAuthenticationFilter,
causing expired tokens to still be accepted.

Fixes #108
```

---

## 三、隐私保护最佳实践

### 3.1 绝对禁止提交到仓库的内容

| 类型 | 示例 | 风险等级 |
|------|------|----------|
| 数据库密码 | `MYSQL_ROOT_PASSWORD: root` | 🔴 严重 |
| JWT 密钥 | `jwt.secret: my-secret-key` | 🔴 严重 |
| API 密钥 | 第三方服务 API Key | 🔴 严重 |
| 用户数据 | 数据库导出含真实用户信息 | 🔴 严重 |
| 用户上传文件 | 图片、文档等运行时数据 | 🟡 中等 |
| 环境配置 | `.env.production` 含生产地址 | 🟡 中等 |
| 测试报告 | 覆盖率报告、录屏文件 | 🟢 低 |

### 3.2 敏感信息处理方式

**密码和密钥：**
- 使用环境变量：`${VARIABLE_NAME}`
- 使用 `.env` 文件（已在 `.gitignore` 中忽略）
- 提供 `.env.example` 作为模板（不含真实值）

**数据库导出：**
- 导出前脱敏：替换真实用户名、邮箱、手机号
- 或使用 `scripts/` 下的脚本生成测试数据替代

**用户上传文件：**
- 存储在 `backend/uploads/` 目录（已在 `.gitignore` 中忽略）
- 生产环境使用对象存储（OSS/S3），不依赖本地文件系统

### 3.3 提交前自检清单

每次 `git commit` 前执行：

```bash
# 查看将要提交的变更
git diff --cached

# 检查是否包含敏感关键词
git diff --cached | grep -iE "(password|secret|key|token|api_key|private)" 

# 确认没有大文件
git diff --cached --stat
```

### 3.4 如果意外提交了敏感信息

```bash
# 1. 立即从 Git 跟踪中移除
git rm --cached <敏感文件>

# 2. 更新 .gitignore
echo "<敏感文件>" >> .gitignore

# 3. 提交清理
git commit -m "chore: remove sensitive file from tracking"

# 4. 强制推送（覆盖远程历史）
git push origin main --force

# 5. 立即轮换泄露的凭据
#    - 更改数据库密码
#    - 重新生成 JWT 密钥
#    - 撤销泄露的 API Key
```

> ⚠️ **注意：** `git push --force` 会覆盖远程历史。如果其他人已拉取旧代码，需要通知他们重新克隆。对于严重泄露，即使 force push 后，GitHub 缓存中可能仍保留旧数据，建议联系 GitHub 支持清除缓存。

---

## 四、标准化提交/推送流程

### 日常开发流程

```bash
# 1. 确保在正确的分支上
git checkout develop
git pull origin develop

# 2. 创建功能分支
git checkout -b feature/your-feature

# 3. 开发并提交（小步提交）
git add <相关文件>
git commit -m "feat(scope): description"

# 4. 定期同步主分支
git fetch origin
git rebase origin/develop

# 5. 推送到远程
git push origin feature/your-feature

# 6. 创建 Pull Request
# 在 GitHub 上创建 PR，填写描述，请求 Review

# 7. PR 合并后清理
git checkout develop
git pull origin develop
git branch -d feature/your-feature
git push origin --delete feature/your-feature
```

### 发布流程

```bash
# 1. 从 develop 创建 release 分支
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0

# 2. 最终测试和修复
# ... 仅修复 Bug，不添加新功能 ...

# 3. 合并到 main 并打标签
git checkout main
git merge --no-ff release/v1.0.0
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin main --tags

# 4. 合并回 develop
git checkout develop
git merge --no-ff release/v1.0.0
git push origin develop

# 5. 删除 release 分支
git branch -d release/v1.0.0
git push origin --delete release/v1.0.0
```

---

## 五、常见错误规避

### 5.1 提交了不该提交的文件

**预防：** 始终使用 `git diff --cached` 检查暂存区内容再提交。

**修复：**
```bash
# 撤销最后一次提交（保留变更在工作区）
git reset HEAD~1

# 从暂存区移除不需要的文件
git reset HEAD <文件名>

# 重新提交
git add <正确文件>
git commit -m "..."
```

### 5.2 提交信息写错了

```bash
# 修改最后一次提交信息（未推送时）
git commit --amend -m "correct message"

# 已推送时，不建议修改，用新提交修正
git commit -m "docs: correct commit message for previous commit"
```

### 5.3 误删了文件

```bash
# 恢复工作区中被删除的文件
git checkout -- <文件名>

# 恢复已提交删除的文件
git checkout HEAD~1 -- <文件名>
```

### 5.4 合并冲突

```bash
# rebase 时遇到冲突
git rebase --abort  # 放弃 rebase
# 或
# 手动解决冲突后
git add <冲突文件>
git rebase --continue
```

### 5.5 推送被拒绝

```bash
# 原因：远程有本地没有的提交
# 正确做法：先拉取再推送
git pull origin main --rebase
git push origin main

# 绝对不要轻易使用 --force
# 只在明确知道后果时才 force push
```

### 5.6 仓库体积过大

```bash
# 检查大文件
git rev-list --objects --all | git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | sort -k3 -n -r | head -20

# 如果历史中有大文件，使用 git-filter-repo 清理
# pip install git-filter-repo
# git-filter-repo --path-glob '*.webm' --invert-paths
```

---

## 六、环境变量配置指南

### 本地开发

1. 复制模板文件：
   ```bash
   cp docker-compose.override.yml.example docker-compose.override.yml
   ```

2. 编辑 `docker-compose.override.yml`，填入本地密码

3. 前端环境变量：
   ```bash
   cp frontend/.env.example frontend/.env.development
   # 编辑 frontend/.env.development
   ```

### CI/CD 环境

在 GitHub Secrets 中配置：
- `MYSQL_ROOT_PASSWORD`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`

### 生产环境

使用服务器环境变量或密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault）。
