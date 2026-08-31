import { execFileSync } from 'node:child_process';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';

import { expect, test } from '@playwright/test';
import { fillLabeledInput, login } from './helpers.js';

test('expands a middle text-diff gap up, both, and down', async ({ page }) => {
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
  const directionalRow = page.locator('tr.expander').filter({ has: page.locator('.expander-controls') });
  await expect(directionalRow).toHaveCount(1);
  await expect(page.locator('tr.expander').filter({ hasNot: page.locator('.expander-controls') })).toHaveCount(2);

  const controls = directionalRow.locator('.expander-controls > a');
  await expect(controls).toHaveCount(3);
  await expect(directionalRow.getByLabel('Show more lines above')).toBeVisible();
  await expect(directionalRow.getByLabel('Show more lines', { exact: true })).toBeVisible();
  await expect(directionalRow.getByLabel('Show more lines below')).toBeVisible();
  await expect(controls.nth(1)).toContainText(/skipped \d+ lines/);
  const widths = await controls.evaluateAll((elements) => elements.map((element) => element.getBoundingClientRect().width));
  expect(Math.max(...widths) - Math.min(...widths)).toBeLessThan(2);

  const rowClass = (await directionalRow.getAttribute('class')).split(/\s+/).find((name) => /^expander\d+$/.test(name));
  const middleRow = page.locator(`tr.${rowClass}`);

  // 2. Expand only toward the preceding hunk
  await directionalRow.locator('.expand-up').click();
  await expect(page.getByText('unchanged-line-28', { exact: true })).toBeVisible();
  await expect(page.getByText('unchanged-line-71', { exact: true })).toHaveCount(0);

  // 3. Reload and expand only toward the following hunk
  await page.goto(commitUrl);
  await page.locator(`tr.${rowClass} .expand-down`).click();
  await expect(page.getByText('unchanged-line-72', { exact: true })).toBeVisible();
  await expect(page.getByText('unchanged-line-29', { exact: true })).toHaveCount(0);

  // 4. Reload and retain the existing both-sides expansion
  await page.goto(commitUrl);
  await page.locator(`tr.${rowClass} .expand-both`).click();
  await expect(page.getByText('unchanged-line-28', { exact: true })).toBeVisible();
  await expect(page.getByText('unchanged-line-72', { exact: true })).toBeVisible();

  // 5. Exhaust the upward half and leave one control for the downward half
  await page.goto(commitUrl);
  for (let click = 0; click < 3; click += 1)
    await page.locator(`tr.${rowClass} .expand-up`).click();
  await expect(middleRow.locator('.expander-controls')).toHaveCount(0);
  await expect(middleRow.locator('td.expander > a')).toHaveCount(1);

  // 6. Reveal the remaining half until the expander disappears
  for (let click = 0; click < 3; click += 1)
    await middleRow.locator('td.expander > a').click();
  await expect(middleRow).toHaveCount(0);
});
