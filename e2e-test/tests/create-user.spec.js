import { expect, test } from '@playwright/test';
import { createUser, login, logout } from './helpers.js';

test('admin can create a user', async ({ page }) => {
  await login(page, 'admin', 'admin');
  const { userName, password } = await createUser(page);

  await logout(page);
  await login(page, userName, password);
  await expect(page).not.toHaveURL(/~login/);
});
