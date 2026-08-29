import { expect, test } from '@playwright/test';
import { createProject, login, openIssue } from './helpers.js';

test('admin can edit an issue field from the issue list', async ({ page }) => {
  test.setTimeout(60_000);

  const indicatorErrors = [];
  page.on('console', (message) => {
    if (message.type() === 'error' && message.text().includes('successIndicatorTimeout')) {
      indicatorErrors.push(message.text());
    }
  });

  await login(page, 'admin', 'admin');
  const projectName = await createProject(page);
  await openIssue(page, projectName);

  await page.goto(`${projectName}/~issues`);
  const field = page.locator('tr.issue .field-values.editable').first();
  await expect(field).toBeVisible();
  await field.click();

  expect(indicatorErrors).toEqual([]);
  await expect(page.locator('.floating.inplace-property-edit')).toBeVisible();
});
