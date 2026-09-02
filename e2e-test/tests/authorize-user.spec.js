import { expect, test } from './fixtures.js';
import { authorizeUser, createProject, createUser, login } from './helpers.js';

test('admin can authorize a user to a project with Issue Reporter role', async ({ page }) => {
  const suffix = Date.now();

  await login(page, 'admin', 'admin');
  const { userName } = await createUser(page, {
    userName: `reporter${suffix}`,
    password: 'reporter1',
  });
  const projectName = await createProject(page, `authorize-user-${suffix}`);

  await authorizeUser(page, projectName, userName, 'Issue Reporter');

  await page.goto(`${projectName}/~settings/user-authorizations`);
  const authRow = page
    .locator('.user-authorizations .bean-list tbody tr')
    .filter({ hasText: userName });
  await expect(authRow).toContainText('Issue Reporter');
});
