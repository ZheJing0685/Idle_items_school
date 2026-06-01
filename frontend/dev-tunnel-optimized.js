import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const PORT = 5173;
const CLOUDFLARED = 'C:\\Program Files (x86)\\cloudflared\\cloudflared.exe';

console.log(`\n[1/2] Starting Vite dev server on port ${PORT}...`);

// 优化Vite启动参数
const vite = spawn('npx', ['vite', '--host', '0.0.0.0', '--port', PORT.toString()], {
  cwd: __dirname,
  stdio: 'inherit',
  shell: true,
  env: {
    ...process.env,
    NODE_OPTIONS: '--max-old-space-size=4096'
  }
});

console.log('[2/2] Starting Cloudflare Tunnel with optimizations...');

// 优化cloudflared启动参数（使用确认支持的参数）
const cloudflaredArgs = [
  'tunnel',
  '--url', `http://localhost:${PORT}`,
  '--no-tls-verify',              // 跳过TLS验证（开发环境）
  '--retries', '10',              // 增加重试次数
  '--compression-quality', '1',   // 启用压缩（1=低压缩）
  '--proxy-keepalive-connections', '100',  // 保持长连接数
  '--proxy-keepalive-timeout', '90s',      // keepalive超时
  '--proxy-tcp-keepalive', '30s',          // TCP keepalive
  '--edge-ip-version', '4',               // 强制使用IPv4（在中国更稳定）
  '--loglevel', 'info'
];

const cloudflared = spawn(CLOUDFLARED, cloudflaredArgs, {
  stdio: 'pipe',
  shell: false,
});

let urlFound = false;
let tunnelUrl = '';

function checkOutput(data) {
  if (urlFound) return;
  const output = data.toString();
  const match = output.match(/https:\/\/[a-z0-9-]+\.trycloudflare\.com/);
  if (match) {
    urlFound = true;
    tunnelUrl = match[0];
    console.log('\n========================================');
    console.log('  Tunnel ready (Optimized)!');
    console.log(`  Mobile URL: ${tunnelUrl}`);
    console.log('  Optimizations: Retries=10, Metrics enabled');
    console.log('========================================\n');
  }
  
  // 输出更多调试信息
  if (output.includes('error') || output.includes('Error')) {
    console.log('[Tunnel Error]', output);
  }
}

cloudflared.stdout.on('data', checkOutput);
cloudflared.stderr.on('data', checkOutput);

vite.on('error', (err) => {
  console.error('Vite failed:', err.message);
  process.exit(1);
});

process.on('SIGINT', () => {
  console.log('\nShutting down...');
  cloudflared.kill();
  vite.kill();
  process.exit(0);
});

// 优雅关闭处理
process.on('SIGTERM', () => {
  cloudflared.kill();
  vite.kill();
  process.exit(0);
});
