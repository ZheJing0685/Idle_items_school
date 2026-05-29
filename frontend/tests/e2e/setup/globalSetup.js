import { chromium } from '@playwright/test';

async function globalSetup(config) {
  // 1. 安装 Playwright 浏览器（如需要）
  // await install({
  //   browser: ['chromium', 'firefox', 'webkit']
  // })

  // 2. 启动开发服务器
  // 已在 playwright.config.js 的 webServer 中配置

  // 3. 预热：确保服务器可用
  const maxRetries = 30;
  const retryInterval = 1000;
  let serverReady = false;

  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch('http://localhost:5173');
      if (response.ok) {
        serverReady = true;
        console.log('✅ Dev server is ready');
        break;
      }
    } catch {
      console.log(`Waiting for dev server... (${i + 1}/${maxRetries})`);
      await new Promise(resolve => setTimeout(resolve, retryInterval));
    }
  }

  if (!serverReady) {
    throw new Error('Dev server did not start in time');
  }

  // 4. 清理测试数据
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  await page.goto('http://localhost:5173');

  // 清理所有存储
  await page.evaluate(() => {
    localStorage.clear();
    sessionStorage.clear();
    if (document.cookie) {
      document.cookie.split(';').forEach(cookie => {
        const eqPos = cookie.indexOf('=');
        const name = eqPos > -1 ? cookie.substring(0, eqPos).trim() : cookie.trim();
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';
      });
    }
  });

  await browser.close();
  console.log('✅ Global setup completed');
}

export default globalSetup;
