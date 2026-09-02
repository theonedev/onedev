import { expect, test } from './fixtures.js';

test('serves the OneDev web interface', async ({ page }) => {
  const response = await page.goto('/');

  expect(response).not.toBeNull();
  expect(response.ok()).toBe(true);
  await expect(page).toHaveTitle(/OneDev/i);
  await expect(page.locator('body')).toBeVisible();
});
