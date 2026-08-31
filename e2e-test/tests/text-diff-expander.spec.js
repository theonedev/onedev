import { execFileSync } from 'node:child_process';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';

import { expect, test } from '@playwright/test';
import { fillLabeledInput, login } from './helpers.js';

test('expands text-diff gaps with directional arrows', async ({ page }) => {
  test.setTimeout(120_000);

  // 1. Open a commit containing two distant changes
  await login(page, 'admin', 'admin');
  const projectName = `text-diff-expander-${Date.now()}`;
  await page.goto('~projects/new');
  await fillLabeledInput(page, 'Name', projectName);
  await page.getByRole('button', { name: 'Create' }).click();
  await expect(page).toHaveURL(new RegExp(`/${projectName}(/|$)`));
  const repositoryDir = mkdtempSync(path.join(tmpdir(), 'onedev-text-diff-'));
  let commitHash;
  try {
    const git = (...args) => execFileSync('git', args, { cwd: repositoryDir, stdio: 'pipe' });
    git('init', '--initial-branch=main');
    git('config', 'user.name', 'OneDev E2E');
    git('config', 'user.email', 'e2e@example.com');

    const lines = Array.from({ length: 100 }, (_, index) => `unchanged-line-${index + 1}`);
    writeFileSync(path.join(repositoryDir, 'fixture.txt'), `${lines.join('\n')}\n`);
    git('add', 'fixture.txt');
    git('commit', '-m', 'Add text diff fixture');

    lines[9] = 'changed-line-10';
    lines[89] = 'changed-line-90';
    writeFileSync(path.join(repositoryDir, 'fixture.txt'), `${lines.join('\n')}\n`);
    git('commit', '-am', 'Change distant fixture lines');
    commitHash = git('rev-parse', 'HEAD').toString().trim();

    const remoteUrl = new URL(page.url());
    remoteUrl.username = 'admin';
    remoteUrl.password = 'admin';
    remoteUrl.pathname = `/${projectName}`;
    remoteUrl.search = '';
    remoteUrl.hash = '';
    git('remote', 'add', 'origin', remoteUrl.toString());
    git('push', 'origin', 'main');
  } finally {
    rmSync(repositoryDir, { recursive: true, force: true });
  }

  const commitUrl = `${projectName}/~commits/${commitHash}`;
  await page.goto(commitUrl);
  const expanders = page.locator('tr.expander');
  await expect(expanders).toHaveCount(3);
  const startRow = expanders.first();
  const endRow = expanders.last();
  const directionalRow = expanders.filter({ has: page.locator('.expander-controls') });
  await expect(directionalRow).toHaveCount(1);

  await expect(startRow.locator('.expand-up')).toBeVisible();
  await expect(startRow.getByLabel('Show more lines below')).toBeVisible();
  await expect(startRow.locator('.expand-down')).toHaveCount(0);

  await expect(endRow.locator('.expand-down')).toBeVisible();
  await expect(endRow.getByLabel('Show more lines above')).toBeVisible();
  await expect(endRow.locator('.expand-up')).toHaveCount(0);

  const controls = directionalRow.locator('.expander-controls > a');
  await expect(controls).toHaveCount(2);
  await expect(directionalRow.locator('td.expander .expand-down')).toBeVisible();
  await expect(directionalRow.locator('td.expander .expand-up')).toBeVisible();
  await expect(directionalRow.getByLabel('Show more lines above')).toBeVisible();
  await expect(directionalRow.getByLabel('Show more lines below')).toBeVisible();

  // Skipped lines are informational only, without any expand control
  for (const row of [startRow, directionalRow, endRow]) {
    await expect(row.locator('td.skipped')).toContainText(/skipped \d+ lines/);
    await expect(row.locator('td.skipped a')).toHaveCount(0);
  }

  const [downBox, upBox, directionalWidth, singleWidth] = await Promise.all([
    directionalRow.locator('.expand-down').evaluate((element) => element.getBoundingClientRect()),
    directionalRow.locator('.expand-up').evaluate((element) => element.getBoundingClientRect()),
    directionalRow.locator('td.expander').evaluate((element) => element.getBoundingClientRect().width),
    startRow.locator('td.expander').evaluate((element) => element.getBoundingClientRect().width),
  ]);
  expect(downBox.left).toBeLessThan(upBox.left);
  expect(Math.abs(downBox.top - upBox.top)).toBeLessThan(2);
  expect(Math.abs(directionalWidth - singleWidth)).toBeLessThan(2);

  const rowClass = (await directionalRow.getAttribute('class')).split(/\s+/).find((name) => /^expander\d+$/.test(name));
  const middleRow = page.locator(`tr.${rowClass}`);

  // 2. Expand the bottom of the preceding hunk
  await directionalRow.locator('.expand-down').click();
  await expect(page.getByText('unchanged-line-28', { exact: true })).toBeVisible();
  await expect(page.getByText('unchanged-line-71', { exact: true })).toHaveCount(0);

  // 3. Reload and expand the top of the following hunk
  await page.goto(commitUrl);
  await page.locator(`tr.${rowClass} .expand-up`).click();
  await expect(page.getByText('unchanged-line-72', { exact: true })).toBeVisible();
  await expect(page.getByText('unchanged-line-29', { exact: true })).toHaveCount(0);

  // 4. Expand both halves of the same gap
  await page.goto(commitUrl);
  await page.locator(`tr.${rowClass} .expand-down`).click();
  await expect(page.getByText('unchanged-line-28', { exact: true })).toBeVisible();
  await page.locator(`tr.${rowClass} .expand-up`).click();
  await expect(page.getByText('unchanged-line-72', { exact: true })).toBeVisible();

  // 5. Expand the top of the first hunk
  await page.goto(commitUrl);
  await startRow.locator('.expand-up').click();
  await expect(page.getByText('unchanged-line-1', { exact: true })).toBeVisible();

  // 6. Expand the bottom of the last hunk
  await page.goto(commitUrl);
  await endRow.locator('.expand-down').click();
  await expect(page.getByText('unchanged-line-100', { exact: true })).toBeVisible();

  // 7. Keep both arrows while any line of the gap is still skipped
  await page.goto(commitUrl);
  for (const remaining of [43, 13]) {
    await middleRow.locator('.expand-down').click();
    await expect(middleRow.locator('td.skipped')).toContainText(`skipped ${remaining} lines`);
    await expect(middleRow.locator('.expand-down')).toBeVisible();
    await expect(middleRow.locator('.expand-up')).toBeVisible();
  }

  // 8. Reveal the rest of the gap
  await middleRow.locator('.expand-down').click();
  await expect(middleRow).toHaveCount(0);
});
