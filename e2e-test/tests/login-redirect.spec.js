import { readFile, stat } from 'node:fs/promises';
import { admin, expect, test as base } from './fixtures.js';

const test = base.extend({
  pageStoreLog: [async ({}, use, testInfo) => {
    // Wicket catches statelessness-check failures, so the browser can succeed
    // while request teardown logs an enclosure error. See README for logging.
    const logPath = process.env.E2E_SERVER_LOG;
    const offset = logPath ? (await stat(logPath)).size : 0;
    await use();
    if (logPath) {
      const log = (await readFile(logPath)).subarray(offset).toString();
      await testInfo.attach('server-log', { body: log, contentType: 'text/plain' });
      const errors = log.split(/\r?\n/).filter(line =>
        line.includes('An error occurred while checking whether a page is stateless')
        || /Could not find child with id: .* in the wicket:enclosure/.test(line));
      expect(errors, 'Page-store errors during the login redirect').toEqual([]);
    }
  }, { auto: true }],
});

test('restores a private issue comment fragment after login', async ({ page, api, baseURL }) => {
  const project = await api.createProject();
  const issue = await api.createIssue(project, {
    // Keep the comment below the initial viewport so restoring the URL alone
    // is insufficient: the browser must also scroll to the linked comment.
    description: Array.from({ length: 40 }, (_, index) => `Context paragraph ${index + 1}.`).join('\n\n'),
  });
  const comment = await api.createIssueComment(issue, 'The comment linked from a notification.');
  const target = new URL(comment.url, baseURL);
  target.searchParams.set('login-redirect-test', 'fragment');
  const storageKey = target.pathname + target.search;

  // Fixture requests authenticate independently; this page starts logged out.
  await page.goto(target.href);
  await expect(page).toHaveURL(/\/~login(?:[;?#]|$)/);
  await expect(page.getByRole('button', { name: 'Sign in', exact: true })).toBeVisible();
  expect(new URL(page.url()).hash).toBe('');
  expect(await page.evaluate(key => sessionStorage.getItem(key), storageKey)).toBe(target.hash);

  // Submit the intercepted login form without navigating away from it.
  await page.getByPlaceholder('Login name or email address', { exact: true }).fill(admin.name);
  await page.locator('form input[type="password"]').fill(admin.password);
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page).toHaveURL(target.href);

  const linkedComment = page.locator(target.hash);
  await expect(linkedComment).toContainText(comment.content);
  await expect(linkedComment).toBeInViewport();
  expect(await page.evaluate(key => sessionStorage.getItem(key), storageKey)).toBeNull();

  // An ordinary visit must not reuse the consumed fragment. This also verifies
  // that the fixture requires scrolling to reach the comment.
  await page.goto('~administration/settings/system');
  target.hash = '';
  await page.goto(target.href);
  await expect(page).toHaveURL(target.href);
  await expect(linkedComment).toBeVisible();
  await expect(linkedComment).not.toBeInViewport();
});

// Administration pages are intercepted during onInitialize(), before their
// wicket:enclosure children exist. Private projects are intercepted earlier,
// in ProjectPage's constructor. Exercise both with real anonymous sessions.
for (const existingSession of [false, true]) {
  for (const destination of ['system settings', 'private project']) {
    test(`returns to ${destination} after login with ${existingSession ? 'an existing' : 'a fresh'} anonymous session`, async ({ page, api, baseURL }, testInfo) => {
      const project = destination === 'private project' ? await api.createProject() : null;
      const path = project ? project.name : '~administration/settings/system';
      const target = `${path}?login-redirect-test=retained`;
      const failures = [];
      const navigation = [];
      page.on('pageerror', error => failures.push(error.message));
      page.on('response', response => {
        if (response.status() >= 500)
          failures.push(`${response.status()} ${response.url()}`);
        if (response.request().isNavigationRequest())
          navigation.push({ status: response.status(), url: response.url() });
      });

      try {
        let originalSessionId;
        if (existingSession) {
          await page.goto('~login');
          await expect(page.getByRole('button', { name: 'Sign in', exact: true })).toBeVisible();
          originalSessionId = (await page.context().cookies()).find(cookie => /JSESSIONID/i.test(cookie.name))?.value;
          expect(originalSessionId).toBeTruthy();
        }

        await page.goto(target);
        await expect(page).toHaveURL(/\/~login(?:[;?#]|$)/);
        const userName = page.getByPlaceholder('Login name or email address', { exact: true });
        const password = page.locator('form input[type="password"]');
        const signIn = page.getByRole('button', { name: 'Sign in', exact: true });
        await expect(signIn).toBeVisible();
        if (existingSession) {
          const rotatedSessionId = (await page.context().cookies()).find(cookie => /JSESSIONID/i.test(cookie.name))?.value;
          expect(rotatedSessionId).toBeTruthy();
          expect(rotatedSessionId).not.toBe(originalSessionId);
        }

        await userName.fill(admin.name);
        if (existingSession) {
          // A rejected submission must also retain the intercepted destination.
          await password.fill(`${admin.password}-incorrect`);
          await signIn.click();
          await expect(page.getByText('Invalid account or incorrect credentials', { exact: true })).toBeVisible();
          await expect(page).toHaveURL(/\/~login(?:[;?#]|$)/);
        }
        // Submit this form directly: navigating to ~login again would lose the
        // opportunity to test the original intercepted request.
        await password.fill(admin.password);
        await signIn.click();
        await page.waitForURL(url => !url.pathname.includes('~login'));
        const returnedUrl = new URL(page.url());
        expect(returnedUrl.pathname).toBe(new URL(target, baseURL).pathname);
        expect(returnedUrl.searchParams.get('login-redirect-test')).toBe('retained');

        const content = project
          ? page.locator('.project-overview')
          : page.getByRole('button', { name: 'Save Settings', exact: true });
        await expect(content).toBeVisible();
        // Make another request to verify the authenticated session survives.
        await page.reload();
        await expect(content).toBeVisible();
        expect(failures).toEqual([]);
      } finally {
        await testInfo.attach('navigation', {
          body: JSON.stringify({ navigation, failures }, null, 2),
          contentType: 'application/json',
        });
      }
    });
  }
}

test('replacing the login session closes old WebSockets and rejects the old cookie', async ({ page, browser, baseURL }) => {
  await page.goto('~login');
  await page.waitForFunction(() => window.Wicket?.WebSocket?.INSTANCE?.ws?.readyState === WebSocket.OPEN);
  await page.evaluate(() => {
    window.sessionSocketClose = null;
    Wicket.WebSocket.INSTANCE.ws.addEventListener('close', event => {
      window.sessionSocketClose = { code: event.code, reason: event.reason };
    });
  });
  const originalCookie = (await page.context().cookies()).find(cookie => /JSESSIONID/i.test(cookie.name));
  expect(originalCookie).toBeTruthy();

  // Keep the first tab open: a close here must come from session destruction,
  // not from navigating away from the page that owns the connection.
  const loginTab = await page.context().newPage();
  try {
    await loginTab.goto('~administration/settings/system?login-redirect-test=retained');
    await expect(loginTab).toHaveURL(/\/~login(?:[;?#]|$)/);
    await expect.poll(() => page.evaluate(() => window.sessionSocketClose), { timeout: 10000 })
      .toEqual({ code: 1000, reason: 'Session destroyed' });
    const replacementCookie = (await page.context().cookies()).find(cookie => /JSESSIONID/i.test(cookie.name));
    expect(replacementCookie?.value).toBeTruthy();
    expect(replacementCookie.value).not.toBe(originalCookie.value);

    await loginTab.getByPlaceholder('Login name or email address', { exact: true }).fill(admin.name);
    await loginTab.locator('form input[type="password"]').fill(admin.password);
    await loginTab.getByRole('button', { name: 'Sign in', exact: true }).click();
    await expect(loginTab.getByRole('button', { name: 'Save Settings', exact: true })).toBeVisible();
    expect(new URL(loginTab.url()).searchParams.get('login-redirect-test')).toBe('retained');

    const staleContext = await browser.newContext({ baseURL });
    try {
      await staleContext.addCookies([originalCookie]);
      const stalePage = await staleContext.newPage();
      await stalePage.goto('~administration/settings/system');
      await expect(stalePage).toHaveURL(/\/~login(?:[;?#]|$)/);
      await expect(stalePage.getByRole('button', { name: 'Sign in', exact: true })).toBeVisible();
      await expect(stalePage.getByRole('button', { name: 'Save Settings', exact: true })).toHaveCount(0);
    } finally {
      await staleContext.close();
    }
  } finally {
    await loginTab.close();
  }
});
