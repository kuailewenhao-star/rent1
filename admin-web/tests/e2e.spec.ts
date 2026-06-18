/**
 * E2E tests for 房东租赁管理系统
 *
 * Root cause found and fixed: asyncRoutes was not registered in router.
 * Fixed in src/router/index.ts: routes: [...constantRoutes, ...asyncRoutes]
 *
 * Also fixed: .env.development VITE_API_BASE_URL changed from
 * http://localhost:8080/api to /api (use Vite proxy to avoid CORS).
 */
import { test, expect, chromium } from '@playwright/test';

const BASE_URL = 'http://localhost:3000';
const USERNAME = 'admin';
const PASSWORD = 'admin123';

// ====== Shared auth state ======
let authState: any = null;

test.beforeAll(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();

  // Login once
  await page.goto(`${BASE_URL}/login`);
  await page.fill('input[placeholder="请输入用户名"]', USERNAME);
  await page.fill('input[placeholder="请输入密码"]', PASSWORD);

  const resp = page.waitForResponse(
    r => r.url().includes('/admin/auth/login') && r.status() === 200,
    { timeout: 10000 }
  );
  await page.click('.login-button');
  await resp;

  // Wait for Vue to fully render
  await page.waitForSelector('.layout-container', { timeout: 15000 });
  await page.waitForTimeout(2000);

  authState = await page.context().storageState();
  await page.close();
  await browser.close();
});

// ====== Login Form Tests (fresh browser) ======

test.describe('登录功能', () => {
  test('登录页应显示标题和表单', async () => {
    const browser = await chromium.launch();
    const p = await browser.newPage();
    await p.goto(`${BASE_URL}/login`);
    await expect(p.locator('h1.title')).toContainText('房东租赁管理系统');
    await expect(p.locator('.subtitle')).toContainText('平台超级管理员登录');
    await expect(p.locator('input[placeholder="请输入用户名"]')).toBeVisible();
    await expect(p.locator('input[placeholder="请输入密码"]')).toBeVisible();
    await expect(p.locator('.login-button')).toBeVisible();
    await p.close();
    await browser.close();
  });

  test('空表单提交应显示校验错误', async () => {
    const browser = await chromium.launch();
    const p = await browser.newPage();
    await p.goto(`${BASE_URL}/login`);
    await p.locator('.login-button').click();
    await expect(p.locator('.el-form-item__error').first()).toHaveText('请输入用户名');
    await expect(p.locator('.el-form-item__error').nth(1)).toHaveText('请输入密码');
    await p.close();
    await browser.close();
  });

  test('使用正确凭据可以登录成功', async () => {
    const browser = await chromium.launch();
    const p = await browser.newPage();
    await p.goto(`${BASE_URL}/login`);
    await p.fill('input[placeholder="请输入用户名"]', USERNAME);
    await p.fill('input[placeholder="请输入密码"]', PASSWORD);

    const resp = p.waitForResponse(
      r => r.url().includes('/admin/auth/login') && r.status() === 200,
      { timeout: 10000 }
    );
    await p.click('.login-button');
    await resp;

    await p.waitForSelector('.layout-container', { timeout: 15000 });
    await expect(p.locator('.page-title')).toContainText('首页看板');
    await p.close();
    await browser.close();
  });
});

// ====== Authenticated Tests ======

async function newAuthPage() {
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ storageState: authState });
  const p = await ctx.newPage();
  return { page: p, close: async () => { await p.close(); await browser.close(); } };
}

test.describe('首页看板', () => {
  test('dashboard 页面应正常渲染', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await expect(page.locator('.page-title')).toContainText('首页看板');
    await close();
  });

  test('统计卡片应显示数据', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await expect(page.locator('.stat-card')).toHaveCount(8);
    await close();
  });

  test('侧边栏应显示所有菜单项', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    const items = [
      '首页看板', '会员管理', '房源管理', '房间管理',
      '合约管理', '收入账单', '支出账单', '角色权限', '日志溯源'
    ];
    for (const item of items) {
      await expect(page.locator('.el-menu').getByText(item, { exact: true })).toBeVisible();
    }
    await close();
  });

  test('用户头像应显示用户名', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await expect(page.locator('.username')).toContainText('管理员');
    await close();
  });
});

test.describe('功能模块导航', () => {
  test('会员列表页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '会员管理' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '会员列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('会员列表');
    await close();
  });

  test('房源列表页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '房源管理' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '房源列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('房源列表');
    await close();
  });

  test('房间列表页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '房间管理' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '房间列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('房间列表');
    await close();
  });

  test('合约列表页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '合约管理' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '合约列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('合约列表');
    await close();
  });

  test('收入账单页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '收入账单' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '收入账单列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('收入账单');
    await close();
  });

  test('支出账单页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '支出账单' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '支出账单列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('支出账单');
    await close();
  });

  test('角色列表页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '角色权限' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '角色列表' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('角色');
    await close();
  });

  test('操作日志页应能访问', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.locator('.el-sub-menu__title', { hasText: '日志溯源' }).click();
    await page.waitForTimeout(500);
    await page.locator('.el-menu-item', { hasText: '操作日志' }).click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.page-title')).toContainText('日志');
    await close();
  });
});

test.describe('退出登录', () => {
  test('退出登录后应跳转回登录页', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await page.getByRole('button', { name: /管理员/ }).click();
    await page.waitForTimeout(500);
    await page.getByRole('menuitem', { name: '退出登录' }).click();
    await page.waitForURL(/.*\/login/, { timeout: 10000 });
    await close();
  });
});

test.describe('Token 持久化', () => {
  test('登录后刷新页面应保持登录状态', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });

    const token = await page.evaluate(() => localStorage.getItem('admin_token'));
    expect(token).toBeTruthy();

    await page.reload();
    await page.waitForSelector('.layout-container', { timeout: 10000 });
    await expect(page.locator('.page-title')).toContainText('首页看板');
    await close();
  });
});

test.describe('侧边栏交互', () => {
  test('侧边栏可以折叠和展开', async () => {
    const { page, close } = await newAuthPage();
    await page.goto(BASE_URL);
    await page.waitForSelector('.layout-container', { timeout: 10000 });

    await expect(page.locator('.logo-text')).toBeVisible();
    await page.locator('.header-left .el-button').click();
    await page.waitForTimeout(500);
    await expect(page.locator('.logo-text')).not.toBeVisible();

    await page.locator('.header-left .el-button').click();
    await page.waitForTimeout(500);
    await expect(page.locator('.logo-text')).toBeVisible();
    await close();
  });
});
