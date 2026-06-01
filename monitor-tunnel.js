#!/usr/bin/env node

/**
 * Cloudflare Tunnel 性能监控脚本
 * 用法: node monitor-tunnel.js <tunnel-url>
 */

import { exec } from 'child_process';
import { promisify } from 'util';
import os from 'os';

const execAsync = promisify(exec);

class TunnelMonitor {
  constructor(url) {
    this.url = url;
    this.results = [];
    this.startTime = Date.now();
  }

  async ping() {
    try {
      const { stdout } = await execAsync(`ping -n 4 ${this.url.replace('https://', '')}`);
      const lines = stdout.split('\n');
      let min = Infinity, max = 0, avg = 0;
      
      for (const line of lines) {
        if (line.includes('时间=') || line.includes('time=')) {
          const timeMatch = line.match(/时间[=:](\d+)ms|time[=:](\d+)ms/);
          if (timeMatch) {
            const time = parseInt(timeMatch[1] || timeMatch[2]);
            if (time < min) min = time;
            if (time > max) max = time;
            avg += time;
          }
        }
      }
      
      const pingCount = (stdout.match(/时间=|time=/g) || []).length;
      if (pingCount > 0) {
        avg = Math.round(avg / pingCount);
      }
      
      return { min, max, avg, count: pingCount };
    } catch (error) {
      return { error: error.message };
    }
  }

  async curl() {
    try {
      const start = Date.now();
      const { stdout } = await execAsync(`curl -o /dev/null -s -w "HTTP状态: %{http_code}\\n下载速度: %{speed_download} bytes/sec\\n总时间: %{time_total}s\\n连接时间: %{time_connect}s\\nTLS时间: %{time_appconnect}s\\n" ${this.url}`);
      const time = Date.now() - start;
      
      const stats = {};
      const lines = stdout.split('\n');
      for (const line of lines) {
        if (line.includes('HTTP状态:')) {
          stats.httpCode = line.split(':')[1]?.trim();
        } else if (line.includes('下载速度:')) {
          stats.speed = line.split(':')[1]?.trim();
        } else if (line.includes('总时间:')) {
          stats.totalTime = line.split(':')[1]?.trim();
        } else if (line.includes('连接时间:')) {
          stats.connectTime = line.split(':')[1]?.trim();
        } else if (line.includes('TLS时间:')) {
          stats.tlsTime = line.split(':')[1]?.trim();
        }
      }
      
      return { ...stats, curlTime: time };
    } catch (error) {
      return { error: error.message };
    }
  }

  async dns() {
    try {
      const start = Date.now();
      const { stdout } = await execAsync(`nslookup ${this.url.replace('https://', '').split('/')[0]}`);
      const time = Date.now() - start;
      
      const servers = stdout.match(/\d+\.\d+\.\d+\.\d+/g) || [];
      return { dnsTime: time, servers: servers.slice(0, 3) };
    } catch (error) {
      return { error: error.message };
    }
  }

  async trace() {
    try {
      const { stdout } = await execAsync(`tracert -d -h 10 ${this.url.replace('https://', '').split('/')[0]}`);
      const hops = stdout.split('\n').filter(line => line.includes('ms') || line.includes('请求超时'));
      return { hops: hops.length, trace: stdout };
    } catch (error) {
      return { error: error.message };
    }
  }

  async fullTest() {
    console.log(`\n${'='.repeat(60)}`);
    console.log(`  Cloudflare Tunnel 性能监控`);
    console.log(`  目标: ${this.url}`);
    console.log(`  开始时间: ${new Date().toLocaleString()}`);
    console.log(`${'='.repeat(60)}\n`);

    // DNS测试
    console.log('[1/4] DNS解析测试...');
    const dnsResult = await this.dns();
    console.log(`  DNS解析时间: ${dnsResult.dnsTime}ms`);
    console.log(`  DNS服务器: ${dnsResult.servers.join(', ')}`);

    // Ping测试
    console.log('\n[2/4] Ping延迟测试...');
    const pingResult = await this.ping();
    if (pingResult.error) {
      console.log(`  Ping失败: ${pingResult.error}`);
    } else {
      console.log(`  最小延迟: ${pingResult.min}ms`);
      console.log(`  最大延迟: ${pingResult.max}ms`);
      console.log(`  平均延迟: ${pingResult.avg}ms`);
      console.log(`  测试包数: ${pingResult.count}`);
    }

    // HTTP测试
    console.log('\n[3/4] HTTP连接测试...');
    const curlResult = await this.curl();
    if (curlResult.error) {
      console.log(`  HTTP测试失败: ${curlResult.error}`);
    } else {
      console.log(`  HTTP状态: ${curlResult.httpCode}`);
      console.log(`  下载速度: ${curlResult.speed}`);
      console.log(`  总时间: ${curlResult.totalTime}`);
      console.log(`  连接时间: ${curlResult.connectTime}`);
      console.log(`  TLS时间: ${curlResult.tlsTime}`);
      console.log(`  Curl总耗时: ${curlResult.curlTime}ms`);
    }

    // 路由追踪
    console.log('\n[4/4] 路由追踪...');
    const traceResult = await this.trace();
    console.log(`  跳数: ${traceResult.hops}`);

    // 生成报告
    this.generateReport({
      dns: dnsResult,
      ping: pingResult,
      curl: curlResult,
      trace: traceResult
    });
  }

  generateReport(results) {
    console.log(`\n${'='.repeat(60)}`);
    console.log('  性能评估报告');
    console.log(`${'='.repeat(60)}\n`);

    // 评分
    let score = 100;
    let issues = [];
    let suggestions = [];

    // DNS评估
    if (results.dns.dnsTime > 100) {
      score -= 10;
      issues.push('DNS解析较慢');
      suggestions.push('考虑使用更快的DNS服务器，如1.1.1.1');
    }

    // Ping评估
    if (!results.ping.error) {
      if (results.ping.avg > 200) {
        score -= 20;
        issues.push('网络延迟较高');
        suggestions.push('检查网络连接质量');
      } else if (results.ping.avg > 100) {
        score -= 10;
        issues.push('网络延迟中等');
        suggestions.push('可考虑使用更近的边缘节点');
      }
    }

    // HTTP评估
    if (!results.curl.error) {
      const totalTime = parseFloat(results.curl.totalTime);
      if (totalTime > 3) {
        score -= 30;
        issues.push('HTTP连接时间过长');
        suggestions.push('检查本地服务性能');
      } else if (totalTime > 1) {
        score -= 15;
        issues.push('HTTP连接时间较长');
        suggestions.push('优化本地服务响应时间');
      }

      const tlsTime = parseFloat(results.curl.tlsTime);
      if (tlsTime > 1) {
        score -= 15;
        issues.push('TLS握手时间较长');
        suggestions.push('考虑使用QUIC协议');
      }
    }

    // 生成评分
    console.log(`  综合评分: ${score}/100`);
    console.log(`  等级: ${this.getGrade(score)}`);

    if (issues.length > 0) {
      console.log('\n  发现的问题:');
      issues.forEach((issue, i) => {
        console.log(`    ${i + 1}. ${issue}`);
      });
    }

    if (suggestions.length > 0) {
      console.log('\n  优化建议:');
      suggestions.forEach((suggestion, i) => {
        console.log(`    ${i + 1}. ${suggestion}`);
      });
    }

    console.log(`\n  测试完成时间: ${new Date().toLocaleString()}`);
    console.log(`${'='.repeat(60)}\n`);
  }

  getGrade(score) {
    if (score >= 90) return '优秀 (A)';
    if (score >= 80) return '良好 (B)';
    if (score >= 70) return '中等 (C)';
    if (score >= 60) return '及格 (D)';
    return '不及格 (F)';
  }
}

// 主函数
async function main() {
  const url = process.argv[2];
  
  if (!url) {
    console.log('用法: node monitor-tunnel.js <tunnel-url>');
    console.log('示例: node monitor-tunnel.js https://abc123.trycloudflare.com');
    process.exit(1);
  }

  const monitor = new TunnelMonitor(url);
  await monitor.fullTest();
}

main().catch(console.error);
