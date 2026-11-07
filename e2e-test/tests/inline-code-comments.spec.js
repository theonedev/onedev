import { expect, test, admin } from './fixtures.js';
import { login } from './helpers.js';

async function fixture(api) {
  const project = await api.createProject();
  const lines = Array.from({ length: 160 }, (_, index) => `const value${index} = ${index};`);
  const oldCommit = (await api.putFile(project, 'comments.js', `${lines.join('\n')}\n`)).commitHash;
  lines[9] = 'const reviewedValue = 42;';
  lines[10] = 'const reviewedResult = reviewedValue + 1;';
  const newCommit = (await api.putFile(project, 'comments.js', `${lines.join('\n')}\n`)).commitHash;
  const content = 'Please explain this calculation.';
  const id = await api.json('post', '~api/code-comments', { data: {
    projectId: project.id, content,
    mark: { commitHash: newCommit, path: 'comments.js', range: {
      fromRow: 9, fromColumn: 0, toRow: 10, toColumn: lines[10].length,
    } },
    compareContext: { oldCommitHash: oldCommit, newCommitHash: newCommit },
  } });
  return { project, lines, oldCommit, newCommit, id, content };
}

function thread(page) {
  return page.locator('.inline-comment-cell>.comment, .inline-comment-host>.comment, .revision-diff>.body>.comment-home>.comment');
}

async function reply(page, content) {
  await thread(page).getByRole('link', { name: 'Reply', exact: true }).click();
  await thread(page).locator('textarea:visible').fill(content);
  await thread(page).getByRole('button', { name: 'Save', exact: true }).click();
  await expect(thread(page).getByText(content, { exact: true })).toBeVisible();
}

async function checkDiffAnchor(page, side, line) {
  const row = page.locator('tr.inline-comment-row');
  await expect(row).toHaveCount(1);
  await expect.poll(() => row.evaluate((element, side) =>
    element.previousElementSibling.querySelector(`td.content[data-${side}]`)?.getAttribute(`data-${side}`), side)).toBe(String(line));
}

test('real diff comments stay inline and keep unsaved replies through diff option changes', async ({ page, api }) => {
  const data = await fixture(api);
  const errors = [];
  page.on('pageerror', error => errors.push(error.stack));
  await login(page, admin.name, admin.password);
  await page.goto(`${data.project.name}/~commits/${data.newCommit}?comment=${data.id}`);
  await expect(thread(page)).toContainText(data.content);
  await checkDiffAnchor(page, 'new', 10);
  await reply(page, 'The extra one includes the initial item.');
  await expect.poll(async () => (await api.json('get', `~api/code-comments/${data.id}/replies`)).length).toBe(1);
  await thread(page).getByRole('link', { name: 'Edit', exact: true }).first().click();
  await thread(page).locator('textarea:visible').fill('Please explain the updated calculation.');
  await thread(page).getByRole('button', { name: 'Save', exact: true }).click();
  await expect(thread(page)).toContainText('Please explain the updated calculation.');
  await checkDiffAnchor(page, 'new', 10);
  await thread(page).getByRole('link', { name: 'Set Resolved', exact: true }).click();
  await thread(page).getByRole('button', { name: 'Ok', exact: true }).click();
  await expect.poll(async () => (await api.json('get', `~api/code-comments/${data.id}`)).resolved).toBe(true);
  await page.reload();
  await checkDiffAnchor(page, 'new', 10);
  await thread(page).locator('[data-tippy-content="Hide comment"]').click();
  await expect(page.locator('tr.inline-comment-row')).toHaveCount(0);
  await page.locator('td.number .comment-trigger').first().click();
  await checkDiffAnchor(page, 'new', 10);

  await thread(page).getByRole('link', { name: 'Reply', exact: true }).click();
  const draft = 'Reply typed before changing the diff options.';
  const editor = thread(page).locator('textarea:visible');
  await editor.fill(draft);
  const openEditor = await editor.elementHandle();
  const diffOptions = page.locator('[data-tippy-content="Diff options"]');

  await diffOptions.click();
  await page.getByText(/^split view$/i).click();
  await expect(page.locator('.inline-comment-cell')).toHaveAttribute('colspan', '6');
  await checkDiffAnchor(page, 'new', 10);
  await expect(editor).toHaveValue(draft);
  expect(await editor.evaluate((element, previous) => element === previous, openEditor)).toBe(true);

  const splitDiff = await page.locator('.blob-text-diff').elementHandle();
  await diffOptions.click();
  await page.getByText(/^ignore all whitespace$/i).click();
  await expect.poll(() => splitDiff.evaluate(element => element.isConnected)).toBe(false);
  await checkDiffAnchor(page, 'new', 10);
  await expect(editor).toHaveValue(draft);
  expect(await editor.evaluate((element, previous) => element === previous, openEditor)).toBe(true);
  await thread(page).getByRole('button', { name: 'Save', exact: true }).click();
  await expect(thread(page)).toContainText(draft);

  for (const mode of ['UNIFIED', 'SPLIT']) {
    await page.context().addCookies([{ name: 'onedev.server.diff.viewmode', value: mode, url: new URL(page.url()).origin }]);
    await page.reload();
    await checkDiffAnchor(page, 'new', 10);
    await expect(page.locator('.inline-comment-cell')).toHaveAttribute('colspan', mode === 'SPLIT' ? '6' : '4');
    await expect(thread(page)).toContainText('The extra one includes the initial item.');
  }
  const oldId = await api.json('post', '~api/code-comments', { data: {
    projectId: data.project.id, content: 'Comment on the removed code.',
    mark: { commitHash: data.oldCommit, path: 'comments.js', range: {
      fromRow: 9, fromColumn: 0, toRow: 10, toColumn: 18,
    } },
    compareContext: { oldCommitHash: data.oldCommit, newCommitHash: data.newCommit },
  } });
  await page.goto(`${data.project.name}/~commits/${data.newCommit}?comment=${oldId}`);
  await checkDiffAnchor(page, 'old', 10);
  await expect(page.locator('.inline-comment-cell')).toHaveAttribute('colspan', '6');
  await thread(page).scrollIntoViewIfNeeded();
  await page.screenshot({ path: test.info().outputPath('real-diff-comment.png') });
  expect(errors).toEqual([]);
});

test('real source comments remain editable after scrolling out of view', async ({ page, api }) => {
  const data = await fixture(api);
  const errors = [];
  page.on('pageerror', error => errors.push(error.stack));
  await login(page, admin.name, admin.password);
  await page.goto(`${data.project.name}/~files/main/comments.js?comment=${data.id}`);
  await expect(page.locator('.CodeMirror-linewidget')).toContainText(data.content);
  await expect.poll(() => page.locator('.source-view').evaluate(element => {
    const state = $(element).data('inlineComment');
    return state?.widget?.line && state.widget.doc.getLineNumber(state.widget.line);
  })).toBe(10);
  await reply(page, 'Source-view reply.');
  await page.locator('.source-view>.code>.CodeMirror').evaluate(editor => editor.CodeMirror.scrollIntoView({ line: 150, ch: 0 }));
  await expect(page.locator('.source-view>.comment-home>.comment')).toBeAttached();
  await page.locator('.source-view>.code>.CodeMirror').evaluate(editor => editor.CodeMirror.scrollIntoView({ line: 10, ch: 0 }));
  await expect(page.locator('.CodeMirror-linewidget')).toContainText('Source-view reply.');
  await reply(page, 'Reply after scrolling back.');
  await page.reload();
  await expect(thread(page)).toContainText('Reply after scrolling back.');
  await page.screenshot({ path: test.info().outputPath('real-source-comment.png') });
  await thread(page).locator('[data-tippy-content="Hide comment"]').click();
  await expect(page.locator('.CodeMirror-linewidget')).toHaveCount(0);
  expect(errors).toEqual([]);
});

test('outdated comments remain readable and actionable in source and diff views', async ({ page, api }) => {
  const data = await fixture(api);
  data.lines[9] = 'const replacement = "different implementation";';
  data.lines[10] = 'const replacementResult = replacement.length;';
  await api.putFile(data.project, 'comments.js', `${data.lines.join('\n')}\n`, 'Replace the commented code');
  data.lines[20] = 'const laterChange = true;';
  const latest = (await api.putFile(data.project, 'comments.js', `${data.lines.join('\n')}\n`, 'Change unrelated code')).commitHash;
  const mapped = await api.request.get(`~api/code-comments/${data.id}/mark?commitHash=${latest}`);
  expect(mapped.status()).toBe(204);
  const errors = [];
  page.on('pageerror', error => errors.push(error.stack));
  await login(page, admin.name, admin.password);
  await page.goto(`${data.project.name}/~files/main/comments.js?comment=${data.id}`);
  await expect(thread(page)).toContainText(data.content);
  await expect(thread(page).getByText('outdated', { exact: true })).toBeVisible();
  await reply(page, 'This source comment is still useful after the rewrite.');
  await page.screenshot({ path: test.info().outputPath('outdated-source-comment.png') });
  await page.goto(`${data.project.name}/~commits/${latest}?comment=${data.id}`);
  await expect(page.locator('.revision-diff>.body>.comment-home>.comment')).toBeVisible();
  await expect(thread(page).getByText('outdated', { exact: true })).toBeVisible();
  await expect(page.locator('tr.inline-comment-row')).toHaveCount(0);
  await reply(page, 'The outdated diff discussion also remains usable.');
  await page.reload();
  await expect(thread(page)).toContainText('The outdated diff discussion also remains usable.');
  await expect(thread(page).getByText('outdated', { exact: true })).toBeVisible();
  await page.screenshot({ path: test.info().outputPath('outdated-diff-comment.png') });
  await thread(page).locator('[data-tippy-content="Hide comment"]').click();
  await expect(thread(page)).toHaveCount(0);
  expect(errors).toEqual([]);
});

async function selectText(page, locator) {
  // The source viewer may replace a line while sizing its initial viewport.
  await expect(async () => {
    await locator.scrollIntoViewIfNeeded({ timeout: 2000 });
  }).toPass({ timeout: 10000 });
  const box = await locator.boundingBox();
  await page.mouse.move(box.x + 4, box.y + box.height / 2);
  await page.mouse.down();
  await page.mouse.move(box.x + Math.min(box.width - 4, 115), box.y + box.height / 2, { steps: 8 });
  await page.mouse.up();
  await page.getByText('Add comment on this selection', { exact: true }).click();
}

test('creates real comments beneath selected source and diff lines', async ({ page, api }) => {
  const data = await fixture(api);
  const errors = [];
  page.on('pageerror', error => errors.push(error.stack));
  await login(page, admin.name, admin.password);
  await page.goto(`${data.project.name}/~files/main/comments.js`);
  await selectText(page, page.locator('.CodeMirror-code pre').filter({ hasText: /^const value5 = 5;$/ }));
  await expect(thread(page).locator('.new-comment')).toBeVisible();
  await thread(page).locator('textarea:visible').fill('Created from the source selection.');
  await thread(page).getByRole('button', { name: 'Save', exact: true }).click();
  await expect(thread(page)).toContainText('Created from the source selection.');
  await page.reload();
  await expect(thread(page)).toContainText('Created from the source selection.');
  await page.goto(`${data.project.name}/~commits/${data.newCommit}`);
  await selectText(page, page.locator('.blob-text-diff>table>tbody>tr.code>td.content[data-new="8"]').last());
  await expect(thread(page).locator('.new-comment')).toBeVisible();
  await checkDiffAnchor(page, 'new', 8);
  await thread(page).locator('textarea:visible').fill('Created from the diff selection.');
  await thread(page).getByRole('button', { name: 'Save', exact: true }).click();
  await expect(thread(page)).toContainText('Created from the diff selection.');
  await page.reload();
  await checkDiffAnchor(page, 'new', 8);
  await expect(thread(page)).toContainText('Created from the diff selection.');
  const comments = await api.json('get', `~api/code-comments?projectId=${data.project.id}&offset=0&count=100`);
  expect(comments.map(comment => comment.content)).toEqual(expect.arrayContaining([
    'Created from the source selection.', 'Created from the diff selection.',
  ]));
  expect(errors).toEqual([]);
});
