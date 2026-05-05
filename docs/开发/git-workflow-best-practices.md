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

---

## 七、.gitignore 维护指南

### 7.1 何时需要更新 .gitignore

| 场景 | 示例 | 说明 |
|------|------|------|
| 新增依赖/包管理器 | 引入 pnpm、Bun 等 | 需忽略对应的锁文件或缓存目录 |
| 新增开发工具 | 引入 IDE（如 VS Code 的 `.vscode/`）、Docker | 忽略工具生成的本地配置 |
| 新增环境配置 | 创建 `.env.staging`、`.env.production` | 忽略含敏感信息的环境文件 |
| 新增构建产物目录 | 自定义 `dist/`、`build/` 输出路径 | 忽略编译生成的文件 |
| 新增日志/临时文件 | 应用日志目录、临时缓存 | 忽略运行时生成的文件 |
| 新增上传/数据目录 | 用户上传文件、数据库导出 | 忽略运行时数据 |

### 7.2 如何正确添加新规则

**正确顺序（先更新规则，再清理缓存）：**

```bash
# 1. 编辑 .gitignore，添加新规则
echo "new-ignored-file.txt" >> .gitignore

# 2. 如果文件已被 Git 跟踪，需要从缓存中移除
git rm --cached new-ignored-file.txt

# 3. 如果是目录
git rm --cached -r some-directory/

# 4. 提交 .gitignore 变更
git add .gitignore
git commit -m "chore: update .gitignore"
```

> ⚠️ **注意：** `git rm --cached` 只从 Git 跟踪中移除，不会删除本地文件。如果直接使用 `git rm`（不带 `--cached`），本地文件也会被删除。

### 7.3 常见场景示例

**场景一：添加新的环境配置文件**

```bash
# 假设新增了 .env.staging
echo ".env.staging" >> .gitignore

# 如果 .env.staging 已被提交过
git rm --cached .env.staging
git add .gitignore
git commit -m "chore: ignore .env.staging"
```

**场景二：添加新的构建产物目录**

```bash
# 假设新增了 backend/target/ 目录
echo "backend/target/" >> .gitignore

# 如果该目录已被跟踪
git rm --cached -r backend/target/
git add .gitignore
git commit -m "chore: ignore backend/target/"
```

**场景三：忽略特定类型的文件**

```bash
# 忽略所有 .log 文件
echo "*.log" >> .gitignore

# 忽略某个目录下所有 .class 文件
echo "backend/**/*.class" >> .gitignore

# 但保留某个特定文件（使用 ! 取反）
echo "!important.log" >> .gitignore
```

### 7.4 验证方法

添加规则后，务必验证是否生效：

```bash
# 检查文件状态，确认被忽略的文件不再出现在变更列表中
git status

# 检查某个文件是否被忽略
git check-ignore -v <文件路径>

# 查看当前所有忽略规则
git status --ignored
```

### 7.5 本项目的 .gitignore 结构说明

本项目 `.gitignore` 按以下类别分组组织：

| 类别 | 包含内容 |
|------|----------|
| **IDE / 编辑器** | `.idea/`、`.vscode/`、`*.iml`、`.project` 等 |
| **操作系统** | `.DS_Store`、`Thumbs.db`、`Desktop.ini` 等 |
| **Java / Maven** | `*.class`、`*.jar`、`target/`、`.mvn/` 等 |
| **Node.js / 前端** | `node_modules/`、`dist/`、`.cache/` 等 |
| **环境变量** | `.env`、`.env.*`（保留 `.env.example`） |
| **日志** | `*.log`、`logs/` |
| **Docker** | `docker-compose.override.yml` |
| **上传文件** | `backend/uploads/` |
| **数据库** | `*.sql`（导出文件）、`*.sqlite` |

> 💡 **建议：** 修改 `.gitignore` 时，遵循现有的分组结构，将新规则添加到对应类别下，保持文件的可读性和可维护性。

---

## 八、GitHub CLI (gh) 常用命令速查

### 8.1 安装和认证

```bash
# 安装（Windows，使用 winget）
winget install GitHub.cli

# 安装（macOS）
brew install gh

# 安装（Linux / Ubuntu）
sudo apt install gh

# 登录认证
gh auth login

# 查看认证状态
gh auth status

# 使用 Token 认证（适用于 CI/CD）
gh auth login --with-token < token.txt

# 刷新认证
gh auth refresh
```

### 8.2 仓库操作

```bash
# 克隆仓库
gh repo clone owner/repo

# 创建新仓库
gh repo create my-project --public
gh repo create my-project --private

# 查看仓库信息
gh repo view
gh repo view owner/repo

# 在浏览器中打开仓库
gh repo view --web

# Fork 仓库
gh repo fork owner/repo

# 查看仓库列表
gh repo list
gh repo list owner --limit 20
```

### 8.3 Issue 操作

```bash
# 创建 Issue
gh issue create --title "Bug: 登录页面崩溃" --body "描述..."

# 交互式创建 Issue
gh issue create

# 列出 Issue
gh issue list
gh issue list --state open
gh issue list --label bug
gh issue list --assignee @me

# 查看 Issue 详情
gh issue view 42

# 关闭 Issue
gh issue close 42

# 重新打开 Issue
gh issue reopen 42

# 添加评论
gh issue comment 42 --body "已修复，请验证"
```

### 8.4 Pull Request 操作

```bash
# 创建 PR（从当前分支）
gh pr create --title "feat: 新功能" --body "描述..."

# 交互式创建 PR
gh pr create

# 创建 Draft PR
gh pr create --draft

# 列出 PR
gh list
gh pr list --state open
gh pr list --state merged
gh pr list --base develop

# 查看 PR 详情
gh pr view 15

# 在浏览器中打开 PR
gh pr view 15 --web

# 合并 PR
gh pr merge 15
gh pr merge 15 --squash
gh pr merge 15 --merge
gh pr merge 15 --rebase

# 检出 PR 分支到本地（Code Review 用）
gh pr checkout 15

# 审查 PR
gh pr review 15 --approve
gh pr review 15 --request-changes --body "请修改..."

# 查看 PR 的 diff
gh pr diff 15
```

### 8.5 GitHub Actions 操作

```bash
# 列出最近的 workflow 运行
gh run list

# 查看特定运行的详情
gh run view <run-id>

# 实时监控运行状态
gh run watch <run-id>

# 查看运行日志
gh run view <run-id> --log

# 手动触发 workflow
gh workflow run <workflow-name>

# 查看 workflow 列表
gh workflow list

# 重新运行失败的 workflow
gh run rerun <run-id>
```

### 8.6 本项目常用命令组合示例

```bash
# 完整的功能开发流程
# 1. 同步 develop 并创建功能分支
git checkout develop && git pull origin develop
git checkout -b feature/item-search

# 2. 开发完成后推送并创建 PR
git push origin feature/item-search
gh pr create --base develop --title "feat(item): add search functionality" --body "实现物品搜索功能"

# 3. 查看 CI 状态
gh run list --branch feature/item-search

# 4. PR 合并后清理
gh pr merge <pr-number> --squash
git checkout develop && git pull origin develop
git branch -d feature/item-search
git push origin --delete feature/item-search

# 快速查看项目状态
gh issue list --assignee @me          # 查看分配给我的 Issue
gh pr list --state open               # 查看待处理的 PR
gh run list --limit 5                 # 查看最近 5 次 CI 运行
```

---

## 九、Pre-commit Hook 配置

### 9.1 为什么需要 Pre-commit Hook

Pre-commit hook 在每次 `git commit` 执行前自动运行检查脚本，可以：

| 检查项 | 作用 | 风险等级 |
|--------|------|----------|
| 敏感信息检测 | 防止密码、密钥等被提交到仓库 | 🔴 严重 |
| 大文件检测 | 防止二进制文件等大文件膨胀仓库 | 🟡 中等 |
| 调试代码检测 | 防止 `console.log`、`TODO` 等调试代码被提交 | 🟢 低 |

> 💡 **核心价值：** 将问题拦截在提交之前，避免事后清理（force push、轮换密钥等高风险操作）。

### 9.2 手动配置方法

Git hook 存放在 `.git/hooks/` 目录下。创建 `pre-commit` 文件即可：

```bash
# 进入项目目录
cd /d/Project/Idle_items_school

# 创建 pre-commit hook
touch .git/hooks/pre-commit

# 赋予执行权限（Linux/macOS）
chmod +x .git/hooks/pre-commit
```

> ⚠️ **注意：** `.git/hooks/` 目录不会被 Git 跟踪，因此 hook 配置不会随仓库分发。团队协作时，建议将 hook 脚本放在项目目录（如 `scripts/hooks/`）中，并在文档中说明安装方式。

### 9.3 Pre-commit 脚本示例

将以下内容保存为 `.git/hooks/pre-commit`：

```bash
#!/bin/bash

# ============================================================
# Pre-commit Hook - 提交前自动检查
# ============================================================

RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

ERRORS=0

echo -e "${GREEN}Running pre-commit checks...${NC}"

# ----------------------------------------------------------
# 1. 敏感信息检测
# ----------------------------------------------------------
echo -e "\n${YELLOW}[1/3] Checking for sensitive information...${NC}"

SENSITIVE_PATTERNS=(
    "password\s*[:=]"
    "secret\s*[:=]"
    "api_key\s*[:=]"
    "apikey\s*[:=]"
    "token\s*[:=]"
    "private_key"
    "BEGIN RSA PRIVATE KEY"
    "BEGIN OPENSSH PRIVATE KEY"
    "BEGIN EC PRIVATE KEY"
)

for pattern in "${SENSITIVE_PATTERNS[@]}"; do
    MATCHES=$(git diff --cached --diff-filter=ACM | grep -inE "$pattern" | grep -v "^--" || true)
    if [ -n "$MATCHES" ]; then
        echo -e "${RED}  ✗ Potential sensitive information found:${NC}"
        echo "$MATCHES" | head -10
        ERRORS=$((ERRORS + 1))
    fi
done

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}  ✓ No sensitive information detected${NC}"
fi

# ----------------------------------------------------------
# 2. 大文件检测（超过 1MB）
# ----------------------------------------------------------
echo -e "\n${YELLOW}[2/3] Checking for large files (>1MB)...${NC}"

LARGE_FILES=$(git diff --cached --name-only --diff-filter=ACM | while read -r file; do
    if [ -f "$file" ]; then
        FILE_SIZE=$(wc -c < "$file" 2>/dev/null || echo 0)
        if [ "$FILE_SIZE" -gt 1048576 ]; then
            SIZE_MB=$(echo "scale=2; $FILE_SIZE / 1048576" | bc 2>/dev/null || echo "unknown")
            echo "  $file (${SIZE_MB}MB)"
        fi
    fi
done)

if [ -n "$LARGE_FILES" ]; then
    echo -e "${RED}  ✗ Large files detected:${NC}"
    echo "$LARGE_FILES"
    echo -e "${YELLOW}  Consider using Git LFS for large files${NC}"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}  ✓ No large files detected${NC}"
fi

# ----------------------------------------------------------
# 3. 调试代码检测
# ----------------------------------------------------------
echo -e "\n${YELLOW}[3/3] Checking for debug code...${NC}"

DEBUG_PATTERNS=(
    "console\.log"
    "console\.debug"
    "System\.out\.print"
    "System\.err\.print"
    "debugger;"
    "// TODO"
    "// FIXME"
    "// HACK"
)

DEBUG_FOUND=0
for pattern in "${DEBUG_PATTERNS[@]}"; do
    MATCHES=$(git diff --cached --diff-filter=ACM | grep -n "$pattern" | grep -v "^--" || true)
    if [ -n "$MATCHES" ]; then
        if [ $DEBUG_FOUND -eq 0 ]; then
            echo -e "${YELLOW}  ⚠ Debug code detected (warnings, not blocking):${NC}"
            DEBUG_FOUND=1
        fi
        echo "$MATCHES" | head -5
    fi
done

if [ $DEBUG_FOUND -eq 0 ]; then
    echo -e "${GREEN}  ✓ No debug code detected${NC}"
fi

# ----------------------------------------------------------
# 结果汇总
# ----------------------------------------------------------
echo ""
if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}✗ Pre-commit check failed with $ERRORS error(s).${NC}"
    echo -e "${RED}  Please fix the issues above before committing.${NC}"
    echo -e "${YELLOW}  To bypass this check (not recommended): git commit --no-verify${NC}"
    exit 1
else
    echo -e "${GREEN}✓ All pre-commit checks passed.${NC}"
    exit 0
fi
```

### 9.4 启用和禁用 Hook

**启用：**

```bash
# 确保脚本有执行权限
chmod +x .git/hooks/pre-commit

# 验证 hook 存在且可执行
ls -la .git/hooks/pre-commit
```

**临时禁用（单次提交跳过检查）：**

```bash
# 使用 --no-verify 跳过 pre-commit hook
git commit --no-verify -m "chore: emergency commit"
```

**永久禁用：**

```bash
# 重命名 hook 文件即可禁用
mv .git/hooks/pre-commit .git/hooks/pre-commit.disabled

# 重新启用
mv .git/hooks/pre-commit.disabled .git/hooks/pre-commit
```

### 9.5 团队共享 Hook 配置

由于 `.git/hooks/` 不会被 Git 跟踪，团队协作时建议：

```bash
# 1. 将 hook 脚本放在项目目录中
mkdir -p scripts/hooks
cp .git/hooks/pre-commit scripts/hooks/pre-commit

# 2. 提交到仓库
git add scripts/hooks/pre-commit
git commit -m "chore: add shared pre-commit hook script"

# 3. 团队成员安装 hook
# 方式一：手动复制
cp scripts/hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

# 方式二：使用符号链接（推荐）
ln -sf ../../scripts/hooks/pre-commit .git/hooks/pre-commit

# 方式三：配置 Git hooks 路径（Git 2.9+）
git config core.hooksPath scripts/hooks
```

> 💡 **推荐方式：** 使用 `git config core.hooksPath` 配置 hooks 路径，一劳永逸，且后续更新 hook 脚本只需拉取最新代码即可。
