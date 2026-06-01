#!/usr/bin/env node

/**
 * 快速性能测试脚本
 * 用法: node quick-test.js <tunnel-url>
 */

import { exec } from 'child_process';
import { promisify } from 'util';

const execAsync = promisify(exec);

async function quickTest(url) {
  console.log(`\n${'='.repeat(50)}`);
  console.log(`  快速性能测试`);
  console.log(`  目标: ${url}`);
  console.log(`${'='.repeat(50)}\n`);

  // 测试1: DNS解析
  console.log('[1/3] DNS解析测试...');
  try {
    const start = Date.now();
    await execAsync(`nslookup ${url.replace('https://', '').split('/')[0]}`);
    const dnsTime = Date.now() - start;
    console.log(`  DNS解析: ${dnsTime}ms ${dnsTime < 100 ? '✅' : '⚠️'}`);
  } catch (error) {
    console.log(`  DNS解析: 失败 ❌`);
  }

  // 测试2: Ping延迟
  console.log('\n[2/3] Ping延迟测试...');
  try {
    const { stdout } = await execAsync(`ping -n 4 ${url.replace('https://', '').split('/')[0]}`);
    const times = stdout.match(/时间[=:](\d+)ms/g) || [];
    if (times.length > 0) {
      const avgTime = times.reduce((sum, t) => sum + parseInt(t.match(/\d+/)[0]), 0) / times.length;
      console.log(`  平均延迟: ${Math.round(avgTime)}ms ${avgTime < 100 ? '✅' : '⚠️'}`);
    } else {
      console.log(`  Ping测试: 无响应 ❌`);
    }
  } catch (error) {
    console.log(`  Ping测试: 失败 ❌`);
  }

  // 测试3: HTTP连接
  console.log('\n[3/3] HTTP连接测试...');
  try {
    const start = Date.now();
    await execAsync(`curl -o /dev/null -s -w "HTTP状态: %{http_code}\\n" ${url}`);
    const httpTime = Date.now() - start;
    console.log(`  HTTP连接: ${httpTime}ms ${httpTime < 1000 ? '✅' : '⚠️'}`);
  } catch (error) {
    console.log(`  HTTP连接: 失败 ❌`);
  }

  console.log(`\n${'='.repeat(50)}`);
  console.log('  测试完成');
  console.log(`${'='.repeat(50)}\n`);
}

// 主函数
const url = process.argv[2];
if (!url) {
  console.log('用法: node quick-test.js <tunnel-url>');
  console.log('示例: node quick-test.js https://abc123.trycloudflare.com');
  process.exit(1);
}

quickTest(url).catch(console.error);
