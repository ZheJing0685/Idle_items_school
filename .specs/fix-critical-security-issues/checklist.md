# Checklist: fix-critical-security-issues

## docker-compose.yml
- [ ] 无硬编码的数据库用户名（如 `root`）
- [ ] 无硬编码的数据库密码（如 `root`）
- [ ] 无硬编码的 JWT 密钥
- [ ] HTTP_PROXY/HTTPS_PROXY 可通过环境变量覆盖，默认不影响本地开发
- [ ] `grep -E "root|password.*:|secret" docker-compose.yml` 仅匹配环境变量引用

## application.yml
- [ ] `app.encryption.secret-key` 无默认回退值
- [ ] 配置文件无其他硬编码密钥

## PasswordResetServiceImpl.java
- [ ] `log.info` 中不含验证码明文
- [ ] 无 `System.out.println` 输出
- [ ] 使用 `java.security.SecureRandom` 替代 `java.util.Random`
- [ ] 使用 `BusinessException(ErrorCode.XXX, ...)` 替代 `IllegalArgumentException`
- [ ] 密码重置成功后清理频率限制计数 key（`password_reset:count:`前缀）
- [ ] import 语句正确（BusinessException, ErrorCode, SecureRandom）

## DataEncryptionUtil.java
- [ ] 使用 `AES/GCM/NoPadding` 替代 `AES/ECB/PKCS5Padding`
- [ ] 加密时生成随机 IV（12 字节）并前置到密文
- [ ] 解密时从密文头部正确提取 IV
- [ ] 密钥派生使用 SHA-256 哈希替代简单截断/补零
- [ ] 无硬编码默认密钥（`@Value` 无默认值）
- [ ] 异常日志使用 `log.error("...", e)` 记录完整堆栈
- [ ] import 含 `GCMParameterSpec`、`SecureRandom`
- [ ] 编译通过（`mvn compile`）

## 全局验证
- [ ] `git diff --stat` 确认仅修改了 4 个目标文件
- [ ] 无新增的安全敏感信息引入
