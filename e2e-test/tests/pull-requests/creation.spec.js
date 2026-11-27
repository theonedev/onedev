import { execFile } from 'node:child_process';
import { mkdtemp, rm } from 'node:fs/promises';
import { devNull, tmpdir } from 'node:os';
import { join } from 'node:path';
import { promisify } from 'node:util';
import { credentials } from '../api.js';
import { admin, expect, test as base } from '../fixtures.js';
import { login } from '../helpers.js';
import { openPullRequest, sendPullRequestButton, visitPullRequestCreation, waitForMergePreview } from './helpers.js';

const exec = promisify(execFile);
const feature = 'feature';

const test = base.extend({
  project: async ({ api }, use) => {
    const project = await api.createProject();
    await api.putFile(project, 'README.md', 'Project notes\n');
    await use(project);
  },
});

test.beforeEach(async ({ page }) => {
  await login(page, admin.name, admin.password);
});

async function createFeatureBranch(api, project) {
  await api.createBranch(project, feature);
  await api.putFile(project, 'feature.txt', 'Feature notes\n', 'Add feature notes', feature);
}

async function pushUnrelatedBranch(project, branch, baseURL) {
  const directory = await mkdtemp(join(tmpdir(), 'onedev-unrelated-'));
  try {
    const git = (...args) => exec('git', args, {
      cwd: directory,
      timeout: 30_000,
      env: {
        ...process.env, GIT_CONFIG_NOSYSTEM: '1', GIT_CONFIG_GLOBAL: devNull, GIT_TERMINAL_PROMPT: '0',
        GIT_CONFIG_COUNT: '1', GIT_CONFIG_KEY_0: 'http.extraHeader',
        GIT_CONFIG_VALUE_0: `Authorization: ${credentials(admin).Authorization}`,
      },
    });
    await git('init', '--quiet');
    await git('-c', 'user.name=OneDev E2E', '-c', 'user.email=e2e@example.com', '-c', 'commit.gpgsign=false',
      'commit', '--allow-empty', '-m', 'Start unrelated history');
    const repositoryUrl = new URL(`${project.name}.git`, `${baseURL.replace(/\/$/, '')}/`);
    await git('push', repositoryUrl.href, `HEAD:refs/heads/${branch}`);
  } finally {
    await rm(directory, { recursive: true, force: true });
  }
}

async function selectSourceBranch(page, project, branch) {
  await page.locator('.new-pull-request .compare .branch-picker').click();
  await page.locator('.branch-selector').getByRole('link', { name: branch, exact: true }).click();
  await expect(page).toHaveURL(url => url.searchParams.get('source') === `${project.id}:${branch}`);
}

function creationStatus(page) {
  return page.locator('.new-pull-request > .status');
}

test('asks for different branches when source and target are the same', async ({ page, api, project }) => {
  await createFeatureBranch(api, project);
  await visitPullRequestCreation(page, project, { sourceBranch: feature });
  await expect(sendPullRequestButton(page)).toBeVisible();

  await selectSourceBranch(page, project, 'main');
  await expect(creationStatus(page).getByText('Please select different branches', { exact: true })).toBeVisible();
  await expect(sendPullRequestButton(page)).toBeHidden();
});

test('shows when the target already contains all source commits', async ({ page, api, project }) => {
  await createFeatureBranch(api, project);
  const requestId = await api.createPullRequest(project, {
    sourceBranch: feature, title: 'Add feature notes', mergeStrategy: 'CREATE_MERGE_COMMIT',
  });
  const requestUrl = `~api/pulls/${requestId}`;
  await waitForMergePreview(api, requestUrl);
  await expect(await api.request.post(`${requestUrl}/merge`)).toBeOK();

  await visitPullRequestCreation(page, project, { sourceBranch: 'main' });
  await selectSourceBranch(page, project, feature);
  const status = creationStatus(page);
  await expect(status).toContainText(`Branch main is up to date with all commits from ${feature}.`);
  await expect(sendPullRequestButton(page)).toBeHidden();
  await status.getByRole('link', { name: 'swap source and target', exact: true }).click();
  await expect(sendPullRequestButton(page)).toBeVisible();
});

test('warns when the selected branches have unrelated histories', async ({ page, project, baseURL }) => {
  const branch = 'unrelated-history';
  await pushUnrelatedBranch(project, branch, baseURL);

  await visitPullRequestCreation(page, project, { sourceBranch: 'main' });
  await selectSourceBranch(page, project, branch);
  await expect(creationStatus(page).getByText('History of target branch and source branch are unrelated', { exact: true })).toBeVisible();
  await expect(sendPullRequestButton(page)).toBeHidden();
});

test('links to the open pull request for the selected branches', async ({ page, api, project }) => {
  await createFeatureBranch(api, project);
  const existingRequest = await openPullRequest(page, project, { sourceBranch: feature, title: 'Add feature notes' });

  await visitPullRequestCreation(page, project, { sourceBranch: 'main' });
  await selectSourceBranch(page, project, feature);
  const status = creationStatus(page);
  await expect(status).toContainText('This change is already opened for merge by');
  await expect(sendPullRequestButton(page)).toBeHidden();
  const requestNumber = existingRequest.split('/').at(-1);
  await status.getByRole('link', { name: `pull request #${requestNumber}`, exact: true }).click();
  await expect(page).toHaveURL(url => url.pathname === existingRequest);
});
