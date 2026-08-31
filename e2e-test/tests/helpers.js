import { expect } from '@playwright/test';

/**
 * @param {import('@playwright/test').Page} page
 * @param {string} userName
 * @param {string} password
 */
export async function login(page, userName, password) {
  await page.goto('~login');
  await page.getByPlaceholder('Login name or email address', { exact: true }).fill(userName);
  await page.locator('form input[type="password"]').fill(password);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.waitForURL((url) => !url.pathname.includes('~login'));
}

/**
 * @param {import('@playwright/test').Page} page
 */
export async function logout(page) {
  await page.goto('~logout');
  await page.waitForURL(/~login/);
}

/**
 * Locate a BeanEditor form-group by its visible label text.
 * @param {import('@playwright/test').Page | import('@playwright/test').Locator} root
 * @param {string} label
 */
export function formGroup(root, label) {
  return root.locator('.form-group').filter({
    has: root.locator('label.name > span').getByText(label, { exact: true }),
  });
}

/**
 * @param {import('@playwright/test').Page | import('@playwright/test').Locator} root
 * @param {string} label
 * @param {string} value
 */
export async function fillLabeledInput(root, label, value) {
  await formGroup(root, label).locator('input.form-control, textarea.form-control').first().fill(value);
}

/**
 * Fill a confirmative password editor (password + confirm).
 * @param {import('@playwright/test').Page | import('@playwright/test').Locator} root
 * @param {string} password
 */
export async function fillConfirmativePassword(root, password) {
  const group = formGroup(root, 'Password');
  await group.getByPlaceholder('Type password here').fill(password);
  await group.getByPlaceholder('Confirm password here').fill(password);
}

/**
 * Create a user. Ordinary and service accounts go through the admin UI.
 * AI accounts are created via REST so model settings do not need a live API.
 * Caller must be logged in as an administrator.
 * @param {import('@playwright/test').Page} page
 * @param {{ userName?: string, password?: string, email?: string, type?: 'Ordinary' | 'Service' | 'AI', apiUser?: string, apiPassword?: string }} [options]
 * @returns {Promise<{ userName: string, password: string, email: string }>}
 */
export async function createUser(page, options = {}) {
  const suffix = Date.now();
  const userName = options.userName ?? `user${suffix}`;
  const password = options.password ?? 'userpass1';
  const email = options.email ?? `${userName}@example.com`;
  const type = options.type ?? 'Ordinary';

  if (type === 'AI') {
    const apiUser = options.apiUser ?? 'admin';
    const apiPassword = options.apiPassword ?? 'admin';
    const response = await page.request.post('~api/users', {
      headers: {
        Authorization: `Basic ${Buffer.from(`${apiUser}:${apiPassword}`).toString('base64')}`,
        'Content-Type': 'application/json',
      },
      data: {
        type: 'AI',
        name: userName,
        aiSetting: {
          modelSetting: {
            baseUrl: 'http://127.0.0.1:1',
            name: 'dummy',
            timeoutSeconds: 30,
          },
        },
      },
    });
    expect(response.ok()).toBeTruthy();
    return { userName, password, email };
  }

  await page.goto('~administration/users/new');
  if (type !== 'Ordinary') {
    await select2Choose(page, formGroup(page, 'Type'), type);
  }
  await fillLabeledInput(page, 'Login Name', userName);
  if (type === 'Ordinary') {
    await fillConfirmativePassword(page, password);
    await fillLabeledInput(page, 'Email Address', email);
  }
  await page.getByRole('button', { name: 'Create' }).click();
  await expect(page).toHaveURL(/\/~users\/\d+\/basic-setting(?:\?|$)/);
  await expect(formGroup(page, 'Login Name').locator('input.form-control')).toHaveValue(userName);

  return { userName, password, email };
}

/**
 * Create a project via the UI. Caller must be logged in with permission to create projects.
 * @param {import('@playwright/test').Page} page
 * @param {string} [projectName]
 * @returns {Promise<string>}
 */
export async function createProject(page, projectName) {
  const name = projectName ?? `project${Date.now()}`;

  await page.goto('~projects/new');
  await fillLabeledInput(page, 'Name', name);
  await page.getByRole('button', { name: 'Create' }).click();
  await expect(page).toHaveURL(new RegExp(`/${name}(/|$)`));
  await expect(page.locator('.sidebar')).toContainText(name);

  return name;
}

/**
 * Authorize a user on a project with the given role.
 * @param {import('@playwright/test').Page} page
 * @param {string} projectName
 * @param {string} userName
 * @param {string} roleName
 */
export async function authorizeUser(page, projectName, userName, roleName) {
  await page.goto(`${projectName}/~settings/user-authorizations`);
  const authRows = page.locator('.user-authorizations .bean-list tbody tr');
  const countBefore = await authRows.count();
  await page.locator('.user-authorizations a.add-element').click();
  await expect(authRows).toHaveCount(countBefore + 1);
  const authRow = authRows.last();
  await select2Choose(page, authRow.locator('td').nth(1), userName);
  await select2Choose(page, authRow.locator('td').nth(2), roleName);
  await page.getByRole('button', { name: 'Save' }).click();
  await page.goto(`${projectName}/~settings/user-authorizations`);
  const savedAuthRow = page
    .locator('.user-authorizations .bean-list tbody tr')
    .filter({ hasText: userName });
  await expect(savedAuthRow).toContainText(roleName);
}

/**
 * Open (create) an issue in a project. Caller must be logged in with permission to create issues.
 * @param {import('@playwright/test').Page} page
 * @param {string} projectName
 * @param {{ title?: string, confidential?: boolean }} [options]
 * @returns {Promise<{ title: string, url: string }>}
 */
export async function openIssue(page, projectName, options = {}) {
  const title = options.title ?? `Issue ${Date.now()}`;
  const confidential = options.confidential ?? false;

  await page.goto(`${projectName}/~issues/new`);
  await page.getByPlaceholder('Input title here').fill(title);
  if (confidential) {
    const confidentialSwitch = page.locator('.form-group.confidential .switch');
    await confidentialSwitch.scrollIntoViewIfNeeded();
    await confidentialSwitch.locator('label').click();
  }
  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page).toHaveURL(new RegExp(`/${projectName}/~issues/\\d+`));
  await expect(page.locator('.issue-editable-title')).toContainText(title);

  return { title, url: page.url() };
}

/**
 * Authorize an additional user on a confidential issue. Caller must be able to modify the issue.
 * @param {import('@playwright/test').Page} page
 * @param {string} issueUrl Absolute or relative issue detail URL
 * @param {string} userName
 */
export async function authorizeIssueUser(page, issueUrl, userName) {
  const authorizationsUrl = issueUrl.replace(/\/?$/, '') + '/authorizations';
  await page.goto(authorizationsUrl);
  await select2Choose(page, page.locator('.issue-authorization-list'), userName);
  await page.goto(authorizationsUrl);
  await expect(page.locator('.issue-authorization-list .authorizations')).toContainText(userName);
}

/**
 * Select an option in a Select2 single or multi choice.
 * @param {import('@playwright/test').Page} page
 * @param {import('@playwright/test').Locator} container
 * @param {string} optionText
 */
export async function select2Choose(page, container, optionText) {
  const select2 = container.locator('.select2-container').first();
  const isMulti = await select2.evaluate((el) => el.classList.contains('select2-container-multi'));
  if (isMulti) {
    const multiInput = select2.locator('input.select2-input');
    await multiInput.click();
    await multiInput.fill(optionText);
  } else {
    await select2.locator('.select2-choice').click();
    await page.locator('#select2-drop input.select2-input').fill(optionText);
  }
  await page
    .locator('#select2-drop .select2-result-selectable')
    .filter({ hasText: optionText })
    .first()
    .click();
  await page.locator('#select2-drop').waitFor({ state: 'hidden' }).catch(() => {});
  if (isMulti && (await page.locator('#select2-drop').isVisible().catch(() => false))) {
    await page.keyboard.press('Escape');
    await page.locator('#select2-drop').waitFor({ state: 'hidden' }).catch(() => {});
  }
}
