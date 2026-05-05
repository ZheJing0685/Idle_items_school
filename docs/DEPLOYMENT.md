# 学生闲置物品交易平台部署文档

## 1. 环境准备

### 1.1 硬件要求
- CPU: 2核及以上
- 内存: 4GB及以上
- 磁盘: 20GB及以上

### 1.2 软件要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 14+
- npm 6+

## 2. 后端部署

### 2.1 数据库配置
1. 启动MySQL服务
2. 创建数据库：
   ```sql
   CREATE DATABASE idle_items_school DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 执行初始化脚本：
   ```bash
   mysql -u root -p idle_items_school < sql/init.sql
   ```

### 2.2 后端服务部署
1. 编译打包：
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```
2. 运行服务：
   ```bash
   java -jar target/school-1.0.0.jar
   ```

### 2.3 配置说明
- 配置文件：`backend/src/main/resources/application.yml`
- 数据库连接信息：
  - url: jdbc:mysql://localhost:3306/idle_items_school
  - username: root
  - password: root
- 文件上传路径：`./uploads/`
- 服务端口：8080

## 3. 前端部署

### 3.1 构建前端
1. 安装依赖：
   ```bash
   cd frontend
   npm install
   ```
2. 构建生产版本：
   ```bash
   npm run build
   ```

### 3.2 部署前端
1. 将构建产物 `dist` 目录部署到Nginx或其他Web服务器
2. Nginx配置示例：
   ```nginx
   server {
       listen 80;
       server_name localhost;

       location / {
           root /path/to/dist;
           index index.html;
           try_files $uri $uri/ /index.html;
       }

       location /api {
           proxy_pass http://localhost:8080/api;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
   ```

## 4. Docker部署

### 4.1 Docker Compose配置
创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: mysql-idle-items
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: idle_items_school
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: backend-idle-items
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/idle_items_school
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: frontend-idle-items
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql-data:
```

### 4.2 Dockerfile配置

#### 后端Dockerfile (`backend/Dockerfile`)
```dockerfile
FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/school-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 前端Dockerfile (`frontend/Dockerfile`)
```dockerfile
FROM node:14-alpine as build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

#### Nginx配置 (`frontend/nginx.conf`)
```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 4.3 启动服务
```bash
docker-compose up -d
```

## 5. 访问方式

- 前端：http://localhost
- 后端API文档：http://localhost:8080/doc.html
- 后端API：http://localhost:8080/api

## 6. 常见问题

### 6.1 数据库连接失败
- 检查MySQL服务是否启动
- 检查数据库连接信息是否正确
- 检查网络连接是否正常

### 6.2 图片上传失败
- 检查文件上传路径是否存在且有写入权限
- 检查文件大小是否超过限制
- 检查图片格式是否正确

### 6.3 前端访问后端API失败
- 检查后端服务是否启动
- 检查Nginx配置是否正确
- 检查CORS配置是否正确

## 7. 系统维护

### 7.1 日志管理
- 后端日志：`backend/logs/`
- 前端日志：浏览器控制台

### 7.2 数据备份
- 定期备份MySQL数据库
- 定期备份上传的图片文件

### 7.3 性能优化
- 数据库索引优化
- 图片压缩优化
- 缓存策略优化
