import { expect, test } from '@playwright/test';
import { createUser, login } from './helpers.js';

test('admin can filter users by type', async ({ page }) => {
  test.setTimeout(120_000);

  const suffix = Date.now();
  const prefix = `typefilter${suffix}`;
  const ordinaryName = `${prefix}-ordinary`;
  const serviceName = `${prefix}-service`;
  const aiName = `${prefix}-ai`;

  await login(page, 'admin', 'admin');
  await createUser(page, { userName: ordinaryName });
  await createUser(page, { userName: serviceName, type: 'Service' });
  await createUser(page, { userName: aiName, type: 'AI' });

  await page.goto('~administration/users');
  await page.getByPlaceholder('Filter by name or email address').fill(prefix);
  const usersTable = page.locator('table.users');
  await expect(usersTable).toContainText(ordinaryName);
  await expect(usersTable).toContainText(serviceName);
  await expect(usersTable).toContainText(aiName);

  const typeFilter = page.locator('.user-list a.dropdown-link').filter({ hasNotText: 'Operations' });
  await expect(typeFilter).toContainText('All Types');

  await typeFilter.click();
  await page.locator('.floating.menu a').filter({ hasText: 'AI' }).click();
  await expect(typeFilter).toContainText('AI');
  await expect(usersTable).toContainText(aiName);
  await expect(usersTable).not.toContainText(ordinaryName);
  await expect(usersTable).not.toContainText(serviceName);

  await typeFilter.click();
  await page.locator('.floating.menu a').filter({ hasText: 'All Types' }).click();
  await expect(typeFilter).toContainText('All Types');
  await expect(usersTable).toContainText(ordinaryName);
  await expect(usersTable).toContainText(serviceName);
  await expect(usersTable).toContainText(aiName);
});
