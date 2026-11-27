import { expect, test as base } from '../fixtures.js';
import { login } from '../helpers.js';
import { mergeButton, waitForMergePreview } from './helpers.js';

const jobName = 'required-check';
const executorsUrl = '~api/settings/job-executors';

function buildSpec(executorName) {
  return `version: 59
jobs:
- name: ${jobName}
  jobExecutor: ${executorName}
  executeInSandbox: false
  retryCondition: never
  steps:
  - type: CheckoutStep
    name: Checkout
    cloneCredential:
      type: DefaultCredential
    condition: SUCCESSFUL
  - type: CommandStep
    name: Report checked-out commit
    runInContainer: false
    condition: SUCCESSFUL
    interpreter:
      type: PosixInterpreter
      shell: sh
      commands: |
        checked_out_commit=$(git rev-parse --verify HEAD) || exit 1
        printf 'checked-out-commit=%s\\n' "$checked_out_commit"
`;
}

const test = base.extend({
  buildProject: async ({ api }, use) => {
    const project = await api.createProject();
    const executor = {
      '@type': 'ServerHostExecutor',
      name: project.name,
      jobMatch: `"Project" is "${project.name}" and "Job" is "${jobName}"`,
    };
    const executors = await api.json('get', executorsUrl);
    await expect(await api.request.post(executorsUrl, { data: [...executors, executor] })).toBeOK();
    try {
      await use(project);
    } finally {
      // Remove the test executor before the project it references is deleted.
      const currentExecutors = await api.json('get', executorsUrl);
      await expect(await api.request.post(executorsUrl, {
        data: currentExecutors.filter(current => current.name !== executor.name),
      })).toBeOK();
    }
  },
});

// These cases share the server's job executor settings and can run two builds per case.
test.describe.configure({ mode: 'default', timeout: 6 * 60_000 });

async function runRequiredBuild(page, api, requestUrl, commitHash) {
  const requestPageUrl = page.url();
  await page.locator('.required-jobs [data-tippy-content="Run this job"]').click();
  await expect(page).toHaveURL(/\/~builds\/\d+(?:[/?]|$)/);
  let builds;
  await expect.poll(async () => {
    builds = await api.json('get', `${requestUrl}/current-builds`);
    return builds[0]?.finishDate;
  }, { timeout: 120_000, message: `Build for ${commitHash} should finish` }).toEqual(expect.any(String));
  expect(builds).toEqual([expect.objectContaining({ jobName, commitHash, status: 'SUCCESSFUL' })]);
  const build = builds[0];
  const log = await page.request.get(`~downloads/projects/${build.projectId}/builds/${build.number}/log`);
  try {
    await expect(log).toBeOK();
    expect(await log.text()).toContain(`checked-out-commit=${commitHash}`);
  } finally {
    await log.dispose();
  }
  await page.goto(requestPageUrl);
  return build.id;
}

for (const strict of [false, true]) {
  test(`branch protection requires a successful build with strict mode ${strict ? 'on' : 'off'}`, async ({ page, api, buildProject }) => {
    const project = buildProject;
    const author = await api.createUser();
    await api.authorizeUser(project, author, 'Code Writer');

    const sourceBranch = 'feature';
    const { commitHash: initialTarget } = await api.putFile(project, '.onedev-buildspec.yml', buildSpec(project.name));
    await api.createBranch(project, sourceBranch);
    const { commitHash: initialSource } = await api.putFile(project, 'feature.txt', 'Initial feature\n', 'Add feature', sourceBranch);

    await api.setBranchProtections(project, [{
      branches: 'main',
      // The fixture admin advances main independently of the PR author.
      userMatch: `user(${author.userName})`,
      jobNames: [jobName],
      requireStrictBuilds: strict,
    }]);

    const requestId = await api.createPullRequest(project, { sourceBranch, title: 'Add feature', creator: author });
    const requestUrl = `~api/pulls/${requestId}`;
    const { mergeCommitHash: initialMerge } = await waitForMergePreview(api, requestUrl, {
      targetHeadCommitHash: initialTarget, headCommitHash: initialSource,
    });
    const { number } = await api.json('get', requestUrl);
    await login(page, author.userName, author.password);
    await page.goto(`${project.name}/~pulls/${number}`);

    const merge = mergeButton(page);
    const requiredJob = page.locator('.required-jobs').getByText(jobName, { exact: true });
    await expect(requiredJob).toBeVisible();
    await expect(merge).toBeHidden();
    const initialBuildId = await runRequiredBuild(page, api, requestUrl, initialMerge);
    await expect(merge).toBeVisible();

    const { commitHash: newTarget } = await api.putFile(project, 'target-notes.txt', 'Target-only change\n');
    const { mergeCommitHash: targetMerge } = await waitForMergePreview(api, requestUrl, {
      targetHeadCommitHash: newTarget, headCommitHash: initialSource,
    });
    expect(targetMerge).not.toBe(initialMerge);
    expect(await api.json('get', requestUrl)).toMatchObject({ buildCommitHash: strict ? targetMerge : initialMerge });
    await page.reload();

    if (strict) {
      await expect(requiredJob).toBeVisible();
      await expect(merge).toBeHidden();
      const newBuildId = await runRequiredBuild(page, api, requestUrl, targetMerge);
      expect(newBuildId).not.toBe(initialBuildId);
    } else {
      expect(await api.json('get', `${requestUrl}/current-builds`)).toEqual([
        expect.objectContaining({ id: initialBuildId, commitHash: initialMerge, status: 'SUCCESSFUL' }),
      ]);
    }
    await expect(merge).toBeVisible();

    const { commitHash: newSource } = await api.putFile(project, 'follow-up.txt', 'Source-only change\n', 'Add follow-up', sourceBranch);
    const { mergeCommitHash: sourceMerge } = await waitForMergePreview(api, requestUrl, {
      targetHeadCommitHash: newTarget, headCommitHash: newSource,
    });
    expect(sourceMerge).not.toBe(targetMerge);
    expect(await api.json('get', requestUrl)).toMatchObject({ buildCommitHash: sourceMerge });
    await page.reload();
    await expect(requiredJob).toBeVisible();
    await expect(merge).toBeHidden();
  });
}
