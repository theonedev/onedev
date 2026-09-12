import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { expect, test } from './fixtures.js';

const webRoot = fileURLToPath(new URL('../../server-core/src/main/java/io/onedev/server/web/', import.meta.url));
const resources = path.join(webRoot, 'component/select2/res');

// Exercise the actual bundled widget and OneDev integration with deterministic
// remote choices, without creating application records for each edge case.
test.beforeEach(async ({ page }) => {
  await page.goto('~login');
  // document.open() removes native document listeners; clear jQuery's cache too.
  await page.evaluate(() => $(document).off());
  await page.setContent(`
    <style>body { padding: 30px; } .select2-container { width: 400px; }</style>
    <div id="panel"><form>
      <select id="single" name="single"></select>
      <select id="multiple" name="multiple" multiple></select>
    </form></div>
  `);
  await page.addStyleTag({ path: path.join(resources, 'select2.css') });
  await page.addStyleTag({ path: path.join(resources, 'select2-bootstrap.css') });
  await page.addScriptTag({ path: path.join(webRoot, 'asset/jqueryui/jquery-ui.min.js') });
  await page.addScriptTag({ path: path.join(resources, 'select2.js') });
  await page.addScriptTag({ path: path.join(resources, 'select2-integration.js') });
});

test('single choice preserves custom metadata, escapes placeholders, and clears without extra callbacks', async ({ page }) => {
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.evaluate(() => {
    window.changeCount = 0;
    $('#single').on('change', () => window.changeCount++);
    onedev.server.select2.init($('#single'), {
      placeholder: '<Choose a user>', allowClear: true,
      templateSelection: user => $('<b>').text(user.name),
      templateResult: user => $('<b>').text(user.name),
      ajax: {
        transport: (params, success) => {
          const timer = setTimeout(() => success({
            results: [{ id: '2', text: 'Second user', name: 'Second user' }],
            pagination: { more: false },
          }), 20);
          return { abort: () => clearTimeout(timer) };
        },
      },
    }, [{ id: '1', text: 'Initial user', name: 'Initial user' }]);
  });
  await expect(page.locator('#single + .select2 .select2-selection__rendered b')).toHaveText('Initial user');
  expect(await page.evaluate(() => window.changeCount)).toBe(0);
  await page.locator('#single + .select2 .select2-selection').click();
  await page.getByRole('option', { name: 'Second user', exact: true }).click();
  await expect(page.locator('#single')).toHaveValue('2');
  await expect(page.locator('#single + .select2 .select2-selection__rendered b')).toHaveText('Second user');
  await page.getByRole('button', { name: 'Remove all items' }).click();
  await expect(page.locator('#single')).toHaveValue('');
  await expect(page.locator('.select2-selection__placeholder')).toHaveText('<Choose a user>');
  expect(await page.evaluate(() => window.changeCount)).toBe(2);
  expect(errors).toEqual([]);
});

test('multiple choice submits intact IDs in selection and drag order', async ({ page }) => {
  await page.evaluate(() => {
    window.changeCount = 0;
    $('#multiple').on('change', () => window.changeCount++);
    onedev.server.select2.init($('#multiple'), {
      multiple: true, placeholder: 'Choose...',
      templateSelection: item => $('<b>').text(item.name),
      ajax: {
        transport: (params, success) => {
          success({ results: [
            { id: 'first,;\nvalue', text: 'First', name: 'First' },
            { id: 'second', text: 'Second', name: 'Second' },
          ], pagination: { more: false } });
          return { abort() {} };
        },
      },
    }, [
      { id: 'first,;\nvalue', text: 'First', name: 'First' },
      { id: 'second', text: 'Second', name: 'Second' },
    ]);
  });
  const choices = page.locator('.select2-selection__choice');
  await expect(choices.locator('b')).toHaveText(['First', 'Second']);
  const values = () => page.evaluate(() => new FormData(document.querySelector('form')).getAll('multiple'));
  expect(await values()).toEqual(['first,;\nvalue', 'second']);

  await page.getByRole('button', { name: 'Remove item', exact: true }).first().click();
  await page.locator('.select2-search--inline textarea').fill('First');
  await page.getByRole('option', { name: 'First', exact: true }).click();
  await expect(choices.locator('b')).toHaveText(['Second', 'First']);
  expect(await values()).toEqual(['second', 'first,;\nvalue']);

  const from = await choices.nth(1).boundingBox();
  const to = await choices.nth(0).boundingBox();
  await page.mouse.move(from.x + from.width - 5, from.y + from.height / 2);
  await page.mouse.down();
  await page.mouse.move(to.x + 2, to.y + to.height / 2, { steps: 15 });
  await page.mouse.up();
  await expect(choices.locator('b')).toHaveText(['First', 'Second']);
  expect(await values()).toEqual(['first,;\nvalue', 'second']);
  expect(await page.evaluate(() => window.changeCount)).toBe(3);
});

test('remote search paginates and Wicket replacement destroys open dropdowns', async ({ page }) => {
  await page.evaluate(() => {
    window.queries = [];
    onedev.server.select2.init($('#single'), {
      placeholder: 'Choose...',
      ajax: {
        delay: 1,
        transport: (params, success) => {
          window.queries.push(params.data);
          const currentPage = params.data.page || 1;
          success({
            results: Array.from({ length: 20 }, (_, i) => ({
              id: String(currentPage * 100 + i), text: `${params.data.term || 'All'} ${currentPage}-${i}`,
            })),
            pagination: { more: currentPage < 2 },
          });
          return { abort() {} };
        },
      },
    }, []);
  });
  await page.locator('.select2-selection').click();
  await page.getByRole('searchbox').fill('Filtered');
  await expect(page.getByRole('option', { name: 'Filtered 1-0', exact: true })).toBeVisible();
  await page.locator('.select2-results__options').evaluate(element => {
    element.scrollTop = element.scrollHeight;
    element.dispatchEvent(new Event('scroll'));
  });
  await expect(page.getByRole('option', { name: 'Filtered 2-0', exact: true })).toBeAttached();
  expect(await page.evaluate(() => window.queries.some(query => query.term === 'Filtered' && query.page === 2))).toBe(true);
  await page.evaluate(() => $(document).trigger('beforeElementReplace', ['panel']));
  await expect(page.locator('.select2-dropdown')).toHaveCount(0);
  await expect(page.locator('.select2-container')).toHaveCount(0);
  await expect(page.locator('#single')).not.toHaveClass(/select2-hidden-accessible/);
});

test('dropdown search keeps focus inside a Bootstrap modal and is cleaned up on close', async ({ page }) => {
  await page.addStyleTag({ path: path.join(webRoot, 'asset/bootstrap/css/bootstrap.css') });
  await page.evaluate(() => {
    const $modal = $('<div class="modal" tabindex="-1"><div class="modal-dialog"><div class="modal-content"></div></div></div>')
      .appendTo(document.body);
    $modal.find('.modal-content').append($('#panel'));
    $modal.modal({ backdrop: false });
    onedev.server.select2.init($('#single'), {
      placeholder: 'Choose...',
      data: [{ id: 'one', text: 'One' }, { id: 'two', text: 'Two' }],
    }, []);
  });
  await page.locator('.select2-selection').click();
  const search = page.getByRole('searchbox');
  await expect(search).toBeFocused();
  await search.fill('Two');
  await expect(page.getByRole('option', { name: 'Two', exact: true })).toBeVisible();
  await search.press('Escape');
  await expect(page.locator('.select2-dropdown')).toBeHidden();
  await expect(page.locator('.modal')).toBeVisible();
  await page.locator('.select2-selection').click();
  await page.evaluate(() => $('.modal').modal('hide'));
  await expect(page.locator('.select2-dropdown')).toHaveCount(0);
  await expect(page.locator('.select2-container')).toHaveCount(0);
  // Scroll/resize after disposal must not call into the destroyed dropdown.
  await page.evaluate(() => {
    $('.modal').trigger('scroll');
    $(window).trigger('resize');
  });
});

const label = 'First <b>name & value</b>';
const escapedLabel = 'First &lt;b&gt;name &amp; value&lt;/b&gt;';
const description = '<em>Description & details</em>';
const escapedDescription = '&lt;em&gt;Description &amp; details&lt;/em&gt;';
const avatar = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

// Match the providers' payloads: HTML fields are escaped server-side, except
// string/branch names and issue/PR titles, which their formatters escape.
const formatters = [
  { name: 'branch', script: 'branch/choice/branch-choice.js', data: { name: label }, selection: label },
  { name: 'build', script: 'build/choice/build-choice.js', data: { jobName: escapedLabel, version: '1&lt;2', reference: '#12' }, selection: `${label}: 1<2 (#12)` },
  { name: 'group', script: 'groupchoice/group-choice.js', data: { name: escapedLabel }, selection: label },
  { name: 'issue', script: 'issue/choice/issue-choice.js', data: { title: label + ' 🙂', reference: '#12' }, selection: `${label} 🙂 (#12)` },
  { name: 'iteration', script: 'iteration/choice/iteration-choice.js', data: { name: escapedLabel, statusClass: 'badge-warning', statusName: 'Open' }, selection: label, result: label + 'Open', resultMarkup: '.iteration .badge-warning' },
  { name: 'pack', script: 'pack/choice/pack-choice.js', data: { reference: escapedLabel }, selection: label },
  { name: 'project', script: 'project/choice/project-choice.js', data: { path: escapedLabel, avatar }, selection: label, selectionMarkup: 'img.avatar', resultMarkup: '.project img.avatar' },
  { name: 'pullRequest', script: 'pullrequest/choice/pullrequest-choice.js', data: { title: label + ' 🙂', reference: '#12' }, selection: `${label} 🙂 (#12)` },
  { name: 'role', script: 'rolechoice/role-choice.js', data: { name: escapedLabel, description: escapedDescription }, selection: label, result: `${label} ${description}`, resultMarkup: '.text-muted' },
  { name: 'user', script: 'user/choice/user-choice.js', data: { name: escapedLabel, avatar }, selection: label, selectionMarkup: 'img.avatar', resultMarkup: '.user img.avatar' },
  { name: '', data: { name: label, description }, selection: label, result: `${label} ${description}`, resultMarkup: '.text-muted' },
  { name: '', variant: 'special string', prefix: '<$OneDevSpecialChoice$>', data: { name: label }, selection: label, selectionMarkup: 'i', resultMarkup: 'i' },
  { name: 'build', variant: 'build without version', script: 'build/choice/build-choice.js', data: { jobName: escapedLabel, reference: '#12' }, selection: `${label} (#12)` },
  { name: 'user', variant: 'user alias', script: 'user/choice/user-choice.js', data: { name: escapedLabel, avatar, alias: 'Current user' }, selection: label, result: '<<Current user>>', selectionMarkup: 'img.avatar', resultMarkup: 'i' },
];

for (const formatter of formatters) {
  test(`${formatter.variant || formatter.name || 'string'} formatter renders selections and remote results without double escaping`, async ({ page }) => {
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    if (formatter.script)
      await page.addScriptTag({ path: path.join(webRoot, 'component', formatter.script) });
    for (const multiple of [false, true]) {
      const id = multiple ? 'multiple' : 'single';
      await page.evaluate(({ formatter, multiple, id }) => {
        const callbacks = onedev.server[formatter.name ? formatter.name + 'ChoiceFormatter' : 'choiceFormatter'];
        const initial = { id: (formatter.prefix || '') + '1', text: formatter.selection, ...formatter.data };
        const remote = JSON.parse(JSON.stringify(initial).replaceAll('First', 'Second'));
        remote.id = (formatter.prefix || '') + '2';
        window.formatterCalls = { selection: 0, result: 0, escape: 0 };
        onedev.server.select2.init($('#' + id), {
          multiple, placeholder: '<Choose & continue>', allowClear: true,
          templateSelection: (data, container) => {
            window.formatterCalls.selection++;
            return callbacks.formatSelection(data, container);
          },
          templateResult: (data, container) => {
            window.formatterCalls.result++;
            return callbacks.formatResult(data, container);
          },
          escapeMarkup: markup => {
            window.formatterCalls.escape++;
            return callbacks.escapeMarkup(markup);
          },
          ajax: {
            transport: (params, success) => {
              const timer = setTimeout(() => success({ results: [remote], pagination: { more: false } }), 25);
              return { abort: () => clearTimeout(timer) };
            },
          },
        }, [initial]);
      }, { formatter, multiple, id });
      const widget = page.locator(`#${id} + .select2`);
      const rendered = widget.locator(multiple ? '.select2-selection__choice__display' : '.select2-selection__rendered');
      await expect(rendered).toHaveText(formatter.selection);
      if (formatter.data.title)
        await expect(multiple ? rendered.locator('..') : rendered).toHaveAttribute('title', formatter.data.title);
      if (formatter.selectionMarkup)
        await expect(rendered.locator(formatter.selectionMarkup)).toHaveCount(1);
      await expect(rendered.locator('b, em')).toHaveCount(0);
      await widget.locator('.select2-selection').click();
      const option = page.locator('.select2-results__option--selectable');
      await expect(option).toHaveText((formatter.result || formatter.selection).replaceAll('First', 'Second'));
      if (formatter.data.title)
        await expect(option).toHaveAttribute('title', formatter.data.title.replaceAll('First', 'Second'));
      if (formatter.resultMarkup)
        await expect(option.locator(formatter.resultMarkup)).toHaveCount(1);
      await expect(option.locator('b, em')).toHaveCount(0);
      await option.click();
      await page.locator(`#${id}`).evaluate(element => $(element).trigger('change.select2'));
      await expect(rendered).toHaveText(multiple
        ? [formatter.selection, formatter.selection.replaceAll('First', 'Second')]
        : formatter.selection.replaceAll('First', 'Second'));
      if (!multiple) {
        await widget.getByRole('button', { name: 'Remove all items' }).click();
        await expect(widget.locator('.select2-selection__placeholder')).toHaveText('<Choose & continue>');
      }
      const calls = await page.evaluate(() => window.formatterCalls);
      expect(calls.selection).toBeGreaterThan(0);
      expect(calls.result).toBeGreaterThan(0);
      expect(calls.escape).toBeGreaterThan(0);
      expect(errors).toEqual([]);
    }
  });
}

test('default escapeMarkup treats string templates as text', async ({ page }) => {
  await page.evaluate(() => {
    onedev.server.select2.init($('#single'), {
      placeholder: 'Choose...',
      templateSelection: item => item.text,
      templateResult: item => item.text,
    }, [{ id: '1', text: '<b>Plain & text</b>' }]);
  });
  await expect(page.locator('.select2-selection__rendered')).toHaveText('<b>Plain & text</b>');
  await expect(page.locator('.select2-selection__rendered b')).toHaveCount(0);
  await page.locator('.select2-selection').click();
  await expect(page.getByRole('option')).toHaveText('<b>Plain & text</b>');
  await expect(page.getByRole('option').locator('b')).toHaveCount(0);
});
