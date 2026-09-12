import { admin, expect, test } from './fixtures.js';
import { login } from './helpers.js';

test('tags mode accepts new list values and job names and persists them on save', async ({ page, api }) => {
  const project = await api.createProject();
  await login(page, admin.name, admin.password);
  await page.goto(`${project.name}/~settings/branch-protection`);
  await page.getByRole('link', { name: 'Add Rule', exact: true }).click();
  await page.locator('.property-branches input').fill('main');

  const fileTypes = page.locator('.property-disallowedFileTypes');
  const jobs = page.locator('.property-jobNames');
  const addTag = async (field, value) => {
    const search = field.locator('.select2-search--inline textarea');
    // Select2 tracks keydown state after Enter; exercise real typing events.
    await search.pressSequentially(value);
    await expect(page.getByRole('option', { name: value, exact: true })).toBeVisible();
    await search.press('Enter');
    await expect(field.locator('.select2-selection__choice__display').filter({ hasText: value })).toHaveCount(1);
  };
  await addTag(fileTypes, 'exe');
  await addTag(fileTypes, 'bin');
  await addTag(jobs, 'new-build-job');
  await addTag(jobs, 'another-build-job');
  await fileTypes.getByRole('button', { name: 'Remove item', exact: true }).first().click();
  await addTag(fileTypes, 'exe');
  await page.keyboard.press('Escape');
  await page.getByRole('button', { name: 'Save', exact: true }).click();
  await expect(page.getByRole('link', { name: 'Add Rule', exact: true })).toBeVisible();

  const setting = await api.json('get', `~api/projects/${project.id}/setting`);
  expect(setting.branchProtections[0].disallowedFileTypes).toEqual(['bin', 'exe']);
  expect(setting.branchProtections[0].jobNames).toEqual(['new-build-job', 'another-build-job']);
  await page.reload();
  await page.locator('[data-tippy-content="Edit this rule"]').click();
  await expect(fileTypes.locator('.select2-selection__choice__display')).toHaveText(['bin', 'exe']);
  await expect(jobs.locator('.select2-selection__choice__display')).toHaveText(['new-build-job', 'another-build-job']);
});

test('single tags mode accepts and restores a new touched-file pattern', async ({ page, api }) => {
  const project = await api.createProject();
  await api.putFile(project, 'README.md', '# Tags test');
  await login(page, admin.name, admin.password);
  await page.goto(`${project.name}/~commits`);
  await page.locator('.commit-list a.filter').click();
  const file = page.locator('.commit-filter-edit .form-group').filter({ has: page.getByText('Touched File', { exact: true }) });
  await file.locator('.select2-selection').click();
  const search = page.locator('.select2-dropdown .select2-search__field');
  await search.fill('generated/*.txt');
  await expect(page.getByRole('option', { name: 'generated/*.txt', exact: true })).toBeVisible();
  await search.press('Enter');
  await expect(page.getByPlaceholder('Query commits', { exact: true })).toHaveValue(/generated\/\*\.txt/);
  await page.reload();
  await page.locator('.commit-list a.filter').click();
  await expect(file.locator('select')).toHaveValue('generated/*.txt');
  await expect(file.locator('.select2-selection__rendered')).toHaveText('generated/*.txt');
});
