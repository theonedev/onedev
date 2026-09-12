import { admin, expect, test } from './fixtures.js';
import { login } from './helpers.js';

async function checkScrolledModal(page, testInfo, dark) {
  await login(page, admin.name, admin.password);
  await page.goto('~administration/settings/issue-fields');
  await page.evaluate(dark => $('html').toggleClass('dark-mode', dark), dark);
  await page.locator('.issue-fields').getByRole('link', { name: 'Type', exact: true }).click();
  const modal = page.locator('.modal.show');
  const selection = modal.locator('.property-defaultValueProvider .select2-selection--single');
  await expect(selection).toHaveText('New Feature');
  await selection.scrollIntoViewIfNeeded();
  await selection.evaluate(element => {
    element.closest('.modal').scrollTop += element.getBoundingClientRect().top - window.innerHeight * 0.4;
  });
  const scrollTop = await modal.evaluate(element => element.scrollTop);
  expect(scrollTop).toBeGreaterThan(100);
  await modal.evaluate(element => {
    element.scrollPositions = [];
    element.addEventListener('scroll', () => element.scrollPositions.push(element.scrollTop));
  });
  await selection.click();
  const dropdown = modal.locator('.select2-dropdown');
  await expect(dropdown).toBeInViewport();
  await expect(selection).toBeInViewport();
  await expect(dropdown.getByRole('searchbox')).toBeFocused();
  expect(await modal.evaluate(element => element.scrollTop)).toBeCloseTo(scrollTop, 0);
  const controlBounds = await selection.boundingBox();
  const dropdownBounds = await dropdown.boundingBox();
  expect(Math.min(
    Math.abs(dropdownBounds.y - controlBounds.y - controlBounds.height),
    Math.abs(dropdownBounds.y + dropdownBounds.height - controlBounds.y),
  )).toBeLessThan(0.1);
  expect(dropdownBounds.x).toBeCloseTo(controlBounds.x, 1);
  expect(dropdownBounds.width).toBeCloseTo(controlBounds.width, 1);
  await expect(dropdown.getByRole('option', { name: 'Bug', exact: true })).toBeVisible();
  const scrollPositions = await modal.evaluate(element => element.scrollPositions);
  for (const position of scrollPositions)
    expect(position).toBeCloseTo(scrollTop, 0);
  await page.screenshot({ path: testInfo.outputPath('scrolled-modal-dropdown.png') });
  await dropdown.getByRole('option', { name: 'Bug', exact: true }).click();
  await expect(selection).toHaveText('Bug');

  await selection.click();
  const contentBounds = await modal.locator('.modal-content').boundingBox();
  await page.mouse.move(contentBounds.x + contentBounds.width - 15, 300);
  await page.mouse.wheel(0, -250);
  await expect.poll(() => modal.evaluate(element => element.scrollTop)).toBeLessThan(scrollTop - 50);
  await expect(dropdown).toHaveCount(0);
  await modal.evaluate(element => { element.scrollTop = 0; });
  await expect.poll(() => modal.evaluate(element => element.scrollTop)).toBe(0);
  // Discard the unsaved selection; do not modify the global field definition.
  page.on('dialog', dialog => dialog.accept());
  await modal.getByRole('link', { name: 'Cancel', exact: true }).click();
  await expect(modal).toHaveCount(0);
}

for (const viewport of [{ width: 1280, height: 720 }, { width: 1855, height: 929 }]) {
  const dark = viewport.width === 1855;
  test(`issue field dropdown stays aligned and allows modal scrolling at ${viewport.width}x${viewport.height} in ${dark ? 'dark' : 'light'} mode`, async ({ page }, testInfo) => {
    await page.setViewportSize(viewport);
    await checkScrolledModal(page, testInfo, dark);
  });
}
