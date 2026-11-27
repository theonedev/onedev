import { expect, test } from '../fixtures.js';
import { login } from '../helpers.js';
import {
  approvePullRequest, expectPendingReview, expectRebasedHead, mergeButton, openPullRequest, rebaseSourceBranch,
  reviewStatus, waitForMergeButton, waitForPullRequestCommit,
} from './helpers.js';

test('branch-level approval resets for a new PR file and survives an unrelated rebase', async ({ page, browser, baseURL, api }) => {
  const project = await api.createProject();
  const author = await api.createUser();
  const reviewer = await api.createUser();
  await api.authorizeUser(project, author, 'Code Writer');
  await api.authorizeUser(project, reviewer, 'Code Reader');

  const sourceBranch = 'reviewed-change';
  await api.putFile(project, 'README.md', 'Project notes\n');
  await api.createBranch(project, sourceBranch);
  await api.putFile(project, 'README.md', 'Updated project notes\n', 'Update project notes', sourceBranch);

  await api.setBranchProtections(project, [{
    branches: 'main',
    // Apply the review rule to the PR author and let the fixture admin advance main independently.
    userMatch: `user(${author.userName})`,
    reviewRequirement: `user(${reviewer.userName})`,
  }]);

  await login(page, author.userName, author.password);
  const pullRequestUrl = await openPullRequest(page, project, { sourceBranch, title: 'Update project notes' });
  const status = reviewStatus(page, reviewer);
  const merge = mergeButton(page);
  await expectPendingReview(page, reviewer);

  const reviewerPage = await browser.newPage({ baseURL });
  try {
    await login(reviewerPage, reviewer.userName, reviewer.password);
    await reviewerPage.goto(pullRequestUrl);
    await approvePullRequest(reviewerPage, reviewer);
    await waitForMergeButton(page);

    const { commitHash: sourceHead } = await api.putFile(
      project, 'pr-notes.txt', 'New pull request notes\n', 'Add PR notes', sourceBranch,
    );
    await waitForPullRequestCommit(page, sourceHead);
    await expectPendingReview(page, reviewer);

    await reviewerPage.reload();
    await approvePullRequest(reviewerPage, reviewer);

    const { commitHash: targetHead } = await api.putFile(project, 'release-notes.txt', 'New release notes\n');
    await rebaseSourceBranch(page);
    const rebasedHead = await expectRebasedHead(api, project, sourceBranch, targetHead, 2);
    expect(rebasedHead).not.toBe(sourceHead);
    await waitForPullRequestCommit(page, rebasedHead);
    await expect(status).toHaveAttribute('data-tippy-content', 'Approved');
    await expect(merge).toBeVisible();
  } finally {
    await reviewerPage.close();
  }
});
