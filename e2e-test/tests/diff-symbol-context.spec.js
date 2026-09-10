import { expect, test, admin } from './fixtures.js';
import { login } from './helpers.js';

async function waitForContext(page, url, text, side) {
  await page.goto(url);
  // Indexing completion should populate context on the current page without a reload.
  await expect(page.locator(side ? `.diff-symbol-hunk .${side}` : '.diff-symbol-hunk')
    .filter({ hasText: text }).first()).toBeVisible({ timeout: 60000 });
}

for (const mode of ['UNIFIED', 'SPLIT']) {
test(`shows independent indexed contexts in ${mode.toLowerCase()} view`, async ({ page, api, baseURL }) => {
  await page.context().addCookies([{ name: 'onedev.server.diff.viewmode', value: mode, url: baseURL }]);
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  const project = await api.createProject();
  const source = (name, value) => [
    'class AccountService {',
    `  void ${name}() {`,
    ...Array.from({ length: 60 }, (_, i) => `    check(${i === 4 ? value : i});`),
    '  }',
    '  void resetPassword() {',
    ...Array.from({ length: 60 }, (_, i) => `    reset(${i === 40 ? value : i});`),
    '  }',
    '}',
  ].join('\n');
  const first = await api.putFile(project, 'AccountService.java', source('validateNewAccount', 500));
  await login(page, admin.name, admin.password);
  // Ensure the old revision has been indexed before replacing it on the branch.
  await waitForContext(page, `${project.name}/~commits/${first.commitHash}`, 'AccountService');
  await expect(page.locator('.diff-symbol-sticky .old')).toBeHidden();
  await expect(page.locator('.diff-symbol-hunk .old')).toBeHidden();
  await expect(page.locator('.diff-symbol-sticky')).toBeHidden();
  await expect(page.locator('.diff-symbol-hunk .new')).toContainText('AccountService');
  const second = await api.putFile(project, 'AccountService.java', source('validateImportedAccount', 999));
  const url = `${project.name}/~commits/${second.commitHash}`;
  await waitForContext(page, url, 'AccountService', 'common');
  const diff = page.locator('.blob-text-diff');
  await expect(diff.locator('.diff-symbol-hunk').first().locator('.symbol-context-label')).toHaveCount(1);
  await expect(diff.locator('.diff-symbol-hunk .common .symbol-context-name').first()).toHaveText('AccountService');
  await expect(diff.locator('.diff-symbol-hunk .symbol-context-side')).toHaveCount(0);
  await expect(diff.locator('.diff-symbol-hunk').last()).toContainText('resetPassword');
  await expect(diff.locator('.diff-symbol-sticky')).toBeHidden();
  await page.screenshot({ path: `test-results/diff-symbol-initial-${mode}.png`, fullPage: true });

  // Expand across method boundaries, retaining syntax highlighting and both revisions.
  await diff.locator('.expander-controls .expand-down').first().click();
  await expect(diff.locator('tr.code.expanded')).not.toHaveCount(0);
  await expect(diff.locator('tr.code.syntaxHighlighted')).not.toHaveCount(0);
  // The hunk header supplies context until it scrolls behind the file header.
  const scrollHeader = async offset => {
    await diff.locator('.diff-symbol-hunk').first().evaluate((header, offset) => {
      const $ = window.jQuery;
      const parent = $(header).scrollParent();
      const head = $(header).closest('.blob-diff').children('.head')[0];
      parent.scrollTop(parent.scrollTop() + header.getBoundingClientRect().bottom
        - head.getBoundingClientRect().bottom + offset);
    }, offset);
  };
  await scrollHeader(-1);
  await expect(diff.locator('.diff-symbol-sticky')).toBeHidden();
  await scrollHeader(1);
  await expect(diff.locator('.diff-symbol-sticky')).toBeVisible();
  await scrollHeader(-1);
  await expect(diff.locator('.diff-symbol-sticky')).toBeHidden();
  await diff.locator('td.content[data-new="10"]').last().evaluate(cell => {
    const $ = window.jQuery;
    const parent = $(cell).scrollParent();
    const head = $(cell).closest('.blob-diff').children('.head')[0];
    parent.scrollTop(parent.scrollTop() + cell.getBoundingClientRect().top
      - head.getBoundingClientRect().bottom + 1);
  });
  await expect(diff.locator('.diff-symbol-sticky')).toBeVisible();
  await expect(diff.locator('.diff-symbol-sticky .old')).toContainText('validateNewAccount');
  await expect(diff.locator('.diff-symbol-sticky .new')).toContainText('validateImportedAccount');

  if (mode === 'SPLIT') {
    await expect.poll(() => diff.evaluate(element => {
      const row = element.querySelector('tr.code');
      return Math.abs(element.querySelector('.diff-symbol-sticky .new').getBoundingClientRect().left
        - row.children[row.children.length / 2].getBoundingClientRect().left);
    })).toBeLessThan(2);
  }
  await page.screenshot({ path: `test-results/diff-symbol-context-${mode}.png`, fullPage: true });
  // Reveal enough trailing context to scroll the second method to the top.
  await diff.locator('tr.expander').last().locator('.expand-down').click();
  await expect(diff.locator('td.content[data-new="125"]').last()).toBeAttached();
  await diff.locator('td.content[data-new="105"]').last().evaluate(cell => {
    const $ = window.jQuery;
    const parent = $(cell).scrollParent();
    const sticky = $(cell).closest('.blob-text-diff').children('.diff-symbol-sticky');
    parent.scrollTop(parent.scrollTop() + cell.getBoundingClientRect().top
      - sticky[0].getBoundingClientRect().bottom);
  });
  await expect(diff.locator('.diff-symbol-sticky .old')).toContainText('resetPassword');
  await expect(diff.locator('.diff-symbol-sticky .new')).toBeHidden();
  await expect(diff.locator('.diff-symbol-sticky .symbol-context-side:visible')).toHaveCount(0);
  await page.screenshot({ path: `test-results/diff-symbol-scrolled-${mode}.png`, fullPage: true });
  await page.evaluate(() => document.documentElement.classList.add('dark-mode'));
  await page.screenshot({ path: `test-results/diff-symbol-dark-${mode}.png`, fullPage: true });
  await page.evaluate(() => document.documentElement.classList.remove('dark-mode'));
  // Switching modes replaces the panel through Ajax and must reinstall its context.
  await page.locator('[data-tippy-content="Diff options"]').click();
  await page.getByText(mode === 'UNIFIED' ? 'Split view' : 'Unified view', { exact: true }).click();
  await expect(diff.locator('.diff-symbol-sticky')).toHaveCount(1);
  await expect(diff.locator('.diff-symbol-sticky')).toHaveClass(mode === 'UNIFIED' ? /split/ : /^(?!.*split).*diff-symbol-sticky/);
  await expect(diff.locator('.diff-symbol-hunk .common .symbol-context-name').first()).toHaveText('AccountService');
  const deletion = await api.json('post', `~api/repositories/${project.id}/files/main/AccountService.java`, {
    data: { '@type': 'FileDeleteRequest', commitMessage: 'Delete context fixture' },
  });
  await waitForContext(page, `${project.name}/~commits/${deletion.commitHash}`, 'AccountService');
  await expect(diff.locator('.diff-symbol-sticky .new')).toBeHidden();
  await expect(diff.locator('.diff-symbol-hunk .new')).toBeHidden();
  await expect(diff.locator('.diff-symbol-sticky')).toBeHidden();
  await expect(diff.locator('.diff-symbol-hunk .old')).toContainText('AccountService');
  expect(errors).toEqual([]);
});
}

test('shows enclosing JavaScript object methods and prototype assignments', async ({ page, api }) => {
  const project = await api.createProject();
  const source = [
    'var onedev = {};',
    'String.prototype.escape = function() {',
    '  return this;',
    '};',
    'onedev.server = {',
    '  form: {',
    '    markDirty: function(form) {',
    ...Array.from({ length: 60 }, (_, i) => `      form.check(${i});`),
    '    },',
    '    markClean() {',
    '      return true;',
    '    }',
    '  }',
    '};',
  ].join('\n');
  const first = await api.putFile(project, 'test.js', source);
  await login(page, admin.name, admin.password);
  await waitForContext(page, `${project.name}/~commits/${first.commitHash}`, 'onedev');
  const second = await api.putFile(project, 'test.js', source.replace('form.check(30)', 'form.check(999)'));
  await waitForContext(page, `${project.name}/~commits/${second.commitHash}`, 'markDirty');
  const diff = page.locator('.blob-text-diff');
  await expect(diff.locator('.diff-symbol-hunk .common')).toHaveText('onedev > server > form > markDirty');
  await expect(diff.locator('.diff-symbol-sticky')).toBeHidden();
});

for (const mode of ['UNIFIED', 'SPLIT']) {
  test(`anchors expanded insertion and deletion contexts to displayed lines in ${mode.toLowerCase()} view`, async ({ page, api, baseURL }) => {
    await page.context().addCookies([{ name: 'onedev.server.diff.viewmode', value: mode, url: baseURL }]);
    const project = await api.createProject();
    const source = [
      'class Service {',
      ...Array.from({ length: 80 }, (_, i) => `  // Class context ${i}`),
      '  void start() {',
      ...Array.from({ length: 60 }, (_, i) => `    check(${i});`),
      '  }',
      '}',
    ].join('\n');
    const first = await api.putFile(project, 'Service.java', source);
    await login(page, admin.name, admin.password);
    await waitForContext(page, `${project.name}/~commits/${first.commitHash}`, 'Service', 'new');
    for (const content of [source.replace('    check(30);\n    check(31);\n', ''), source]) {
      const commit = await api.putFile(project, 'Service.java', content);
      await waitForContext(page, `${project.name}/~commits/${commit.commitHash}`, 'Service > start', 'common');
      const diff = page.locator('.blob-text-diff');
      await expect(diff.locator('.diff-symbol-hunk .symbol-context-label')).toHaveText('Service > start');

      await diff.locator('tr.expander .expand-up').first().click();
      await expect(diff.locator('tr.code.expanded')).not.toHaveCount(0);
      await expect(diff.locator('.diff-symbol-hunk .symbol-context-label')).toHaveText('Service');
    }
  });

  test(`hides empty context and restores it on scroll in ${mode.toLowerCase()} view`, async ({ page, api, baseURL }) => {
    await page.context().addCookies([{ name: 'onedev.server.diff.viewmode', value: mode, url: baseURL }]);
    const project = await api.createProject();
    const source = [
      ...Array.from({ length: 70 }, (_, i) => `// Before ${i}`),
      'function run() {',
      ...Array.from({ length: 70 }, (_, i) => `  check(${i});`),
      '}',
      ...Array.from({ length: 100 }, (_, i) => `// After ${i}`),
    ].join('\n');
    const first = await api.putFile(project, 'context.js', source);
    await login(page, admin.name, admin.password);
    const url = `${project.name}/~commits/${first.commitHash}`;
    // Wait for indexing without requiring a hunk header: this whole-file hunk
    // starts outside a symbol and should not have one.
    await expect(async () => {
      await page.goto(url);
      await expect(page.locator('.diff-symbol-sticky')).toBeAttached({ timeout: 2000 });
    }).toPass({ timeout: 60000, intervals: [1000] });
    const diff = page.locator('.blob-text-diff');
    const sticky = diff.locator('.diff-symbol-sticky');
    await expect(sticky).toBeHidden();
    await expect(diff.locator('.diff-symbol-hunk')).toHaveCount(0);

    const scrollToLine = async line => {
      await diff.locator(`td.content[data-new="${line}"]`).last().evaluate(cell => {
        const $ = window.jQuery;
        const parent = $(cell).scrollParent();
        const head = $(cell).closest('.blob-diff').children('.head');
        const top = head[0].getBoundingClientRect().bottom;
        // Scroll one pixel past the boundary to avoid a fractional rem-sized
        // remainder of the preceding line at the top of the viewport.
        parent.scrollTop(parent.scrollTop() + cell.getBoundingClientRect().top - top + 1);
      });
    };
    await scrollToLine(90);
    await expect(sticky).toBeVisible();
    await expect(sticky.locator('.old')).toBeHidden();
    await expect(sticky.locator('.new')).toContainText('run');
    if (mode === 'SPLIT') {
      await expect.poll(() => diff.evaluate(element => {
        const row = element.querySelector('tr.code');
        return Math.abs(element.querySelector('.diff-symbol-sticky .new').getBoundingClientRect().left
          - row.children[row.children.length / 2].getBoundingClientRect().left);
      })).toBeLessThan(2);
    }
    await scrollToLine(170);
    await expect(sticky).toBeHidden();
    await expect(sticky).toHaveJSProperty('offsetHeight', 0);
    await scrollToLine(90);
    await expect(sticky).toBeVisible();
    await expect(sticky.locator('.new')).toContainText('run');

    // Scope boundaries must settle without the row repeatedly hiding and showing
    // as its height changes which source line is visible.
    for (const [line, visible] of [[142, false], [70, true]]) {
      await scrollToLine(line);
      await expect(sticky).toBeVisible({ visible });
      const states = await sticky.evaluate(async element => {
        const states = [];
        for (let i = 0; i < 12; i++) {
          await new Promise(requestAnimationFrame);
          states.push(element.offsetHeight > 0);
        }
        return states;
      });
      expect(states).toEqual(Array(12).fill(visible));
    }

    const second = await api.putFile(project, 'context.js', source
      .replace('// Before 4', '// Updated before 4')
      .replace('check(30)', 'check(999)'));
    await waitForContext(page, `${project.name}/~commits/${second.commitHash}`, 'run');
    // Only the in-function hunk has context. The unscoped change gets no row.
    await expect(diff.locator('.diff-symbol-hunk')).toHaveCount(1);
    await expect(diff.locator('.diff-symbol-hunk .symbol-context-label')).toHaveText('run');
  });
}
