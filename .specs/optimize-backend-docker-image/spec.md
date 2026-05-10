# 后端 Docker 镜像体积优化

## Why

当前后端 Docker 镜像体积达到 **413MB**，远超合理范围，导致：
- 镜像拉取/推送耗时，CI/CD 流水线变慢
- 占用大量磁盘空间
- 构建上下文过大（109MB），每次构建都传输大量冗余文件

## What Changes

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `backend/.dockerignore` | **ADDED** | 排除 target、logs、.env 等无关文件，构建上下文从 109MB → <1MB |
| `backend/Dockerfile` | **MODIFIED** | 从单阶段改为三阶段构建：builder → jre-customizer → runtime |
| `backend/pom.xml` | **MODIFIED** | 移除 hutool-all（零使用）、knife4j 改为 Maven profile 管理 |
| `backend/src/main/java/.../config/SwaggerConfig.java` | **MODIFIED** | 添加 `@ConditionalOnProperty`，生产环境不加载 Bean |

## Impact

| 维度 | 优化前 | 优化后 |
|------|--------|--------|
| Docker 镜像体积 | ~413 MB | **~220-250 MB** |
| 构建上下文大小 | 109 MB | **<1 MB** |
| 构建速度 | 慢（传输 109MB） | **快（传输 <1MB）** |
| 功能影响 | — | 无（hutool 零使用，knife4j 生产本就已禁用） |
| 构建兼容性 | 依赖本地 mvn 预编译 | Docker 内完成全流程编译，无需本地安装 Maven |

## Detailed Changes

### 1. `.dockerignore` (新文件)

```
target/
logs/
.idea/
.env
*.log
*.md
.git
.gitignore
```

### 2. `pom.xml` 依赖调整

**移除：**
- `cn.hutool:hutool-all:5.8.26` — 源码中零使用，直接删除

**knife4j 改为 profile 隔离：**

在 `<profiles>` 中定义两个 profile：
- `dev`（默认激活）：包含 knife4j 依赖
- `prod`：不包含 knife4j 依赖

```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <dependencies>
            <!-- knife4j 仅 dev profile 引入 -->
        </dependencies>
    </profile>
    <profile>
        <id>prod</id>
    </profile>
</profiles>
```

`SwaggerConfig.java` 添加 `@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true")`，确保即使依赖存在也不会在 production 加载 Bean。

### 3. `Dockerfile` 多阶段构建

```
第一阶段 builder:
  镜像: maven:3.9-eclipse-temurin-17-alpine
  工作: 编译打包，生成 school-1.0.0.jar

第二阶段 jre-build:
  镜像: eclipse-temurin:17-jre-alpine
  工作: jlink 定制最小 JRE（仅包含 java.base, java.sql, java.naming 等必需模块）
         保留 fontconfig/ttf-dejavu（用户需要中文）
         使用 --strip-debug 缩减体积

第三阶段 runtime:
  镜像: alpine:3.19
  工作: 复制定制 JRE、构建产物、字体配置
        暴露 7000 端口
        设置 ENTRYPOINT
```

### 4. 关键依赖模块分析

| 依赖 | 体积（约） | 处理方式 |
|------|-----------|----------|
| hutool-all 5.8.26 | 20 MB | **删除**（零使用） |
| knife4j 及其依赖 | 10-12 MB | **移入 dev profile**，prod 构建不包含 |
| JRE 运行时 | 141 MB | **jlink 裁剪至 ~40MB** |
| fontconfig + ttf-dejavu | ~20 MB | **保留**（用户需要中文字体） |
