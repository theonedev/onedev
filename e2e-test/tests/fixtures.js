import { expect, test as base } from '@playwright/test';

export { expect };

export const test = base.extend({
  disableAutoFocus: [async ({ context }, use) => {
    await context.addInitScript(() => {
      document.documentElement.classList.add('no-autofocus');
    });
    await use();
  }, { auto: true }],
});
