# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: confidential-issue.spec.js >> Issue Reporter can create and view a confidential issue
- Location: tests/confidential-issue.spec.js:4:1

# Error details

```
Test timeout of 120000ms exceeded.
```

```
Error: locator.fill: Test timeout of 120000ms exceeded.
Call log:
  - waiting for getByPlaceholder('Login name or email address')

```

# Page snapshot

```yaml
- generic [ref=e2]:
  - generic [ref=e5]:
    - heading "Page Not Found" [level=3] [ref=e6]
    - generic [ref=e7]: I didn't eat it. I swear!
  - link "Back To Home" [ref=e8] [cursor=pointer]:
    - /url: /
```

# Test source

```ts
  1  | /**
  2  |  * @param {import('@playwright/test').Page} page
  3  |  * @param {string} userName
  4  |  * @param {string} password
  5  |  */
  6  | export async function login(page, userName, password) {
  7  |   await page.goto('~/login');
> 8  |   await page.getByPlaceholder('Login name or email address').fill(userName);
     |                                                              ^ Error: locator.fill: Test timeout of 120000ms exceeded.
  9  |   await page.getByPlaceholder('Password').fill(password);
  10 |   await page.getByRole('button', { name: 'Sign in' }).click();
  11 |   await page.waitForURL((url) => !url.pathname.includes('~/login'));
  12 | }
  13 | 
  14 | /**
  15 |  * @param {import('@playwright/test').Page} page
  16 |  */
  17 | export async function logout(page) {
  18 |   await page.goto('~/logout');
  19 |   await page.waitForURL(/~login/);
  20 | }
  21 | 
  22 | /**
  23 |  * Locate a BeanEditor form-group by its visible label text.
  24 |  * @param {import('@playwright/test').Page | import('@playwright/test').Locator} root
  25 |  * @param {string} label
  26 |  */
  27 | export function formGroup(root, label) {
  28 |   return root.locator('.form-group').filter({
  29 |     has: root.locator('label.name > span').getByText(label, { exact: true }),
  30 |   });
  31 | }
  32 | 
  33 | /**
  34 |  * @param {import('@playwright/test').Page | import('@playwright/test').Locator} root
  35 |  * @param {string} label
  36 |  * @param {string} value
  37 |  */
  38 | export async function fillLabeledInput(root, label, value) {
  39 |   await formGroup(root, label).locator('input.form-control, textarea.form-control').first().fill(value);
  40 | }
  41 | 
  42 | /**
  43 |  * Select an option in a Select2 single or multi choice.
  44 |  * @param {import('@playwright/test').Page} page
  45 |  * @param {import('@playwright/test').Locator} container
  46 |  * @param {string} optionText
  47 |  */
  48 | export async function select2Choose(page, container, optionText) {
  49 |   const select2 = container.locator('.select2-container').first();
  50 |   const multiInput = select2.locator('input.select2-input');
  51 |   if (await multiInput.count()) {
  52 |     await multiInput.click();
  53 |     await multiInput.fill(optionText);
  54 |   } else {
  55 |     await select2.locator('.select2-choice').click();
  56 |     await page.locator('#select2-drop input.select2-input').fill(optionText);
  57 |   }
  58 |   await page
  59 |     .locator('#select2-drop .select2-result-selectable .select2-result-label')
  60 |     .filter({ hasText: new RegExp(`^${escapeRegExp(optionText)}$`) })
  61 |     .click();
  62 |   await page.locator('#select2-drop').waitFor({ state: 'hidden' }).catch(() => {});
  63 | }
  64 | 
  65 | /**
  66 |  * @param {string} value
  67 |  */
  68 | function escapeRegExp(value) {
  69 |   return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  70 | }
  71 | 
```