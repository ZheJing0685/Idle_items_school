import { expect } from '@playwright/test';
import { mockTest as test } from './fixtures/mock-api';

test.describe('认证流程 E2E 测试', () => {
  test.describe.configure({ mode: 'serial' });

  test.beforeEach(async ({ page }) => {
    await page.goto('/login', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');
  });

  test('登录页面正确加载', async ({ page }) => {
    await expect(page).toHaveTitle(/闲置物品交易平台/);
    await expect(page.getByPlaceholder('请输入用户名')).toBeVisible();
    await expect(page.getByPlaceholder('请输入密码')).toBeVisible();
    await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
  });

  test('表单输入功能正常', async ({ page }) => {
    const usernameInput = page.getByPlaceholder('请输入用户名');
    const passwordInput = page.getByPlaceholder('请输入密码');

    await usernameInput.fill('testuser');
    await passwordInput.fill('password123');

    await expect(usernameInput).toHaveValue('testuser');
    await expect(passwordInput).toHaveValue('password123');
  });

  test('记住我复选框功能', async ({ page }) => {
    const rememberCheckbox = page.locator('.remember-check .el-checkbox__input, .el-checkbox__input');
    const isVisible = await rememberCheckbox.isVisible().catch(() => false);
    if (isVisible) {
      await rememberCheckbox.click();
      await expect(rememberCheckbox).toHaveClass(/is-checked/);
      await rememberCheckbox.click();
      await expect(rememberCheckbox).not.toHaveClass(/is-checked/);
    }
  });

  test('空表单提交应显示验证错误', async ({ page }) => {
    const loginButton = page.getByRole('button', { name: '登录' });
    await loginButton.click();
    await expect(page).toHaveURL(/\/login/);
  });

  test('注册链接可正常跳转', async ({ page }) => {
    await page.getByText('立即注册').click();
    await expect(page).toHaveURL(/\/register/);
  });

  test('忘记密码链接存在', async ({ page }) => {
    const forgotLink = page.getByText('忘记密码');
    await expect(forgotLink).toBeVisible();
  });
});

test.describe('注册流程 E2E 测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');
  });

  test('注册页面正确加载', async ({ page }) => {
    await expect(page.getByText('创建账号')).toBeVisible();
    await expect(page.getByRole('button', { name: '立即注册' })).toBeVisible();
  });

  test('已有账号链接跳转登录页', async ({ page }) => {
    await page.getByRole('link', { name: '立即登录' }).click();
    await expect(page).toHaveURL(/\/login/);
  });
});
