import { chromium } from '@playwright/test';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  // Collect ALL requests/responses
  const requests: string[] = [];
  const responses: string[] = [];
  page.on('request', req => {
    if (!req.url().includes('chrome-extension')) {
      requests.push(`${req.method()} ${req.url()}`);
    }
  });
  page.on('response', async resp => {
    if (!resp.url().includes('chrome-extension')) {
      try {
        const body = await resp.text();
        responses.push(`${resp.status()} ${resp.url()} => ${body.substring(0, 400)}`);
      } catch {}
    }
  });

  await page.goto('http://localhost:3000/login');
  console.log('URL:', page.url());

  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('.login-button');

  await page.waitForTimeout(8000);

  console.log('\n=== URL after login:', page.url());
  console.log('=== Page title:', await page.title());

  // Check for ElMessage errors
  const errorMsgs = await page.$$('.el-message');
  for (const m of errorMsgs) {
    console.log('Message:', await m.textContent());
  }

  // Screenshots
  await page.screenshot({ path: 'debug3-login-failed.png' });

  console.log('\n=== Requests ===');
  for (const r of requests) console.log(r);

  console.log('\n=== Responses ===');
  for (const r of responses) console.log(r);

  // localStorage
  const token = await page.evaluate(() => localStorage.getItem('admin_token'));
  console.log('\nToken:', token);

  // Check if dashboard content exists
  const dashboardTitle = await page.locator('.title').textContent().catch(() => 'NOT FOUND');
  console.log('Dashboard title:', dashboardTitle);

  await browser.close();
})();
