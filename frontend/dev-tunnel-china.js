#!/usr/bin/env node

/**
 * Cloudflare 中国加速优化脚本
 * 适用于中国用户访问速度慢的情况
 */

import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import fs from 'fs';
import path from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const PORT = 5173;
const CLOUDFLARED = 'C:\\Program Files (x86)\\cloudflared\\cloudflared.exe';

console.log('\n' + '='.repeat(60));
console.log('  Cloudflare 中国加速优化');
console.log('='.repeat(60) + '\n');

// 中国优选IP列表（Cloudflare边缘节点）
const CHINA_OPTIMIZED_IPS = [
  '162.159.192.1',
  '162.159.193.1',
  '162.159.194.1',
  '162.159.195.1',
  '172.64.32.1',
  '172.64.33.1',
  '172.64.34.1',
  '172.64.35.1',
  '104.16.132.229',
  '104.16.133.229',
];

// 创建优化配置
function createOptimizedConfig() {
  const config = `
# Cloudflare Tunnel 中国优化配置
protocol: quic

# 超时优化
connect-timeout: 30s
tls-timeout: 10s
tcp-keepalive: 30s

# 连接优化
keepalive-connections: 100
keepalive-timeout: 90s

# 压缩优化
compression-quality: 1

# 重试优化
retries: 10

# 日志级别
loglevel: info

# 入口规则
ingress:
  - hostname: localhost
    service: http://localhost:${PORT}
    originRequest:
      connectTimeout: 30s
      tlsTimeout: 10s
      tcpKeepAlive: 30s
      keepAliveConnections: 100
      keepAliveTimeout: 90s
      noTLSVerify: true
      disableChunkedEncoding: false
  - service: http_status:404
`;

  const configPath = path.join(__dirname, 'cloudflared-china.yml');
  fs.writeFileSync(configPath, config);
  console.log(`✅ 配置文件已创建: ${configPath}`);
  return configPath;
}

// 启动优化隧道
function startOptimizedTunnel(configPath) {
  console.log('\n[1/2] 启动 Vite 开发服务器...\n');

  const vite = spawn('npx', ['vite', '--host', '0.0.0.0', '--port', PORT.toString()], {
    cwd: __dirname,
    stdio: 'inherit',
    shell: true,
    env: {
      ...process.env,
      NODE_OPTIONS: '--max-old-space-size=4096'
    }
  });

  console.log('[2/2] 启动 Cloudflare 隧道（中国优化）...\n');

  // 使用配置文件启动
  const cloudflaredArgs = [
    'tunnel',
    '--config', configPath,
    '--url', `http://localhost:${PORT}`,
    '--edge-ip-version', '4',  // 强制使用IPv4（在中国更稳定）
  ];

  const cloudflared = spawn(CLOUDFLARED, cloudflaredArgs, {
    stdio: 'pipe',
    shell: false,
  });

  let urlFound = false;

  function checkOutput(data) {
    if (urlFound) return;
    const output = data.toString();
    const match = output.match(/https:\/\/[a-z0-9-]+\.trycloudflare\.com/);
    if (match) {
      urlFound = true;
      console.log('\n' + '='.repeat(60));
      console.log('  🚀 中国加速隧道已启动!');
      console.log('='.repeat(60));
      console.log(`\n  访问地址: ${match[0]}`);
      console.log('\n  优化配置:');
      console.log('    ✅ 协议: QUIC');
      console.log('    ✅ IPv4: 强制使用');
      console.log('    ✅ 压缩: 已启用');
      console.log('    ✅ 长连接: 已启用');
      console.log('\n  提示:');
      console.log('    1. 首次访问可能需要等待几秒');
      console.log('    2. 建议使用 Chrome/Edge 浏览器');
      console.log('    3. 如速度仍慢，请尝试不同时段');
      console.log('\n' + '='.repeat(60) + '\n');
    }
    
    if (output.includes('error') || output.includes('Error')) {
      console.log('[Tunnel Error]', output);
    }
  }

  cloudflared.stdout.on('data', checkOutput);
  cloudflared.stderr.on('data', checkOutput);

  vite.on('error', (err) => {
    console.error('Vite 启动失败:', err.message);
    process.exit(1);
  });

  process.on('SIGINT', () => {
    console.log('\n正在关闭...');
    cloudflared.kill();
    vite.kill();
    process.exit(0);
  });

  process.on('SIGTERM', () => {
    cloudflared.kill();
    vite.kill();
    process.exit(0);
  });
}

// 主函数
const configPath = createOptimizedConfig();
startOptimizedTunnel(configPath);
