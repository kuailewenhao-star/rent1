/**
 * Debug script: figure out why login fails
 */
import { chromium } from '@playwright/test';

(async () => {
  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();

  await page.goto('http://localhost:3000/login');
  console.log('Page title:', await page.title());
  console.log('Current URL:', page.url());

  // Take a screenshot to see the page
  await page.screenshot({ path: 'debug-login-before.png' });

  // Fill in credentials
  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.screenshot({ path: 'debug-login-filled.png' });

  // Intercept network requests
  const responses: string[] = [];
  page.on('response', async (resp) => {
    if (resp.url().includes('/api/')) {
      const body = await resp.text().catch(() => '');
      responses.push(`${resp.status()} ${resp.url()} => ${body.substring(0, 300)}`);
    }
  });

  // Click login
  await page.click('.login-button');
  console.log('Clicked login button...');

  // Wait and check
  await page.waitForTimeout(5000);

  console.log('URL after login:', page.url());
  console.log('Page title after:', await page.title());
  await page.screenshot({ path: 'debug-login-after.png' });

  // Check for error messages
  const errors = await page.$$('.el-message--error');
  if (errors.length > 0) {
    console.log('Error messages found!');
    for (const el of errors) {
      const text = await el.innerText();
      console.log('  Error:', text);
    }
  }

  console.log('\n=== Network responses ===');
  for (const r of responses) {
    console.log(r);
  }

  // Check localStorage
  const token = await page.evaluate(() => localStorage.getItem('admin_token'));
  console.log('localStorage admin_token:', token);

  await browser.close();
})();
