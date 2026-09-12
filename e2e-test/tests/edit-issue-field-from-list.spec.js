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
  const editor = page.locator('.floating.inplace-property-edit');
  await expect(editor).toBeVisible();
  await expect(editor.locator('.select2-selection__rendered')).toHaveText('Bug');
  await editor.locator('.select2-selection').click();
  await page.getByRole('option', { name: 'New Feature', exact: true }).click();
  await expect(editor).toBeHidden();
  await expect(field).toHaveText('New Feature');
  await page.reload();
  await expect(field).toHaveText('New Feature');
});
