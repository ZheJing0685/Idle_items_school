# 验证检查清单

## 构建阶段

- [ ] `.dockerignore` 文件已创建，内容正确
- [ ] `pom.xml` 中 `hutool-all` 已移除
- [ ] `pom.xml` 中 knife4j 已移入 `dev` profile，`prod` profile 不包含
- [ ] `SwaggerConfig.java` 已添加 `@ConditionalOnProperty`
- [ ] Dockerfile 三阶段构建结构正确（builder → jre-build → runtime）
- [ ] `mvn clean package -P prod` 构建成功且不包含 knife4j
- [ ] `docker build` 命令执行成功，无错误

## 镜像体积

- [ ] `docker images` 显示镜像体积 ≤ **250 MB**
- [ ] `docker history` 显示 JRE 层从 141MB 降至 ≤ 50MB
- [ ] `docker history` 显示 JAR 层从 80MB 降至 ≤ 60MB（移除了 hutool + knife4j）

## 功能验证

- [ ] 容器启动成功（`docker run school-backend:test`）
- [ ] 健康检查端点 `/actuator/health` 返回 200
- [ ] API 接口正常响应（如 `/api/config` 等）
- [ ] 中文字体正常支持（如文件上传功能涉及图片处理时）
- [ ] 日志正常输出（UTF-8 编码正确）

## 构建产物

- [ ] 构建上下文大小明显减少（可用 `docker build --no-cache` 的 output 确认）
- [ ] 构建速度显著提升

## 回滚方案

- [ ] 保留原始 Dockerfile 备份
- [ ] 如有问题，可快速切回原镜像 `idle_items_school-backend:latest`
