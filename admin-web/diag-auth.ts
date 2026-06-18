import { chromium } from '@playwright/test';

(async () => {
  const browser = await chromium.launch({ headless: true });

  // Login
  const page = await browser.newPage();
  await page.goto('http://localhost:3000/login');
  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('.login-button');
  await page.waitForURL(u => !u.toString().includes('/login'), { timeout: 15000 });
  await page.waitForTimeout(3000);
  const state = await page.context().storageState();
  console.log('Token saved');
  await page.close();

  // New context
  const ctx = await browser.newContext({ storageState: state });
  const page2 = await ctx.newPage();

  await page2.goto('http://localhost:3000/dashboard');
  await page2.waitForTimeout(5000);

  // Check HTML
  const html = await page2.content();
  console.log('HTML length:', html.length);
  console.log('HTML:', html.substring(0, 500));

  // Check innerHTML of #app
  const appContent = await page2.$eval('#app', el => el.innerHTML);
  console.log('\n#app innerHTML:', appContent);

  // Check console errors
  const logs: string[] = [];
  page2.on('console', msg => logs.push(`${msg.type()}: ${msg.text()}`));

  // Reload and capture console
  await page2.reload();
  await page2.waitForTimeout(5000);

  console.log('\n=== Console Logs ===');
  for (const log of logs) console.log(log);

  await browser.close();
})();
