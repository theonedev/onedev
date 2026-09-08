import { expect, test } from './fixtures.js';
import { login } from './helpers.js';

test.describe('confidential issue access', () => {
  let creator;
  let other;
  let issue;

  // Each case creates its own fixture and can run independently.
  test.beforeEach(async ({ api }) => {
    const project = await api.createProject();
    creator = await api.createUser();
    other = await api.createUser();
    await api.authorizeUser(project, creator);
    await api.authorizeUser(project, other);
    issue = await api.createIssue(project, { confidential: true, creator });
  });

  test('normal user can access confidential issue created by himself', async ({ page }) => {
    await login(page, creator.userName, creator.password);
    await page.goto(issue.url);
    await expect(page.locator('.issue-editable-title [data-tippy-content="Confidential"]')).toBeVisible();
    await expect(page.locator('.issue-editable-title')).toContainText(issue.title);
  });

  test('normal user cannot access confidential issue created by others', async ({ page }) => {
    await login(page, other.userName, other.password);
    const response = await page.goto(issue.url);
    expect(response?.status()).toBe(403);
    await expect(page.locator('.title h3')).toContainText('OOPS! There Is An Error');
    await expect(page.locator('.sub-title')).toContainText('You are not allowed to perform this operation');
    await expect(page.locator('.issue-editable-title')).toHaveCount(0);
  });

  test('normal user authorized to a confidential issue created by others can access it', async ({ page, api }) => {
    await api.authorizeIssueUser(issue, other, creator);
    await login(page, other.userName, other.password);
    await page.goto(issue.url);
    await expect(page.locator('.issue-editable-title')).toContainText(issue.title);
    await expect(page.locator('.issue-editable-title [data-tippy-content="Confidential"]')).toBeVisible();
  });
});
