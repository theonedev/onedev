import { credentials } from '../api.js';
import { admin, expect, test } from '../fixtures.js';
import { login } from '../helpers.js';

const descriptionAttachment = {
  name: 'initial report.txt',
  bytes: Buffer.from('Initial report\0with attachment data\n'),
};
const commentAttachment = {
  name: 'follow-up.txt',
  bytes: Buffer.from('Follow-up attachment\n'),
};

async function expectDownload(request, url, attachment, options) {
  const response = await request.get(url, options);
  await expect(response).toBeOK();
  expect(await response.body()).toEqual(attachment.bytes);
}

async function uploadInEditor(editor, { name, bytes }) {
  const dataTransfer = await editor.page().evaluateHandle(({ name, bytes }) => {
    const transfer = new DataTransfer();
    transfer.items.add(new File([new Uint8Array(bytes)], name, { type: 'text/plain' }));
    return transfer;
  }, { name, bytes: [...bytes] });
  try {
    await editor.dispatchEvent('drop', { dataTransfer });
    await expect.poll(() => editor.inputValue()).toContain(`[${name}](`);
  } finally {
    await dataTransfer.dispose();
  }
}

function multipart(data, { name, bytes }) {
  const form = new FormData();
  form.append('data', JSON.stringify(data));
  // TodResource expects a form-encoded path in each attachment's filename.
  const encodedName = new URLSearchParams({ path: name }).toString().slice('path='.length);
  form.append('attachment', new Blob([bytes], { type: 'text/plain' }), encodedName);
  return form;
}

function downloadPath(markdown) {
  const path = markdown.match(/\]\(<?(\/~downloads\/projects\/[^)>]+)>?\)/)?.[1];
  expect(path, 'saved Markdown links to a OneDev attachment').toBeDefined();
  return path;
}

test('uploads attachments while creating and commenting on an issue in the web UI', async ({ api, page }) => {
  const project = await api.createProject();
  const title = 'Issue with uploaded files';
  await login(page, admin.name, admin.password);
  await page.goto(`${project.name}/~issues/new`);
  await page.getByPlaceholder('Input title here').fill(title);
  for (const [name, value] of [['Type', 'Enhancement'], ['Priority', 'Normal']]) {
    const field = page.locator('.new-issue .form-group').filter({
      has: page.locator('label.name > span', { hasText: new RegExp(`^${name}$`) }),
    });
    await field.locator('.select2-selection').click();
    await page.getByRole('option', { name: value, exact: true }).click();
  }
  const description = page.locator('.new-issue .description textarea');
  await description.fill('Original report.\n\n');
  await uploadInEditor(description, descriptionAttachment);
  await page.getByRole('button', { name: 'Save', exact: true }).click();
  await page.waitForURL(new RegExp(`/${project.name}/~issues/\\d+`));
  await expect(page.locator('.issue-editable-title')).toContainText(title);

  const comment = page.locator('form.add-comment textarea');
  await comment.fill('Follow-up report.\n\n');
  await uploadInEditor(comment, commentAttachment);
  await page.getByRole('button', { name: 'Comment', exact: true }).click();
  await expect(page.getByRole('link', { name: commentAttachment.name, exact: true })).toBeVisible();
  await page.reload();

  for (const attachment of [descriptionAttachment, commentAttachment]) {
    const link = page.getByRole('link', { name: attachment.name, exact: true });
    await expect(link).toBeVisible();
    await expectDownload(page.request, await link.evaluate((anchor) => anchor.href), attachment);
  }
});

test('creates an issue and adds a comment with multipart attachments through TodResource', async ({ api, request }) => {
  // Downloads can start an admin session. Keep those cookies in the test-scoped client.
  const downloadOptions = { headers: credentials(admin) };
  const project = await api.createProject();
  const title = 'Issue with multipart uploads';
  const issue = await api.json('post', '~api/tod/create-issue', {
    params: { project: project.name },
    multipart: multipart({
      title,
      description: `See [original report](<attachment:${descriptionAttachment.name}>).`,
      Type: 'Enhancement',
      Priority: 'Normal',
    }, descriptionAttachment),
  });
  const { title: savedTitle, description } = await api.json('get', `~api/issues/${issue.id}`);
  expect(savedTitle).toBe(title);
  const descriptionUrl = downloadPath(description);
  expect(description).toBe(`See [original report](<${descriptionUrl}>).`);
  await expectDownload(request, descriptionUrl, descriptionAttachment, downloadOptions);

  const text = 'Follow-up report.';
  const comment = await api.json('post', '~api/tod/add-issue-comment', {
    params: { reference: issue.reference },
    multipart: multipart({ content: text }, commentAttachment),
  });
  const { content } = await api.json('get', `~api/issue-comments/${comment.id}`);
  const commentUrl = downloadPath(content);
  expect(content).toBe(`${text}\n\n[${commentAttachment.name}](${commentUrl})`);
  await expectDownload(request, commentUrl, commentAttachment, downloadOptions);
});
