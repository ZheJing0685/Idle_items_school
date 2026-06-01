#!/usr/bin/env node

/**
 * 页面加载性能分析工具
 */

import http from 'http';
import https from 'https';

class PerformanceAnalyzer {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.results = [];
  }

  async measure(name, url) {
    return new Promise((resolve, reject) => {
      const start = Date.now();
      const client = url.startsWith('https') ? https : http;
      
      client.get(url, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          const time = Date.now() - start;
          const size = Buffer.byteLength(data, 'utf8');
          this.results.push({ name, url, time, size, status: res.statusCode });
          resolve({ name, time, size, status: res.statusCode });
        });
      }).on('error', (err) => {
        const time = Date.now() - start;
        this.results.push({ name, url, time, size: 0, status: 'error', error: err.message });
        reject(err);
      });
    });
  }

  async analyze() {
    console.log('\n' + '='.repeat(60));
    console.log('  页面加载性能分析');
    console.log(`  目标: ${this.baseUrl}`);
    console.log('='.repeat(60) + '\n');

    // 测试各种资源
    const tests = [
      { name: 'HTML首页', path: '/' },
      { name: 'CSS文件', path: '/src/style.css' },
      { name: 'JS入口', path: '/src/main.ts' },
      { name: 'API健康检查', path: '/api/actuator/health' },
    ];

    console.log('【资源加载测试】\n');

    for (const test of tests) {
      try {
        const url = `${this.baseUrl}${test.path}`;
        const result = await this.measure(test.name, url);
        const sizeKB = (result.size / 1024).toFixed(1);
        console.log(`  ${test.name}: ${result.time}ms (${sizeKB}KB) [${result.status}]`);
      } catch (error) {
        console.log(`  ${test.name}: 失败 - ${error.message}`);
      }
    }

    // 生成报告
    this.generateReport();
  }

  generateReport() {
    console.log('\n' + '='.repeat(60));
    console.log('  性能分析报告');
    console.log('='.repeat(60) + '\n');

    const totalTime = this.results.reduce((sum, r) => sum + r.time, 0);
    const totalSize = this.results.reduce((sum, r) => sum + r.size, 0);

    console.log(`  总加载时间: ${totalTime}ms`);
    console.log(`  总资源大小: ${(totalSize / 1024).toFixed(1)}KB`);
    console.log(`  平均响应时间: ${Math.round(totalTime / this.results.length)}ms`);

    console.log('\n【优化建议】\n');

    // 分析瓶颈
    const htmlResult = this.results.find(r => r.name === 'HTML首页');
    if (htmlResult && htmlResult.time > 1000) {
      console.log('  ⚠️ HTML加载较慢 (>1s)');
      console.log('     建议：检查Vite服务器配置');
    }

    const apiResult = this.results.find(r => r.name === 'API健康检查');
    if (apiResult && apiResult.time > 500) {
      console.log('  ⚠️ API响应较慢 (>500ms)');
      console.log('     建议：检查后端服务器性能');
    }

    // 总体评估
    if (totalTime < 2000) {
      console.log('  ✅ 整体性能良好');
      console.log('     如果页面仍然感觉慢，可能是：');
      console.log('     1. Cloudflare边缘节点距离较远');
      console.log('     2. 浏览器渲染优化问题');
      console.log('     3. 第三方脚本加载');
    } else if (totalTime < 5000) {
      console.log('  ⚠️ 性能中等，有优化空间');
    } else {
      console.log('  ❌ 性能较差，需要优化');
    }

    console.log('\n' + '='.repeat(60));
  }
}

// 主函数
const baseUrl = process.argv[2] || 'http://localhost:5173';
const analyzer = new PerformanceAnalyzer(baseUrl);
analyzer.analyze().catch(console.error);
