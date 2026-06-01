#!/usr/bin/env node

/**
 * 隧道速度测试工具
 * 用法: node test-tunnel-speed.js <tunnel-url>
 */

import https from 'https';
import http from 'http';

const url = process.argv[2];

if (!url) {
  console.log('\n用法: node test-tunnel-speed.js <tunnel-url>');
  console.log('示例: node test-tunnel-speed.js https://abc123.trycloudflare.com\n');
  process.exit(1);
}

console.log('\n' + '='.repeat(60));
console.log('  Cloudflare Tunnel 速度测试');
console.log(`  目标: ${url}`);
console.log('='.repeat(60) + '\n');

// 测试资源列表
const tests = [
  { name: '首页加载', path: '/' },
  { name: '静态资源', path: '/favicon.ico' },
];

async function testResource(name, resourceUrl) {
  return new Promise((resolve) => {
    const start = Date.now();
    const client = resourceUrl.startsWith('https') ? https : http;
    
    client.get(resourceUrl, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const time = Date.now() - start;
        const size = Buffer.byteLength(data, 'utf8');
        resolve({ name, time, size, status: res.statusCode, success: true });
      });
    }).on('error', (err) => {
      const time = Date.now() - start;
      resolve({ name, time, size: 0, status: 'error', error: err.message, success: false });
    });
  });
}

async function runTests() {
  console.log('【测试项目】\n');
  
  const results = [];
  
  for (const test of tests) {
    const testUrl = `${url}${test.path}`;
    process.stdout.write(`  ${test.name}... `);
    const result = await testResource(test.name, testUrl);
    results.push(result);
    
    if (result.success) {
      const sizeKB = (result.size / 1024).toFixed(1);
      console.log(`${result.time}ms (${sizeKB}KB) [${result.status}]`);
    } else {
      console.log(`失败: ${result.error}`);
    }
  }
  
  // 生成报告
  console.log('\n' + '='.repeat(60));
  console.log('  测试报告');
  console.log('='.repeat(60) + '\n');
  
  const totalTime = results.reduce((sum, r) => sum + r.time, 0);
  const avgTime = Math.round(totalTime / results.length);
  
  console.log(`  总时间: ${totalTime}ms`);
  console.log(`  平均时间: ${avgTime}ms`);
  
  console.log('\n【速度评级】\n');
  
  if (avgTime < 500) {
    console.log('  ⭐⭐⭐⭐⭐ 优秀 (<500ms)');
    console.log('  速度非常好！');
  } else if (avgTime < 1000) {
    console.log('  ⭐⭐⭐⭐ 良好 (<1s)');
    console.log('  速度不错，可以接受。');
  } else if (avgTime < 2000) {
    console.log('  ⭐⭐⭐ 中等 (<2s)');
    console.log('  速度一般，有优化空间。');
  } else if (avgTime < 5000) {
    console.log('  ⭐⭐ 较慢 (<5s)');
    console.log('  速度较慢，建议优化。');
  } else {
    console.log('  ⭐ 很慢 (>5s)');
    console.log('  速度很慢，需要优化。');
  }
  
  console.log('\n【优化建议】\n');
  
  if (avgTime > 2000) {
    console.log('  1. 尝试使用中国优化版本: node dev-tunnel-china.js');
    console.log('  2. 检查网络连接质量');
    console.log('  3. 尝试不同时段测试');
    console.log('  4. 使用 Chrome/Edge 浏览器');
  } else {
    console.log('  速度正常，无需优化。');
  }
  
  console.log('\n' + '='.repeat(60));
}

runTests().catch(console.error);
