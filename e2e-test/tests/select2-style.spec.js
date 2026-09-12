import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { admin, expect, test } from './fixtures.js';
import { login } from './helpers.js';

const webRoot = fileURLToPath(new URL('../../server-core/src/main/java/io/onedev/server/web/', import.meta.url));
const resources = path.join(webRoot, 'component/select2/res');
const danger = 'rgb(246, 78, 96)';

for (const dark of [false, true]) {
  const theme = dark ? 'dark' : 'light';
  const background = dark ? 'rgb(35, 35, 45)' : 'rgb(255, 255, 255)';
  const foreground = dark ? 'rgb(255, 255, 255)' : 'rgb(85, 85, 85)';

  test(`${theme} theme preserves OneDev controls, buttons, dropdowns and sizing`, async ({ page }) => {
    await page.goto('~login');
    await page.addStyleTag({ path: path.join(resources, 'select2.css') });
    await page.addStyleTag({ path: path.join(resources, 'select2-bootstrap.css') });
    await page.addScriptTag({ path: path.join(webRoot, 'asset/jqueryui/jquery-ui.min.js') });
    await page.addScriptTag({ path: path.join(resources, 'select2.js') });
    await page.addScriptTag({ path: path.join(resources, 'select2-integration.js') });
    await page.evaluate(dark => {
      $('html').addClass('force-ordinary-style').toggleClass('dark-mode', dark);
      $('body').append(`<div id="style-fixture" style="position:fixed;top:30px;left:30px;width:440px;padding:20px;z-index:10000">
        <input class="form-control" value="Ordinary input">
        <div id="validation"><select id="styled-single" class="form-control"></select></div>
        <select id="styled-multiple" class="form-control" multiple></select>
        <select id="styled-small" class="form-control form-control-sm"></select>
        <select id="styled-large" class="form-control form-control-lg"></select>
        <div class="input-group"><select id="styled-group" class="form-control"></select></div>
        <div class="input-group"><span class="input-group-prepend">Prefix</span><select id="styled-prepend" class="form-control"></select></div>
        <select id="styled-disabled" class="form-control" disabled></select>
      </div>`);
      $('#style-fixture select').each(function() {
        onedev.server.select2.init($(this), {
          multiple: this.multiple, placeholder: 'Choose...', allowClear: true,
          data: [{ id: 'one', text: 'One' }, { id: 'two', text: 'Two' }],
        }, [{ id: 'one', text: 'One' }]);
      });
    }, dark);

    const selection = id => page.locator(`#styled-${id} + .select2 .select2-selection`);
    await expect(selection('single')).toHaveCSS('background-color', background);
    await expect(selection('single')).toHaveCSS('color', foreground);
    await expect(selection('single')).toHaveCSS('border-color', dark ? 'rgb(50, 50, 72)' : 'rgb(228, 230, 239)');
    const height = (await page.locator('#style-fixture > input').boundingBox()).height;
    expect((await selection('single').boundingBox()).height).toBeCloseTo(height, 1);
    expect((await selection('multiple').boundingBox()).height).toBeCloseTo(height, 1);
    await expect(selection('small')).toHaveCSS('height', '30px');
    await expect(selection('large')).toHaveCSS('height', '45px');
    await expect(selection('group')).toHaveCSS('border-top-left-radius', '5.88px');
    await expect(selection('prepend')).toHaveCSS('border-top-left-radius', '0px');
    await expect(selection('disabled')).toHaveCSS('background-color', dark ? 'rgb(50, 50, 72)' : 'rgb(238, 238, 238)');
    await expect(selection('single').locator('.select2-selection__clear')).toHaveCSS('color', foreground);
    const chip = selection('multiple').locator('.select2-selection__choice');
    await expect(chip).toHaveCSS('background-color', background);
    const remove = chip.getByRole('button', { name: 'Remove item', exact: true });
    await remove.hover();
    await expect(remove).toHaveCSS('color', foreground);
    await expect(remove).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)');
    await remove.focus();
    await page.mouse.move(0, 0);
    await expect(remove).toHaveCSS('color', foreground);
    await expect(remove).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)');

    await selection('single').click();
    await expect(selection('single')).toHaveCSS('border-color', dark ? 'rgb(71, 71, 97)' : 'rgb(105, 179, 255)');
    const dropdown = page.locator('.select2-dropdown');
    await expect(dropdown).toHaveCSS('background-color', background);
    await expect(dropdown.locator('.select2-search__field')).toHaveCSS('color', foreground);
    await expect(dropdown.locator('.select2-search__field')).toHaveCSS('background-color', background);
    await dropdown.locator('.select2-search__field').fill('missing');
    await expect(dropdown.locator('.select2-results__message')).toHaveCSS('background-color', dark ? 'rgb(57, 47, 40)' : 'rgb(252, 248, 227)');
    await dropdown.locator('.select2-search__field').press('Escape');

    await page.locator('#validation').evaluate(element => element.classList.add('is-invalid'));
    await selection('single').click();
    await expect(selection('single')).toHaveCSS('border-color', danger);
    await expect(dropdown).toHaveCSS('border-color', danger);
    await dropdown.locator('.select2-search__field').press('Escape');
    await page.locator('#validation').evaluate(element => element.classList.remove('is-invalid'));
    await selection('single').click();
    await expect(dropdown).not.toHaveClass(/is-invalid/);
  });

  test(`${theme} bean validation marks single and multiple choices and their dropdowns`, async ({ page, api }, testInfo) => {
    const project = await api.createProject();
    await login(page, admin.name, admin.password);
    await page.goto(`${project.name}/~settings/user-authorizations`);
    await page.locator('.user-authorizations .add-element').click();
    await page.getByRole('button', { name: 'Save', exact: true }).click();
    const form = page.locator('.user-authorizations form');
    await expect(form.locator('.feedbackPanelERROR')).toHaveCount(2);
    await page.evaluate(dark => $('html').toggleClass('dark-mode', dark), dark);
    for (const type of ['single', 'multiple']) {
      const selection = form.locator(`.is-invalid .select2-selection--${type}`);
      await expect(selection).toHaveCSS('border-color', danger);
      await expect(selection).toHaveCSS('background-color', background);
      await selection.click();
      await expect(selection).toHaveCSS('border-color', danger);
      await expect(page.locator('.select2-dropdown')).toHaveCSS('border-color', danger);
      await expect(page.locator('.select2-dropdown')).toHaveCSS('background-color', background);
      await page.keyboard.press('Escape');
    }
    for (const feedback of await form.locator('.feedbackPanelERROR').all())
      await expect(feedback).toHaveCSS('color', danger);
    await page.screenshot({ path: testInfo.outputPath(`${theme}-validation.png`), fullPage: true });
  });
}
