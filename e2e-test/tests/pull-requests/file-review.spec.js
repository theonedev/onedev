import { admin, expect, test } from '../fixtures.js';
import { login } from '../helpers.js';
import {
  approvePullRequest, expectPendingReview, expectRebasedHead, mergeButton, openPullRequest, rebaseSourceBranch,
  reviewStatus, waitForMergeButton, waitForPullRequestCommit,
} from './helpers.js';

test('required file approval survives an unrelated rebase and resets when a reviewed file changes', async ({ page, browser, baseURL, api }) => {
  const project = await api.createProject();
  const reviewer = await api.createUser();
  await api.authorizeUser(project, reviewer, 'Code Reader');

  const sourceBranch = 'reviewed-change';
  await api.putFile(project, 'README.md', 'Project notes\n');
  await api.putFile(project, 'protected/config.txt', 'initial config\n');
  await api.putFile(project, 'protected/policy.txt', 'initial policy\n');

  await api.setBranchProtections(project, [{
    branches: 'main',
    fileProtections: [{ paths: 'protected/**', reviewRequirement: `user(${reviewer.userName})` }],
  }]);

  await api.createBranch(project, sourceBranch);
  await api.putFile(project, 'protected/config.txt', 'updated config\n', 'Update protected config', sourceBranch);
  const { commitHash: originalSourceHead } = await api.putFile(
    project, 'protected/policy.txt', 'updated policy\n', 'Update protected policy', sourceBranch,
  );

  await login(page, admin.name, admin.password);
  const pullRequestUrl = await openPullRequest(page, project, { sourceBranch, title: 'Update protected files' });
  const merge = mergeButton(page);
  await expectPendingReview(page, reviewer);

  const reviewerPage = await browser.newPage({ baseURL });
  try {
    await login(reviewerPage, reviewer.userName, reviewer.password);
    await reviewerPage.goto(pullRequestUrl);
    await approvePullRequest(reviewerPage, reviewer);
  } finally {
    await reviewerPage.close();
  }

  await waitForMergeButton(page);
  const { commitHash: targetHead } = await api.putFile(project, 'README.md', 'Updated project notes\n');
  await rebaseSourceBranch(page);
  const rebasedHead = await expectRebasedHead(api, project, sourceBranch, targetHead, 2);
  expect(rebasedHead).not.toBe(originalSourceHead);
  await waitForPullRequestCommit(page, rebasedHead);
  await expect(reviewStatus(page, reviewer)).toHaveAttribute('data-tippy-content', 'Approved');
  await expect(merge).toBeVisible();

  const { commitHash: followUpHead } = await api.putFile(
    project, 'protected/policy.txt', 'revised policy after approval\n', 'Revise reviewed policy', sourceBranch,
  );
  await waitForPullRequestCommit(page, followUpHead);
  await expectPendingReview(page, reviewer);
});
