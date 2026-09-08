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
