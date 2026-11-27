import { admin, expect, test } from '../fixtures.js';
import { login } from '../helpers.js';
import { openPullRequest, waitForPullRequestCommit } from './helpers.js';

async function removeReviewer(page, userName) {
  const row = page.locator('ul.reviews > li').filter({
    has: page.getByText(userName, { exact: true }),
  });
  page.once('dialog', dialog => dialog.accept());
  await row.locator('[data-tippy-content="Remove this reviewer"]').click();
}

test('group reviewers are replaced only when the required count can still be met', async ({ page, api }) => {
  const project = await api.createProject();
  const members = [await api.createUser(), await api.createUser()];
  const group = await api.createGroup(members);
  const roleId = await api.json('get', '~api/roles/ids/Code%20Reader');
  await api.json('post', '~api/group-authorizations', {
    data: { projectId: project.id, groupId: group.id, roleId },
  });

  const sourceBranch = 'reviewed-change';
  await api.putFile(project, 'README.md', 'Project notes\n');
  await api.createBranch(project, sourceBranch);
  await api.putFile(project, 'README.md', 'Updated project notes\n', 'Update README', sourceBranch);

  await api.setBranchProtections(project, [{ branches: 'main', reviewRequirement: `group(${group.name}):1` }]);

  await login(page, admin.name, admin.password);
  await openPullRequest(page, project, { sourceBranch, title: 'Update project notes' });

  const reviewers = page.locator('ul.reviews a.reviewer');
  const memberNames = members.map(member => member.userName).sort();
  const reviewerNames = async () => (await reviewers.allTextContents()).map(name => name.trim()).sort();
  await expect(reviewers).toHaveCount(1);
  const selected = (await reviewers.innerText()).trim();
  expect(memberNames).toContain(selected);

  const replacement = memberNames.find(name => name !== selected);
  await removeReviewer(page, selected);
  await expect(reviewers).toHaveText([replacement]);

  await api.setBranchProtections(project, [{ branches: 'main', reviewRequirement: `group(${group.name}):2` }]);
  // A new commit makes the open pull request re-evaluate the updated rule.
  const { commitHash } = await api.putFile(project, 'README.md', 'Revised project notes\n', 'Revise README', sourceBranch);
  await waitForPullRequestCommit(page, commitHash);
  expect(await reviewerNames()).toEqual(memberNames);

  await removeReviewer(page, replacement);
  await expect(page.getByText(
    `Group '${group.name}' cannot meet its reviewer requirement (2 required, 1 eligible).`, { exact: true },
  )).toBeVisible();
  await page.reload();
  expect(await reviewerNames()).toEqual(memberNames);
});
