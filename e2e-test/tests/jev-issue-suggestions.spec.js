import { expect } from './fixtures.js';
import { test } from './jev-fixtures.js';

function field(page, name) {
  return page.locator('.new-issue .form-group').filter({
    has: page.locator('label.name > span', { hasText: new RegExp(`^${name}$`) }),
  });
}

async function openIssue(page, api) {
  const project = await api.createProject();
  await page.goto(`${project.name}/~issues/new`);
  await expect(field(page, 'Type').locator('select')).toHaveValue('');
  await expect(field(page, 'Priority').locator('select')).toHaveValue('');
  return project;
}

test('Jev suggestions populate required fields and are saved with the new issue', async ({ page, api, jev }) => {
  const project = await openIssue(page, api);
  const title = 'Login crashes when submitting an expired session';
  const description = 'The login page returns a server error instead of asking me to sign in again.';
  await page.locator('.new-issue .description textarea').fill(description);
  await page.getByPlaceholder('Input title here').fill(title);
  const request = await jev.nextRequest();
  expect(JSON.parse(request.body.state)).toEqual({ project: project.name, title, description });
  expect(Object.keys(request.body.questions)).toHaveLength(2);
  await expect(field(page, 'Type')).toContainText('Suggesting...');
  jev.reply(request);
  await expect(field(page, 'Type').locator('.select2-selection__rendered')).toHaveText('Bug');
  await expect(field(page, 'Priority').locator('.select2-selection__rendered')).toHaveText('Major');
  await page.getByRole('button', { name: 'Save', exact: true }).click();
  await page.waitForURL(new RegExp(`/${project.name}/~issues/\\d+`));
  await page.reload();
  await expect(page.locator('.issue-editable-title')).toContainText(title);
  await expect(page.locator('.field-values')).toContainText(['Bug', 'Major']);
});

test('uncertain and unknown suggestions leave fields empty and manual choices still work', async ({ page, api, jev }) => {
  await openIssue(page, api);
  await page.getByPlaceholder('Input title here').fill('Something needs attention');
  jev.reply(await jev.nextRequest(), { Type: ['Bug', 0.84], Priority: ['unknown', 0.99] });
  await expect(field(page, 'Type').locator('.select2-selection__placeholder')).not.toContainText('Suggesting...');
  await expect(field(page, 'Type').locator('select')).toHaveValue('');
  await expect(field(page, 'Priority').locator('select')).toHaveValue('');
  await field(page, 'Type').locator('.select2-selection').click();
  await page.getByRole('option', { name: 'Improvement', exact: true }).click();
  const request = await jev.nextRequest(1);
  expect(Object.keys(request.body.questions)).toHaveLength(1);
  expect(Object.values(request.body.questions)[0].instructions).toContain("'Priority'");
  jev.reply(request);
  await expect(field(page, 'Priority').locator('select')).toHaveValue('Major');
  await expect(field(page, 'Type').locator('select')).toHaveValue('Improvement');
});

test('a response for an older title is ignored and the updated title is suggested', async ({ page, api, jev }) => {
  await openIssue(page, api);
  await page.getByPlaceholder('Input title here').fill('An old bug report');
  const oldRequest = await jev.nextRequest();
  await page.getByPlaceholder('Input title here').fill('Add a new dashboard');
  jev.reply(oldRequest);
  const newRequest = await jev.nextRequest(1);
  expect(JSON.parse(newRequest.body.state).title).toBe('Add a new dashboard');
  await expect(field(page, 'Type').locator('select')).toHaveValue('');
  jev.reply(newRequest, { Type: ['New Feature', 0.99], Priority: ['Normal', 0.99] });
  await expect(field(page, 'Type').locator('select')).toHaveValue('New Feature');
  await expect(field(page, 'Priority').locator('select')).toHaveValue('Normal');
});
