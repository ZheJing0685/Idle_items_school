# Cloudflare Tunnel 性能优化指南

## 目录
1. [快速开始](#快速开始)
2. [问题诊断](#问题诊断)
3. [优化方案](#优化方案)
4. [高级配置](#高级配置)
5. [监控与维护](#监控与维护)

---

## 快速开始

### 一键启动优化隧道
```batch
# Windows
start-optimized-tunnel.bat
```

### 或手动启动
```bash
# 启动优化后的隧道
node dev-tunnel-optimized.js
```

---

## 问题诊断

### 运行性能监控
```bash
# 替换为你的tunnel URL
node monitor-tunnel.js https://your-tunnel.trycloudflare.com
```

### 常见问题排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 高延迟 (>200ms) | 网络距离远、节点选择差 | 使用QUIC协议、选择更近的节点 |
| 低吞吐量 | 带宽限制、连接数限制 | 优化TCP参数、增加连接数 |
| 连接不稳定 | 网络波动、服务器负载 | 启用重试机制、使用长连接 |
| TLS握手慢 | 证书验证、协议选择 | 使用QUIC、跳过不必要的验证 |

---

## 优化方案

### 1. 协议优化

#### 启用QUIC协议
QUIC比HTTP/2更快，支持多路复用，减少连接建立时间。

```yaml
# cloudflared-config.yml
protocol: quic
```

#### 修改dev-tunnel.js
```javascript
const cloudflaredArgs = [
  'tunnel',
  '--url', `http://localhost:${PORT}`,
  '--protocol', 'quic',           // 使用QUIC协议
  '--no-tls-verify',              // 跳过TLS验证（开发环境）
  '--ha-connections', '4',        // 高可用连接数
];
```

### 2. 连接优化

#### 长连接配置
```yaml
ingress:
  - hostname: localhost
    service: http://localhost:5173
    originRequest:
      keepAliveConnections: 100  # 保持长连接数
      keepAliveTimeout: 90s      # keepalive超时
      tcpKeepAlive: 30s          # TCP keepalive
```

#### 超时优化
```yaml
originRequest:
  connectTimeout: 30s        # 连接超时
  tlsTimeout: 10s            # TLS超时
```

### 3. 本地服务优化

#### Spring Boot优化
```yaml
# application-dev.yml
server:
  tomcat:
    max-threads: 200         # 最大线程数
    accept-count: 100        # 等待队列
    connection-timeout: 5000 # 连接超时
  compression:
    enabled: true            # 启用压缩
    mime-types: text/html,text/xml,text/plain,text/css,application/javascript,application/json
    min-response-size: 1024  # 最小压缩大小
```

#### Vite优化
```typescript
// vite.config.ts
export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 5173,
    strictPort: true,
    // 添加优化配置
    fs: {
      strict: false
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus'],
          charts: ['echarts', 'vue-echarts']
        }
      }
    }
  }
})
```

### 4. 网络层优化

#### Windows TCP优化
```batch
# network-optimization.bat
netsh int tcp set global chimney=enabled
netsh int tcp set global dca=enabled
netsh int tcp set global netdma=enabled
netsh int tcp set global autotuninglevel=normal
```

#### DNS优化
```bash
# 使用Cloudflare DNS
netsh int ip set dns "Ethernet" static 1.1.1.1
netsh int ip add dns "Ethernet" 1.0.0.1 index=2

# 清除DNS缓存
ipconfig /flushdns
```

#### MTU优化
```bash
# 测试最佳MTU
ping -f -l 1472 1.1.1.1

# 如果失败，尝试
ping -f -l 1400 1.1.1.1
```

### 5. 前端优化

#### 资源压缩
```javascript
// vite.config.ts 添加压缩插件
import compression from 'vite-plugin-compression';

export default defineConfig({
  plugins: [
    vue(),
    compression({
      algorithm: 'gzip',
      ext: '.gz'
    })
  ]
})
```

#### 图片优化
```typescript
// 使用WebP格式
// 在Element Plus中配置
import { ElImage } from 'element-plus';

// 优化图片加载
const imageConfig = {
  lazy: true,
  previewSrcList: [],
  fit: 'contain'
};
```

---

## 高级配置

### 1. 命名隧道配置

```bash
# 创建命名隧道
cloudflared tunnel create my-tunnel

# 配置隧道
cloudflared tunnel route dns my-tunnel my-domain.com

# 启动隧道
cloudflared tunnel --config cloudflared-config.yml run my-tunnel
```

### 2. 多入口配置

```yaml
ingress:
  # 主应用
  - hostname: app.my-domain.com
    service: http://localhost:5173
  
  # API服务
  - hostname: api.my-domain.com
    service: http://localhost:7000
  
  # WebSocket服务
  - hostname: ws.my-domain.com
    service: http://localhost:7001
  
  # 默认规则
  - service: http_status:404
```

### 3. 安全配置

```yaml
# 仅允许特定IP访问
ingress:
  - hostname: localhost
    service: http://localhost:5173
    originRequest:
      noTLSVerify: true
      connectTimeout: 30s
    access:
      - type: ip
        ip: 192.168.1.0/24
```

### 4. 负载均衡

```yaml
# 多个后端服务
ingress:
  - hostname: localhost
    service: round-robin:
      - http://localhost:7001
      - http://localhost:7002
      - http://localhost:7003
```

---

## 监控与维护

### 1. 实时监控

```bash
# 监控隧道状态
cloudflared tunnel info my-tunnel

# 查看实时日志
cloudflared tunnel logs my-tunnel -f
```

### 2. 性能测试

```bash
# 运行完整性能测试
node monitor-tunnel.js https://your-tunnel.trycloudflare.com

# 测试下载速度
curl -o /dev/null -w "Speed: %{speed_download} bytes/sec\n" https://your-tunnel.trycloudflare.com/test-file
```

### 3. 问题排查

```bash
# 检查隧道连接
cloudflared tunnel test my-tunnel

# 测试源站连接
curl -H "Host: your-domain.com" http://localhost:5173

# 检查DNS解析
nslookup your-domain.com
dig your-domain.com
```

### 4. 日志分析

```bash
# 导出日志
cloudflared tunnel logs my-tunnel > tunnel.log

# 分析错误
grep -i "error" tunnel.log
grep -i "timeout" tunnel.log
```

---

## 性能基准

### 预期性能指标

| 指标 | 优秀 | 良好 | 中等 | 需优化 |
|------|------|------|------|--------|
| DNS解析 | <50ms | <100ms | <200ms | >200ms |
| Ping延迟 | <50ms | <100ms | <200ms | >200ms |
| TLS握手 | <200ms | <500ms | <1000ms | >1000ms |
| 页面加载 | <1s | <2s | <3s | >3s |
| 下载速度 | >10MB/s | >5MB/s | >1MB/s | <1MB/s |

### 优化效果对比

| 优化项 | 预期提升 | 实施难度 |
|--------|----------|----------|
| QUIC协议 | 20-30% | 简单 |
| 长连接 | 15-25% | 简单 |
| TCP优化 | 10-20% | 中等 |
| 压缩 | 30-50% | 简单 |
| 图片优化 | 20-40% | 中等 |

---

## 常见问题解答

### Q: 为什么我的隧道速度还是慢？

**A:** 请按以下步骤排查：
1. 运行 `node monitor-tunnel.js` 查看详细性能数据
2. 检查本地网络连接质量
3. 确认已启用QUIC协议
4. 检查Spring Boot是否启用压缩
5. 查看cloudflared日志是否有错误

### Q: 如何选择最佳的边缘节点？

**A:** Cloudflare会自动选择最优节点，但你可以：
1. 使用 `tracert` 查看路由路径
2. 测试不同地区的访问速度
3. 考虑使用Cloudflare的企业版中国网络

### Q: 开发环境和生产环境有什么区别？

**A:** 
- 开发环境：使用Quick Tunnel，每次随机域名
- 生产环境：使用命名隧道，固定域名，更稳定

### Q: 如何监控隧道性能？

**A:** 使用提供的监控脚本：
```bash
node monitor-tunnel.js https://your-tunnel.trycloudflare.com
```

---

## 更新日志

### v1.0.0
- 初始版本
- 基础优化方案
- 性能监控脚本

---

## 相关资源

- [Cloudflare Tunnel 官方文档](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/)
- [QUIC 协议说明](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/get-started/tunnel-useful-terms/#quic)
- [Spring Boot 性能优化](https://docs.spring.io/spring-boot/docs/current/reference/html/performance.html)
- [Vite 性能优化](https://vitejs.dev/guide/performance.html)
