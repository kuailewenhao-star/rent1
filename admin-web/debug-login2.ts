/**
 * Debug v2: check form validation and network more carefully
 */
import { chromium } from '@playwright/test';

(async () => {
  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();

  // Collect ALL network requests
  const allRequests: string[] = [];
  const allResponses: string[] = [];
  page.on('request', req => {
    if (!req.url().includes('chrome-extension')) {
      allRequests.push(`${req.method()} ${req.url()}`);
    }
  });
  page.on('response', async resp => {
    if (!resp.url().includes('chrome-extension')) {
      try {
        const body = await resp.text();
        allResponses.push(`${resp.status()} ${resp.url()} => ${body.substring(0, 500)}`);
      } catch {}
    }
  });

  await page.goto('http://localhost:3000/login');
  console.log('=== Page loaded ===');
  console.log('URL:', page.url());

  // Screenshot
  await page.screenshot({ path: 'debug2-login.png' });

  // Fill credentials
  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.screenshot({ path: 'debug2-filled.png' });

  // Check if form exists
  const formExists = await page.$('.login-form');
  console.log('Form exists:', !!formExists);

  const buttonExists = await page.$('.login-button');
  console.log('Button exists:', !!buttonExists);

  // Check for validation errors before clicking
  const preErrors = await page.$$eval('.el-form-item__error', els => els.map(e => e.textContent));
  console.log('Pre-click errors:', preErrors);

  // Click the button
  await page.click('.login-button');
  console.log('=== Clicked login ===');
  await page.waitForTimeout(3000);

  // Check for errors after click
  const postErrors = await page.$$eval('.el-form-item__error', els => els.map(e => e.textContent));
  console.log('Post-click errors:', postErrors);

  await page.screenshot({ path: 'debug2-after.png' });

  // Check for ElMessage
  const messages = await page.$$('.el-message');
  console.log('ElMessages:', messages.length);
  for (const m of messages) {
    console.log('  msg text:', await m.textContent());
  }

  // Check URL
  console.log('URL after:', page.url());

  // All requests/responses
  console.log('\n=== ALL Requests ===');
  for (const r of allRequests) console.log(r);
  console.log('\n=== ALL Responses ===');
  for (const r of allResponses) console.log(r);

  // Check localStorage
  const token = await page.evaluate(() => localStorage.getItem('admin_token'));
  console.log('Token:', token);

  // Try direct API call
  console.log('\n=== Direct API test ===');
  try {
    const resp = await page.evaluate(async () => {
      const r = await fetch('http://localhost:8080/api/admin/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'admin', password: 'admin123' })
      });
      return { status: r.status, body: await r.text() };
    });
    console.log('Direct API:', JSON.stringify(resp));
  } catch (e) {
    console.log('Direct API error:', e);
  }

  await browser.close();
})();
