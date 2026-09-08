import { expect, test as base, admin } from './fixtures.js';
import { login, logout } from './helpers.js';

// Shared project; each case resets Home so it can also run alone or on retry.
const test = base.extend({
  wikiProject: [async ({ api }, use) => {
    const project = await api.createProject();
    const user = await api.createUser();
    await api.authorizeUser(project, user);
    const putFile = (file, content) => api.putFile(project, file, content);
    await putFile('private.txt', 'password = E2E_PRIVATE_CODE_CONTENT');
    await putFile('wiki/Home.md', '# Home\n\nWelcome to the e2e wiki.');
    await putFile('wiki/Another-page.md', '# Another page\n\nOrdinary link destination.');
    await use({ ...project, reader: { name: user.userName, password: user.password }, putFile });
  }, { scope: 'worker' }],
});

test.describe.configure({ mode: 'default' });

const body = (page) => page.locator('.wiki-content .markdown-rendered');
const editor = (page) => page.locator('.wiki .markdown-editor textarea');

async function editHome(page, wikiProject, markdown) {
  await page.goto(`${wikiProject.name}/~wiki/main/Home?edit=true`);
  await editor(page).fill(markdown);
  await page.getByRole('button', { name: 'Save page', exact: true }).click();
  await expect(body(page)).toBeVisible();
}

test.beforeEach(async ({ page, wikiProject }) => {
  await wikiProject.putFile('wiki/Home.md', '# Home\n\nWelcome to the e2e wiki.');
  await login(page, admin.name, admin.password);
});

test('uploads an image and displays it in the wiki', async ({ page, wikiProject }) => {
  await page.goto(`${wikiProject.name}/~wiki/main/Home?edit=true`);
  await page.locator('.wiki .markdown-editor .do-image:visible').click();
  await page.getByRole('link', { name: 'Upload', exact: true }).click();
  const uploadForm = page.locator('form.upload-blob');
  // A real 1x1 PNG, sent through the editor's upload UI.
  await page.locator('input[type=file]').setInputFiles({
    name: 'wiki-image.png', mimeType: 'image/png',
    buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aD1sAAAAASUVORK5CYII=', 'base64'),
  });
  const preview = uploadForm.locator('.dz-preview').filter({ hasText: 'wiki-image.png' });
  // Dropzone marks completion after processing the response and updating the form.
  // Failed uploads also complete, so verify the result before committing.
  await expect(preview).toHaveClass(/\bdz-complete\b/);
  await expect(preview).not.toHaveClass(/\bdz-error\b/);
  await uploadForm.getByRole('button', { name: 'Commit & Insert', exact: true }).click();
  await expect(editor(page)).toHaveValue(/!\[.*\]\(wiki-image\.png\)/);
  await page.getByRole('button', { name: 'Save page', exact: true }).click();
  const image = body(page).locator('img');
  await expect(image).toBeVisible();
  await expect.poll(() => image.evaluate((img) => img.complete && img.naturalWidth > 0)).toBe(true);
  await page.reload();
  await expect.poll(() => image.evaluate((img) => img.complete && img.naturalWidth > 0)).toBe(true);
});

for (const wiki of [true, false]) {
  for (const destination of [null, 'assets', '']) {
    test(`uploads from ${wiki ? 'wiki' : 'Markdown'} to ${destination === null ? 'default folder' : destination || 'repository root'}`, async ({ page, wikiProject }) => {
      const directory = wiki ? 'wiki/Guides' : 'docs/Guides';
      await wikiProject.putFile(`${directory}/Upload.md`, '# Upload');
      await page.goto(wiki
        ? `${wikiProject.name}/~wiki/main/Guides/Upload?edit=true`
        : `${wikiProject.name}/~files/main/${directory}/Upload.md?mode=edit`);
      const input = page.locator('.markdown-editor textarea');
      await input.fill('');
      await page.locator('.markdown-editor .do-image:visible').click();
      await page.getByRole('link', { name: 'Upload', exact: true }).click();
      const form = page.locator('form.upload-blob');
      const folder = form.locator('.directory input');
      await expect(folder).toHaveValue(directory);
      if (destination !== null)
        await folder.fill(destination);
      const filename = `upload-${wiki}-${destination === null ? 'default' : destination || 'root'}.png`;
      await page.locator('input[type=file]').setInputFiles({
        name: filename, mimeType: 'image/png',
        buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aD1sAAAAASUVORK5CYII=', 'base64'),
      });
      const preview = form.locator('.dz-preview');
      await expect(preview).toHaveClass(/\bdz-complete\b/);
      await expect(preview).not.toHaveClass(/\bdz-error\b/);
      await form.getByRole('button', { name: 'Commit & Insert', exact: true }).click();
      const relative = destination === null ? filename : `../../${destination ? destination + '/' : ''}${filename}`;
      await expect(input).toHaveValue(`![${filename}](${relative})`);
      if (wiki)
        await page.getByRole('button', { name: 'Save page', exact: true }).click();
      else {
        await page.getByText('Save', { exact: true }).click();
        await page.getByRole('button', { name: 'Commit', exact: true }).click();
      }
      const image = page.locator('.markdown-rendered img');
      await expect.poll(() => image.evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
    });
  }
}

test('inserts relative repository file and image links from a nested wiki page', async ({ page, wikiProject }) => {
  await wikiProject.putFile('wiki/Guides/Setup.md', '# Setup');
  await wikiProject.putFile('sample image.png', Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aD1sAAAAASUVORK5CYII=', 'base64'));
  await page.goto(`${wikiProject.name}/~wiki/main/Guides/Setup?edit=true`);
  await editor(page).fill('');
  await page.locator('.wiki .markdown-editor .do-link:visible').click();
  await page.getByRole('link', { name: 'Pick Existing', exact: true }).click();
  await page.locator('.blob-selector').getByRole('link', { name: 'private.txt', exact: true }).click();
  await expect(editor(page)).toHaveValue('[private.txt](../../private.txt)');
  await page.goto(`${wikiProject.name}/~wiki/main/Guides/Setup?edit=true`);
  await editor(page).fill('');
  await page.locator('.wiki .markdown-editor .do-image:visible').click();
  await page.getByRole('link', { name: 'Pick Existing', exact: true }).click();
  await page.waitForFunction(() => onedev.server.ajaxRequests.count === 0);
  await page.locator('.blob-selector').getByRole('link', { name: 'sample image.png', exact: true }).click();
  await expect(editor(page)).toHaveValue('![sample image.png](../../sample%20image.png)');
  await page.getByRole('button', { name: 'Save page', exact: true }).click();
  const image = body(page).locator('img');
  await expect.poll(() => image.evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
});

for (const nested of [false, true]) {
  test(`inserts a wiki page link from ${nested ? 'the same subdirectory' : 'the wiki root into a subdirectory'}`, async ({ page, wikiProject }) => {
    const source = nested ? 'Guides/Link-source' : 'Home';
    await wikiProject.putFile(`wiki/${source}.md`, '# Link source');
    await wikiProject.putFile('wiki/Guides/Page-Name.md', '# Page Name\n\nNested wiki link destination.');
    await wikiProject.putFile('wiki/Guides/Other.md', '# Other');
    await page.goto(`${wikiProject.name}/~wiki/main/${source}?edit=true`);
    await editor(page).fill('');
    await page.locator('.wiki .markdown-editor .do-link:visible').click();
    await page.getByRole('link', { name: 'Pick Existing', exact: true }).click();
    const picker = page.locator('.blob-selector');
    // The picker expands the current page's parent folders automatically.
    if (!nested)
      await picker.getByRole('link', { name: 'Guides', exact: true }).click();
    await picker.getByRole('link', { name: 'Page-Name.md', exact: true }).click();
    await expect(editor(page)).toHaveValue(nested ? '[[Page Name]]' : '[[Guides/Page Name|Page Name]]');
    await page.getByRole('button', { name: 'Save page', exact: true }).click();
    const link = body(page).getByRole('link', { name: 'Page Name', exact: true });
    await expect(link).toHaveAttribute('href', /\/~wiki\/main\/Guides\/Page-Name$/);
    await expect(body(page).locator('.missing, .add-missing')).toHaveCount(0);
    await link.click();
    await expect(page).toHaveURL(/\/~wiki\/main\/Guides\/Page-Name$/);
    await expect(body(page)).toContainText('Nested wiki link destination.');
  });
}

test('creates a missing wiki page using the plus icon', async ({ page, wikiProject }) => {
  await editHome(page, wikiProject, '# Home\n\n[[Some Page]]');
  const link = body(page).getByRole('link', { name: 'Some Page', exact: true });
  await expect(link).toHaveAttribute('href', /\/~wiki\/main\/Some-Page$/);
  await expect(body(page).locator('a + .missing')).toHaveText('!!missing!!');
  await body(page).getByTitle('Add this page', { exact: true }).click();
  await expect(page.locator('.wiki .bean-editor .property-name input')).toBeVisible();
  await expect(page.locator('.wiki .bean-editor .property-name input')).toBeEditable();
  await expect(page.locator('.wiki .bean-editor .property-name input')).toHaveValue('Some-Page');
  await editor(page).fill('# Some Page\n\nCreated from a missing link.');
  await page.getByRole('button', { name: 'Save page', exact: true }).click();
  await expect(body(page)).toContainText('Created from a missing link.');
  await page.goto(`${wikiProject.name}/~wiki/main/Home`);
  await expect(body(page).locator('.missing, .add-missing')).toHaveCount(0);
  await link.click();
  await expect(body(page)).toContainText('Created from a missing link.');
});

test('resolves an ordinary Markdown link to another wiki page', async ({ page, wikiProject }) => {
  await editHome(page, wikiProject, '# Home\n\n[Another page](Another-page.md)');
  const link = body(page).getByRole('link', { name: 'Another page', exact: true });
  await expect(link).toHaveAttribute('href', /\/~wiki\/main\/Another-page$/);
  await expect(body(page).locator('.missing')).toHaveCount(0);
  await link.click();
  await expect(body(page)).toContainText('Ordinary link destination.');
});

test('jumps to sidebar headings after changing pages and within the current page', async ({ page, wikiProject }) => {
  await wikiProject.putFile('wiki/Heading-page.md',
    '# Heading page\n\n' + 'Paragraph before the target.\n\n'.repeat(60)
    + '## Target heading\n\n' + 'Paragraph after the target.\n\n'.repeat(40));
  await page.goto(`${wikiProject.name}/~wiki/main/Home`);
  await page.locator('.wiki-pages summary').click();
  const entry = page.locator('.wiki-pages li').filter({
    has: page.locator('a[data-wiki-page="Heading-page"]'),
  });
  await entry.locator('.page-expand').click();
  const headingLink = entry.getByRole('link', { name: 'Target heading', exact: true });
  const content = page.locator('.wiki-content');
  const heading = content.getByRole('heading', { name: 'Target heading', exact: true });
  for (let attempt = 0; attempt < 3; attempt++) {
    await headingLink.click();
    await expect(heading).toBeInViewport();
    await expect.poll(() => content.evaluate(el => el.scrollTop)).toBeGreaterThan(500);
    const viewer = await body(page).elementHandle();
    await entry.locator('a[data-wiki-page]').click();
    await expect.poll(() => content.evaluate(el => el.scrollTop)).toBe(0);
    await headingLink.click();
    await expect(heading).toBeInViewport();
    expect(await viewer.evaluate(el => el.isConnected)).toBe(true);
    await page.locator('.wiki-pages a[data-wiki-page="Home"]').click();
    await expect(body(page)).toContainText('Welcome to the e2e wiki.');
  }
});

test('keeps a cross-page heading in view after an image above it loads', async ({ page, wikiProject }) => {
  await wikiProject.putFile('wiki/Image-page.md',
    '# Images\n\n![Slow image](/wiki-heading-image.svg)\n\n## Procedures\n\nInstructions.');
  let releaseImage;
  const imageGate = new Promise(resolve => { releaseImage = resolve; });
  await page.route('**/wiki-heading-image.svg', async route => {
    await imageGate;
    await route.fulfill({ contentType: 'image/svg+xml', body:
      '<svg xmlns="http://www.w3.org/2000/svg" width="900" height="1600"><rect width="900" height="1600" fill="green"/></svg>' });
  });
  try {
    await page.goto(`${wikiProject.name}/~wiki/main/Home`);
    await page.locator('.wiki-pages summary').click();
    const entry = page.locator('.wiki-pages li').filter({
      has: page.locator('a[data-wiki-page="Image-page"]'),
    });
    await entry.locator('.page-expand').click();
    await entry.getByRole('link', { name: 'Procedures', exact: true }).click();
    const image = body(page).getByRole('img', { name: 'Slow image' });
    await expect(image).toHaveAttribute('src', /wiki-heading-image.svg/);
    releaseImage();
    await expect.poll(() => image.evaluate(el => el.complete && el.naturalHeight > 0)).toBe(true);
    await expect(body(page).getByRole('heading', { name: 'Procedures', exact: true })).toBeInViewport();
  } finally {
    releaseImage();
  }
});

test('renders wiki references in the ordinary Markdown file viewer', async ({ page, wikiProject }) => {
  await editHome(page, wikiProject, '# Home\n\n[[Another page]]');
  await page.goto(`${wikiProject.name}/~files/main/wiki/Home.md`);
  const markdown = page.locator('.markdown-blob-view .markdown-rendered');
  const link = markdown.getByRole('link', { name: 'Another page', exact: true });
  await expect(link).toHaveAttribute('href', /\/~wiki\/main\/Another-page$/);
  await expect(markdown.locator('.missing, .add-missing')).toHaveCount(0);
  await link.click();
  await expect(body(page)).toContainText('Ordinary link destination.');
});

test('allows project members to view wiki but denies repository API and outside files without code access', async ({ page, wikiProject }) => {
  await editHome(page, wikiProject, '# Home\n\n[Private file](../private.txt)');
  const outsideUrl = await body(page).getByRole('link', { name: 'Private file', exact: true }).getAttribute('href');
  // Positive control: the target exists and is readable by a code reader.
  await page.goto(outsideUrl);
  await expect(page.locator('.source-view')).toContainText('E2E_PRIVATE_CODE_CONTENT');
  await logout(page);
  await login(page, wikiProject.reader.name, wikiProject.reader.password);
  await page.goto(`${wikiProject.name}/~wiki/main/Home`);
  await expect(body(page)).toContainText('Home');
  const wikiFile = await page.request.get(`~api/repositories/${wikiProject.id}/files/main/wiki/Home.md`);
  // Repository API access requires code-read permission even for wiki files.
  expect(wikiFile.status()).toBe(403);
  await body(page).getByRole('link', { name: 'Private file', exact: true }).click();
  await expect(page.getByText('You are not allowed to perform this operation', { exact: true })).toBeVisible();
  await expect(page.locator('body')).not.toContainText('E2E_PRIVATE_CODE_CONTENT');
  for (const url of [
    `${wikiProject.name}/~files/main/wiki/Home.md?query=password`,
    `${wikiProject.name}/~raw/main/private.txt`,
    `~api/repositories/${wikiProject.id}/files/main/private.txt`,
  ]) {
    const response = await page.request.get(url);
    expect(response.status(), url).toBe(403);
    expect(await response.text()).not.toContain('E2E_PRIVATE_CODE_CONTENT');
  }
});

for (const deletedPage of ['Delete-me', 'Home']) {
  test(`deletes ${deletedPage} and returns home without reloading`, async ({ page, wikiProject }) => {
    await wikiProject.putFile(`wiki/${deletedPage}.md`, '# Page to delete');
    await page.goto(`${wikiProject.name}/~wiki/main/${deletedPage}`);
    await page.evaluate(() => { window.wikiDeleteMarker = true; });
    await page.locator('.wiki .head a[data-tippy-content="Delete page"]').click();
    await page.getByRole('button', { name: 'Delete page', exact: true }).click();
    await expect(page).toHaveURL(/\/~wiki\/main\/Home$/);
    if (deletedPage === 'Home') {
      await expect(page.getByText('Home page not found', { exact: true })).toBeVisible();
    } else {
      await expect(body(page)).toContainText('Welcome to the e2e wiki.');
      await expect(page.locator(`a[data-wiki-page="${deletedPage}"]`)).toHaveCount(0);
      await page.locator('.wiki-pages summary').click();
      await page.locator('a[data-wiki-page="Another-page"]').click();
      await expect(body(page)).toContainText('Ordinary link destination.');
    }
    expect(await page.evaluate(() => window.wikiDeleteMarker)).toBe(true);
  });
}

test('renames an existing wiki page while saving content', async ({ page, wikiProject }) => {
  await wikiProject.putFile('wiki/Rename-source.md', '# Original content');
  await page.goto(`${wikiProject.name}/~wiki/main/Rename-source?edit=true`);
  const name = page.locator('.wiki .bean-editor .property-name input');
  await expect(name).toHaveValue('Rename-source');
  await name.fill('Guides/Renamed page');
  await editor(page).fill('# Renamed content');
  await page.getByRole('button', { name: 'Save page', exact: true }).click();
  await expect(page).toHaveURL(/\/~wiki\/main\/Guides\/Renamed-page$/);
  await expect(body(page)).toContainText('Renamed content');
  await expect(page.locator('a[data-wiki-page="Rename-source"]')).toHaveCount(0);
  await expect(page.locator('a[data-wiki-page="Guides/Renamed-page"]')).toHaveCount(1);
  await page.reload();
  await expect(body(page)).toContainText('Renamed content');
  await expect(page.locator('a[data-wiki-page="Rename-source"]')).toHaveCount(0);
});

test('rejects renaming a wiki page to an existing name', async ({ page, wikiProject }) => {
  await page.goto(`${wikiProject.name}/~wiki/main/Another-page?edit=true`);
  await page.locator('.wiki .bean-editor .property-name input').fill('Home');
  await editor(page).fill('# Must not overwrite Home');
  await page.getByRole('button', { name: 'Save page', exact: true }).click();
  await expect(page.getByText('A page with this name already exists.', { exact: true })).toBeVisible();
  await expect(editor(page)).toHaveValue('# Must not overwrite Home');
  await page.goto(`${wikiProject.name}/~wiki/main/Home`);
  await expect(body(page)).toContainText('Welcome to the e2e wiki.');
  await page.goto(`${wikiProject.name}/~wiki/main/Another-page`);
  await expect(body(page)).toContainText('Ordinary link destination.');
});

for (const entry of ['empty repository', 'missing home']) {
  test(`shows an editable page name when adding Home from ${entry}`, async ({ page, api }) => {
    const project = await api.createProject();
    if (entry !== 'empty repository')
      await api.putFile(project, 'wiki/Guide.md', '# Guide');
    await page.goto(`${project.name}/~wiki/${entry === 'empty repository' ? '' : 'main/Home'}`);
    await page.getByRole('link', { name: 'Add home page', exact: true }).click();
    const name = page.locator('.wiki .bean-editor .property-name input');
    await expect(name).toBeVisible();
    await expect(name).toBeEditable();
    await expect(name).toHaveValue('Home');
    await name.fill('Custom home');
    await editor(page).fill('# Custom home');
    await page.getByRole('button', { name: 'Save page', exact: true }).click();
    await expect(page).toHaveURL(/\/~wiki\/main\/Custom-home$/);
    await expect(body(page)).toContainText('Custom home');
  });
}
