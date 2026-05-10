# fix-critical-security-issues

## Why

代码审查发现了 4 个高危安全问题，必须在推送到 GitHub 之前修复：
1. docker-compose.yml 中数据库凭据、JWT 密钥等敏感信息硬编码，一旦推送到公开仓库将永久泄露
2. PasswordResetServiceImpl 将验证码明文写入日志，任何能访问日志的人都可以重置他人密码
3. DataEncryptionUtil 使用不安全的 AES-ECB 模式，相同明文产生相同密文，无法提供语义安全性
4. DataEncryptionUtil 和 application.yml 中硬编码默认加密密钥，生产环境若忘记配置会导致加密完全失效

## What Changes

### 修复范围
| 文件 | 修复内容 |
|------|---------|
| `docker-compose.yml` | 所有硬编码凭据改为 `${VAR}` 环境变量引用 |
| `backend/src/main/resources/application.yml` | 移除加密密钥默认回退值 |
| `backend/src/main/java/.../service/impl/PasswordResetServiceImpl.java` | 移除验证码日志输出，改用 `SecureRandom`，改用 `BusinessException` |
| `backend/src/main/java/.../util/DataEncryptionUtil.java` | AES-ECB → AES-GCM，移除硬编码默认密钥，修复日志 |

### 不修复
- 中危和低危问题（RateLimitFilter IP伪造、XssFilter绕过等）不在本次范围，后续单独处理
- 不新增 `.env` 文件（避免引入未被 `.gitignore` 覆盖的文件泄露风险）

## Impact

- **安全性提升**：修复所有高危漏洞，代码可安全推送到公开仓库
- **DataEncryptionUtil 变更影响**：AES-ECB → AES-GCM 是**破坏性变更**，旧密文无法用新模式解密。需确认当前无已加密的持久化数据（经搜索，`DataEncryptionUtil` 当前无调用方，可安全切换）
- **docker-compose.yml 变更影响**：部署者需设置环境变量（`SPRING_DATASOURCE_USERNAME` 等）才能启动

## ADDED
（无新增文件）

## MODIFIED
- `docker-compose.yml` — 硬编码凭据 → 环境变量引用
- `backend/src/main/resources/application.yml` — 移除加密密钥默认值
- `backend/src/main/java/com/idleitems/school/service/impl/PasswordResetServiceImpl.java` — 安全加固
- `backend/src/main/java/com/idleitems/school/util/DataEncryptionUtil.java` — 加密算法升级

## REMOVED
- `DataEncryptionUtil.java` 中的 `prepareKey()` 方法（简单截断/补零 → 替换为标准密钥派生）
- `PasswordResetServiceImpl.java` 中的 `System.out.println` 和验证码日志
