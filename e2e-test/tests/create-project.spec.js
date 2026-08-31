import { expect, test } from '@playwright/test';
import { createProject, login } from './helpers.js';

test('admin can create a project', async ({ page }) => {
  await login(page, 'admin', 'admin');
  const projectName = await createProject(page);

  await page.goto(projectName);
  await expect(page).toHaveURL(new RegExp(`/${projectName}(/|$)`));
  await expect(page.locator('.sidebar')).toContainText(projectName);
});
