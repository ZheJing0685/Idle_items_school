# Tasks: fix-critical-security-issues

## 依赖关系
```
T1 ──┐
T2 ──┼──→ 全部独立，可并行执行
T3 ──┤
T4 ──┘
```

## 任务列表

### T1: 修复 docker-compose.yml 硬编码凭据
- **文件**: `docker-compose.yml`
- **变更**: 
  1. 数据库用户名 `root` → `${SPRING_DATASOURCE_USERNAME}`
  2. 数据库密码 `root` → `${SPRING_DATASOURCE_PASSWORD}`
  3. JWT 密钥硬编码 → `${JWT_SECRET}`
  4. HTTP_PROXY/HTTPS_PROXY 硬编码 → `${HTTP_PROXY:-}` / `${HTTPS_PROXY:-}`（默认空，按需设置）
- **验证**: 检查文件中无任何凭据字面量

### T2: 修复 application.yml 加密密钥默认值
- **文件**: `backend/src/main/resources/application.yml`
- **变更**: `secret-key: "${ENCRYPTION_SECRET_KEY:defaultSecretKeyForDev1234567890!}"` → `secret-key: "${ENCRYPTION_SECRET_KEY}"`
- **验证**: 无默认回退值，未配置环境变量时启动应报错

### T3: 修复 PasswordResetServiceImpl.java
- **文件**: `backend/src/main/java/com/idleitems/school/service/impl/PasswordResetServiceImpl.java`
- **变更**:
  1. `import java.util.Random` → `import java.security.SecureRandom`
  2. `new Random()` → `new SecureRandom()`
  3. 删除 `log.info("密码重置验证码已生成，邮箱: {}, 验证码: {}", email, code)` 中的验证码输出
  4. 删除 `System.out.println("========== 密码重置验证码: " + code + " ==========")` 整行
  5. 所有 `new IllegalArgumentException(...)` → `new BusinessException(ErrorCode.XXX, ...)`
     - "该邮箱未注册" → `ErrorCode.USER_NOT_FOUND`
     - "发送过于频繁" → `ErrorCode.OPERATION_NOT_ALLOWED`
     - "验证码已过期"/"验证码错误" → `ErrorCode.BAD_REQUEST`
     - "用户不存在" → `ErrorCode.USER_NOT_FOUND`
  6. 添加 `import com.idleitems.school.common.BusinessException` 和 `import com.idleitems.school.common.ErrorCode`
  7. 密码重置成功后清理频率限制计数 key
- **验证**: 无明文验证码输出，使用标准异常类型

### T4: 修复 DataEncryptionUtil.java
- **文件**: `backend/src/main/java/com/idleitems/school/util/DataEncryptionUtil.java`
- **变更**:
  1. 移除 `@Value` 注入的默认值，改为 `@Value("${app.encryption.secret-key}")`
  2. `TRANSFORMATION` 从 `AES/ECB/PKCS5Padding` → `AES/GCM/NoPadding`
  3. 添加随机 IV（GCM 模式需要），加密时生成随机 12 字节 IV 并前置到密文中
  4. 解密时从密文头部提取 IV
  5. `prepareKey()` 改为使用 SHA-256 哈希派生固定长度密钥（不截断/补零）
  6. `log.error("加密失败: {}", e.getMessage())` → `log.error("加密失败", e)`（记录完整堆栈）
  7. `log.error("解密失败: {}", e.getMessage())` → `log.error("解密失败", e)`（记录完整堆栈）
  8. 添加必要的 import：`javax.crypto.spec.GCMParameterSpec`、`java.security.MessageDigest`、`java.security.SecureRandom`
- **验证**: 编译通过，加密解密往返测试正常
