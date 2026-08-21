import { expect, test } from '@playwright/test';
import {
  authorizeIssueUser,
  authorizeUser,
  createProject,
  createUser,
  login,
  logout,
  openIssue,
} from './helpers.js';

test.describe('confidential issue access', () => {
  test.describe.configure({ mode: 'serial' });

  /** @type {string} */
  let projectName;
  /** @type {{ userName: string, password: string }} */
  let creator;
  /** @type {{ userName: string, password: string }} */
  let other;
  /** @type {{ userName: string, password: string }} */
  let granted;
  /** @type {string} */
  let issueUrl;
  /** @type {string} */
  let issueTitle;

  test('normal user can access confidential issue created by himself', async ({ page }) => {
    test.setTimeout(180_000);

    const suffix = Date.now();

    await login(page, 'admin', 'admin');
    creator = await createUser(page, { userName: `creator${suffix}`, password: 'userpass1' });
    other = await createUser(page, { userName: `other${suffix}`, password: 'userpass1' });
    granted = await createUser(page, { userName: `granted${suffix}`, password: 'userpass1' });
    projectName = await createProject(page, `confidential-${suffix}`);
    await authorizeUser(page, projectName, creator.userName, 'Issue Reporter');
    await authorizeUser(page, projectName, other.userName, 'Issue Reporter');
    await authorizeUser(page, projectName, granted.userName, 'Issue Reporter');

    await logout(page);
    await login(page, creator.userName, creator.password);
    ({ title: issueTitle, url: issueUrl } = await openIssue(page, projectName, {
      title: `Confidential ${suffix}`,
      confidential: true,
    }));

    await expect(
      page.locator('.issue-editable-title [data-tippy-content="Confidential"]'),
    ).toBeVisible();
    await expect(page.locator('.issue-editable-title')).toContainText(issueTitle);
  });

  test('normal user cannot access confidential issue created by others', async ({ page }) => {
    test.setTimeout(60_000);

    await login(page, other.userName, other.password);
    const response = await page.goto(issueUrl);

    expect(response?.status()).toBe(403);
    await expect(page.locator('.title h3')).toContainText('OOPS! There Is An Error');
    await expect(page.locator('.sub-title')).toContainText('You are not allowed to perform this operation');
    await expect(page.locator('.issue-editable-title')).toHaveCount(0);
  });

  test('normal user authorized to a confidential issue created by others can access it', async ({
    page,
  }) => {
    test.setTimeout(90_000);

    await login(page, creator.userName, creator.password);
    await authorizeIssueUser(page, issueUrl, granted.userName);

    await logout(page);
    await login(page, granted.userName, granted.password);
    await page.goto(issueUrl);

    await expect(page.locator('.issue-editable-title')).toContainText(issueTitle);
    await expect(
      page.locator('.issue-editable-title [data-tippy-content="Confidential"]'),
    ).toBeVisible();
  });
});
