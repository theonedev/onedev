import { expect, test as base } from '@playwright/test';
import { admin, credentials, FixturesApi } from './api.js';

export { expect, admin };

export const test = base.extend({
  serverReady: [async ({ playwright }, use, workerInfo) => {
    const request = await playwright.request.newContext({
      baseURL: workerInfo.project.use.baseURL,
    });
    try {
      // The root URL can serve the initialization page before authentication is ready.
      await expect(async () => {
        const response = await request.get('~api/server/ready', { timeout: 5000 });
        try {
          expect(response.status()).toBe(200);
          expect(await response.json()).toBe(true);
        } finally {
          await response.dispose();
        }
      }).toPass({ timeout: 10 * 60 * 1000, intervals: [1000] });
    } finally {
      await request.dispose();
    }
    await use();
  }, { scope: 'worker', auto: true, timeout: 11 * 60 * 1000 }],
  api: [async ({ playwright, serverReady }, use, workerInfo) => {
    const request = await playwright.request.newContext({
      baseURL: workerInfo.project.use.baseURL, extraHTTPHeaders: credentials(admin),
    });
    const api = new FixturesApi(request);
    try {
      await use(api);
    } finally {
      await api.dispose();
    }
  }, { scope: 'worker' }],
  disableAutoFocus: [async ({ context }, use) => {
    await context.addInitScript(() => {
      const add = () => document.documentElement.classList.add('no-autofocus');
      // Init scripts run before the document is parsed, so documentElement is null.
      if (document.documentElement) {
        add();
      } else {
        new MutationObserver((_, observer) => {
          if (document.documentElement) {
            add();
            observer.disconnect();
          }
        }).observe(document, { childList: true });
      }
    });
    await use();
  }, { auto: true }],
});
