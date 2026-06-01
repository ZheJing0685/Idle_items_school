#!/usr/bin/env node

/**
 * Cloudflare Tunnel 问题诊断工具
 */

import { exec } from 'child_process';
import { promisify } from 'util';
import http from 'http';
import https from 'https';

const execAsync = promisify(exec);

class TunnelDiagnostics {
  constructor() {
    this.results = [];
  }

  async check(name, fn) {
    process.stdout.write(`  ${name}... `);
    try {
      const result = await fn();
      console.log(`✅ ${result}`);
      this.results.push({ name, status: 'pass', detail: result });
    } catch (error) {
      console.log(`❌ ${error.message}`);
      this.results.push({ name, status: 'fail', detail: error.message });
    }
  }

  async testLocalServer() {
    return new Promise((resolve, reject) => {
      const start = Date.now();
      http.get('http://localhost:5173', (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          const time = Date.now() - start;
          resolve(`本地服务器响应 ${time}ms, 状态码: ${res.statusCode}`);
        });
      }).on('error', (err) => {
        reject(new Error(`本地服务器无法访问: ${err.message}`));
      });
    });
  }

  async testBackendServer() {
    return new Promise((resolve, reject) => {
      const start = Date.now();
      http.get('http://localhost:7000/api/actuator/health', (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          const time = Date.now() - start;
          resolve(`后端服务器响应 ${time}ms, 状态码: ${res.statusCode}`);
        });
      }).on('error', (err) => {
        reject(new Error(`后端服务器无法访问: ${err.message}`));
      });
    });
  }

  async testTunnelUrl() {
    return new Promise((resolve, reject) => {
      const start = Date.now();
      const options = {
        hostname: 'localhost',
        port: 5173,
        path: '/',
        method: 'GET',
        timeout: 10000
      };

      const req = http.request(options, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          const time = Date.now() - start;
          resolve(`Vite开发服务器响应 ${time}ms`);
        });
      });

      req.on('timeout', () => {
        req.destroy();
        reject(new Error('Vite服务器响应超时'));
      });

      req.on('error', (err) => {
        reject(new Error(`Vite服务器错误: ${err.message}`));
      });

      req.end();
    });
  }

  async testCloudflared() {
    try {
      const { stdout } = await execAsync('tasklist /FI "IMAGENAME eq cloudflared.exe" /FO CSV');
      const lines = stdout.split('\n').filter(l => l.includes('cloudflared'));
      if (lines.length > 0) {
        return `cloudflared 正在运行 (${lines.length - 1} 个进程)`;
      }
      throw new Error('cloudflared 未运行');
    } catch (error) {
      throw new Error('cloudflared 未运行或无法检测');
    }
  }

  async testPort() {
    return new Promise((resolve, reject) => {
      exec('netstat -ano | findstr :5173 | findstr LISTENING', (err, stdout) => {
        if (stdout.includes('LISTENING')) {
          resolve('端口 5173 正在监听');
        } else {
          reject(new Error('端口 5173 未监听'));
        }
      });
    });
  }

  async testDns() {
    const start = Date.now();
    try {
      await execAsync('nslookup trycloudflare.com');
      const time = Date.now() - start;
      return `DNS解析正常 (${time}ms)`;
    } catch (error) {
      throw new Error('DNS解析失败');
    }
  }

  async testNetworkLatency() {
    return new Promise((resolve, reject) => {
      exec('ping -n 4 1.1.1.1', (err, stdout) => {
        const times = stdout.match(/时间[=:](\d+)ms/g) || [];
        if (times.length > 0) {
          const avg = times.reduce((sum, t) => sum + parseInt(t.match(/\d+/)[0]), 0) / times.length;
          resolve(`网络延迟 ${Math.round(avg)}ms`);
        } else {
          reject(new Error('网络延迟测试失败'));
        }
      });
    });
  }

  async run() {
    console.log('\n' + '='.repeat(60));
    console.log('  Cloudflare Tunnel 问题诊断');
    console.log('='.repeat(60) + '\n');

    console.log('【基础检查】');
    await this.check('端口监听', () => this.testPort());
    await this.check('cloudflared进程', () => this.testCloudflared());
    await this.check('DNS解析', () => this.testDns());
    await this.check('网络延迟', () => this.testNetworkLatency());

    console.log('\n【服务检查】');
    await this.check('Vite开发服务器', () => this.testLocalServer());
    await this.check('后端服务器', () => this.testBackendServer());

    console.log('\n【问题分析】');
    this.analyze();
  }

  analyze() {
    const failures = this.results.filter(r => r.status === 'fail');
    
    if (failures.length === 0) {
      console.log('\n✅ 所有检查通过！问题可能在以下方面：');
      console.log('  1. Cloudflare边缘节点距离较远');
      console.log('  2. 浏览器缓存问题');
      console.log('  3. 前端资源未优化');
      console.log('  4. 网络环境波动');
      console.log('\n建议：');
      console.log('  - 清除浏览器缓存后重试');
      console.log('  - 使用无痕模式测试');
      console.log('  - 尝试不同时段测试');
    } else {
      console.log('\n❌ 发现以下问题：');
      failures.forEach((f, i) => {
        console.log(`  ${i + 1}. ${f.name}: ${f.detail}`);
      });
      console.log('\n请先解决以上问题后再测试速度。');
    }

    console.log('\n' + '='.repeat(60));
  }
}

const diag = new TunnelDiagnostics();
diag.run().catch(console.error);
