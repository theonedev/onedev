import { expect, test as base } from '@playwright/test';

export { expect };

export const test = base.extend({
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
