import { expect, test, admin } from './fixtures.js';
import { login } from './helpers.js';

test('admin can edit an issue field from the issue list', async ({ page, api }) => {
  const indicatorErrors = [];
  page.on('console', (message) => {
    if (message.type() === 'error' && message.text().includes('successIndicatorTimeout')) {
      indicatorErrors.push(message.text());
    }
  });

  await login(page, admin.name, admin.password);
  const project = await api.createProject();
  await api.createIssue(project, { fields: { Type: 'Bug', Priority: 'Normal' } });

  await page.goto(`${project.name}/~issues`);
  const field = page.locator('tr.issue .field-values.editable').first();
  await expect(field).toBeVisible();
  await field.click();

  expect(indicatorErrors).toEqual([]);
  await expect(page.locator('.floating.inplace-property-edit')).toBeVisible();
});
