# 贡献指南

感谢你对闲置物品校园交易平台项目的关注！我们欢迎任何形式的贡献。

## 如何贡献

### 报告 Bug

1. 在 [Issues](https://github.com/2790849976/Idle_items_school/issues) 中搜索是否已有相同问题
2. 如果没有，使用 **Bug Report** 模板创建新 Issue
3. 请尽可能提供详细的复现步骤和环境信息

### 提交功能建议

1. 在 Issues 中搜索是否已有类似建议
2. 如果没有，使用 **Feature Request** 模板创建新 Issue
3. 描述你的使用场景和期望的行为

### 提交代码

1. Fork 本仓库
2. 创建你的特性分支：`git checkout -b feature/my-feature`
3. 提交你的改动：`git commit -m 'feat: add some feature'`
4. 推送到你的分支：`git push origin feature/my-feature`
5. 创建一个 Pull Request

## 开发规范

### 提交信息

请遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` Bug 修复
- `docs:` 文档变更
- `style:` 代码格式（不影响代码运行的变更）
- `refactor:` 重构（既不是新增功能，也不是修改 bug 的代码变动）
- `test:` 增加测试
- `chore:` 构建过程或辅助工具的变动

### 代码风格

**前端 (Vue 3 + TypeScript):**
- 使用 ESLint + Prettier 格式化代码
- 组件命名使用 PascalCase
- 运行 `npm run lint` 确保代码风格一致

**后端 (Spring Boot + Java):**
- 使用 Qodana 进行代码质量检查
- 方法命名使用 camelCase
- 遵循 Spring Boot 最佳实践

### 测试

- 新功能必须附带单元测试
- Bug 修复必须附带回归测试
- 确保所有现有测试通过：

```bash
# 前端
cd frontend
npm run test:unit

# 后端
cd backend
mvn test
```

## 项目结构

```
Idle_items_school/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── deploy/           # Docker 部署配置
├── sql/              # 数据库脚本
└── docs/             # 文档
```

## 环境搭建

请参考 [README.md](README.md#快速开始) 中的环境搭建说明。

## 行为准则

参与本项目即表示你同意遵守我们的 [行为准则](CODE_OF_CONDUCT.md)。

## 问题反馈

如果你有任何问题或建议，欢迎通过以下方式联系我们：

- 在 GitHub Issues 中提出
- 发送邮件至项目维护者

感谢你的贡献！
