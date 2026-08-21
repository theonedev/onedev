import { expect, test } from '@playwright/test';
import { createProject, login, openIssue } from './helpers.js';

test('admin can open an issue', async ({ page }) => {
  test.setTimeout(60_000);

  await login(page, 'admin', 'admin');
  const projectName = await createProject(page);

  const { title } = await openIssue(page, projectName);
  await expect(page.locator('.issue-editable-title')).toContainText(title);
});
