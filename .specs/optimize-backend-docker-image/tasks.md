# 实施任务

## 前置条件

- [x] 用户确认需要中文字体支持（保留 fontconfig/ttf-dejavu）
- [x] 调研确认 hutool-all 在源码中零使用
- [x] 调研确认 knife4j 生产配置已禁用

## 任务列表

### Task 1: 添加 `.dockerignore`

- **文件**: `backend/.dockerignore`
- **内容**: 排除 target/、logs/、.idea/、.env、*.log、*.md、.git、.gitignore
- **验证**: `docker build` 时不会再发送这些文件到 daemon

### Task 2: 修改 `pom.xml` — 依赖瘦身

- **2a** 移除 `hutool-all` 依赖声明（第 92-96 行）
- **2b** 添加 Maven profiles 声明，将 `knife4j` 移入 `dev` profile
- **依赖**: 无
- **验证**: `mvn clean package -P prod` 不包含 knife4j；`mvn clean package`（默认 dev）包含

### Task 3: 修改 `SwaggerConfig.java`

- **修改**: 在类上添加 `@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true")`
- **依赖**: Task 2
- **验证**: `application-prod.yml` 中 `knife4j.enable: false` 时，Spring 不加载该配置类

### Task 4: 重写 `Dockerfile`

- **4a** 第一阶段 builder：使用 `maven:3.9-eclipse-temurin-17-alpine`，复制 pom.xml → 下载依赖 → 复制源码 → `mvn package`
- **4b** 第二阶段 jre-build：使用 `eclipse-temurin:17-jre-alpine`，安装 fontconfig/ttf-dejavu → `jlink` 定制 JRE
- **4c** 第三阶段 runtime：使用 `alpine:3.19`，COPY 定制 JRE + JAR → EXPOSE → ENTRYPOINT
- **依赖**: Task 2
- **验证**: `docker build -t school-backend:test .` 成功

### Task 5: 构建与验证

- **5a** 执行 `docker build -t school-backend:test .`
- **5b** 检查 `docker images school-backend:test` 体积
- **5c** 检查 `docker history school-backend:test` 各层大小
- **5d** 启动容器，检查 API 是否正常响应
- **依赖**: Task 4

## 任务依赖图

```
Task 1 (.dockerignore)     Task 2 (pom.xml)
         |                       |
         |             +---------+---------+
         |             |                   |
         |        Task 3 (SwaggerConfig)   |
         |             |                   |
         +-------------+-------------------+
                       |
                    Task 4 (Dockerfile)
                       |
                    Task 5 (验证)
```
