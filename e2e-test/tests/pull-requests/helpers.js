import { expect } from '@playwright/test';

const reconciliationTimeout = 60_000;

export async function visitPullRequestCreation(page, project, { sourceBranch, targetBranch = 'main' }) {
  const branches = new URLSearchParams({ target: `${project.id}:${targetBranch}`, source: `${project.id}:${sourceBranch}` });
  await page.goto(`${project.name}/~pulls/new?${branches}`);
}

export async function openPullRequest(page, project, { sourceBranch, title, targetBranch = 'main' }) {
  await visitPullRequestCreation(page, project, { sourceBranch, targetBranch });
  await page.getByPlaceholder('Input title here').fill(title);
  await sendPullRequestButton(page).click();
  await page.waitForURL(/\/~pulls\/\d+(?:\?|$)/);
  return new URL(page.url()).pathname;
}

export function sendPullRequestButton(page) {
  return page.getByRole('button', { name: 'Send Pull Request', exact: true });
}

export function mergeButton(page) {
  return page.locator('.operations').getByRole('link', { name: 'Merge', exact: true });
}

export function reviewStatus(page, reviewer) {
  return page.locator('ul.reviews > li').filter({
    has: page.getByText(reviewer.userName, { exact: true }),
  }).locator('.status');
}

async function waitForPullRequestElement(page, element, message) {
  await expect.poll(async () => {
    if (await element.isVisible()) return true;
    await page.reload();
    return element.isVisible();
  }, { timeout: reconciliationTimeout, message }).toBe(true);
}

export async function expectPendingReview(page, reviewer) {
  await waitForPullRequestElement(page, page.locator('.calculated-merge-preview'), 'PR should have a conflict-free merge preview');
  await expect(reviewStatus(page, reviewer)).toHaveAttribute('data-tippy-content', 'Pending review');
  await expect(page.locator('.waiting-for-reviews')).toBeVisible();
  await expect(mergeButton(page)).toBeHidden();
}

export async function waitForMergeButton(page) {
  await waitForPullRequestElement(page, mergeButton(page), 'Merge should become available on the PR page');
}

export async function approvePullRequest(page, reviewer) {
  await page.getByRole('link', { name: 'Approve', exact: true }).click();
  const approveDialog = page.getByRole('heading', { name: 'Confirm Approve', exact: true });
  await expect(approveDialog).toBeVisible();
  await page.getByRole('button', { name: 'Ok', exact: true }).click();
  await expect(approveDialog).toBeHidden();
  await page.reload();
  await expect(reviewStatus(page, reviewer)).toHaveAttribute('data-tippy-content', 'Approved');
}

export async function waitForPullRequestCommit(page, commitHash) {
  // The source ref changes before the pull request processes the new head.
  const commit = page.locator(`.activities .commit a.hash[href$="/~commits/${commitHash}"]`);
  await expect.poll(async () => {
    await page.reload();
    return commit.count();
  }, { timeout: reconciliationTimeout, message: `PR activities should include ${commitHash}` }).toBe(1);
}

export async function rebaseSourceBranch(page) {
  await page.reload();
  await page.getByRole('link', { name: 'Update Source Branch', exact: true }).click();
  await page.getByRole('link', { name: 'Rebase', exact: true }).click();
  await expect(page.getByText('You are rebasing source branch on top of target branch', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Ok', exact: true }).click();
  await expect(page.getByText('Source branch updated successfully', { exact: true })).toBeVisible();
}

export async function expectRebasedHead(api, project, sourceBranch, targetHead, commitCount) {
  const commits = await api.json('get', `~api/repositories/${project.id}/commits`, {
    params: { query: `branch(${sourceBranch})`, count: commitCount, field: 'PARENTS' },
  });
  expect(commits).toHaveLength(commitCount);
  const expectedParents = [...commits.slice(1).map(commit => [commit.hash]), [targetHead]];
  expect(commits.map(commit => commit.parentHashes)).toEqual(expectedParents);
  return commits[0].hash;
}

export async function waitForMergePreview(api, requestUrl, expected = {}) {
  let preview;
  await expect.poll(async () => {
    const response = await api.request.get(`${requestUrl}/merge-preview`);
    try {
      if (response.status() === 204) return null;
      await expect(response).toBeOK();
      preview = await response.json();
      return preview;
    } finally {
      await response.dispose();
    }
  }, { timeout: reconciliationTimeout, message: 'PR should have a merge preview for the expected commits' })
    .toMatchObject({ ...expected, mergeCommitHash: expect.any(String) });
  return preview;
}
