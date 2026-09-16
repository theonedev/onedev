import { admin, expect, test } from './fixtures.js';
import { login } from './helpers.js';

test.afterEach(async ({ page }, testInfo) => {
  if (testInfo.status !== testInfo.expectedStatus && !page.isClosed()) {
    await testInfo.attach('websocket-updates', {
      body: JSON.stringify(await page.evaluate(() => ({
        updates: window.socketUpdates, replacements: window.replacements,
      })), null, 2),
      contentType: 'application/json',
    });
  }
});

async function watchWebSocketUpdates(page) {
  await page.waitForFunction(() => window.Wicket?.WebSocket?.INSTANCE?.ws?.readyState === WebSocket.OPEN);
  await page.waitForFunction(() => onedev.server.ajaxRequests.count === 0);
  await page.evaluate(() => {
    window.socketUpdates = [];
    window.observedSocket = Wicket.WebSocket.INSTANCE.ws;
    observedSocket.addEventListener('message', event => {
      if (typeof event.data === 'string' && event.data.includes('<ajax-response>'))
        socketUpdates.push(event.data);
    });
  });
}

async function recordReplacements(page, component) {
  const id = await component.getAttribute('id');
  expect(id).toBeTruthy();
  await page.evaluate(id => {
    window.replacements = [];
    let previous;
    $(document).on('beforeElementReplace.e2e afterElementReplace.e2e', (event, componentId) => {
      if (componentId !== id) return;
      const element = document.getElementById(id);
      if (event.type === 'beforeElementReplace') previous = element;
      replacements.push({
        type: event.type,
        text: element?.textContent.trim(),
        connected: element?.isConnected === true,
        replaced: previous != null && previous !== element && !previous.isConnected,
      });
    });
  }, id);
}

async function postIssueChange(api, issue, path, data) {
  const response = await api.request.post(`~api/issues/${issue.id}/${path}`, {
    data: JSON.stringify(data), headers: { 'Content-Type': 'application/json' },
  });
  expect(response.ok(), await response.text()).toBeTruthy();
}

test('WebSocket replacements fire lifecycle events around the actual DOM change', async ({ page, api }) => {
  const project = await api.createProject();
  const issue = await api.createIssue(project, { title: 'Original WebSocket title' });
  await login(page, admin.name, admin.password);
  await page.goto(`${project.name}/~issues`);
  const row = page.locator('tr.issue');
  await expect(row).toContainText(issue.title);
  await watchWebSocketUpdates(page);
  await recordReplacements(page, row);

  // REST changes the issue independently of this browser. No Ajax response or
  // page reload can supply the replacement events tested here.
  const updatedTitle = 'Updated through a WebSocket push';
  await postIssueChange(api, issue, 'title', updatedTitle);
  await expect(row).toContainText(updatedTitle);
  await expect.poll(() => page.evaluate(() => socketUpdates.some(xml => xml.includes('Updated through a WebSocket push'))))
    .toBe(true);
  await expect.poll(() => page.evaluate(title => replacements.some(event =>
    event.type === 'afterElementReplace' && event.text.includes(title)), updatedTitle)).toBe(true);
  const events = await page.evaluate(() => replacements);
  const after = events.findIndex(event => event.type === 'afterElementReplace' && event.text.includes(updatedTitle));
  expect(after).toBeGreaterThan(0);
  expect(events[after - 1]).toMatchObject({
    type: 'beforeElementReplace', text: expect.stringContaining(issue.title), connected: true, replaced: false,
  });
  expect(events[after]).toMatchObject({ connected: true, replaced: true });
});

test('WebSocket replacements subscribe newly rendered linked-issue badges', async ({ page, api }) => {
  const project = await api.createProject();
  const issue = await api.createIssue(project);
  const linkedIssue = await api.createIssue(project);
  await login(page, admin.name, admin.password);
  await page.goto(issue.url);
  await expect(page.locator('.issue-editable-title')).toContainText(issue.title);
  await expect(page.locator('.linked-issues .issue-state')).toHaveCount(0);
  await watchWebSocketUpdates(page);

  await api.json('get', '~api/tod/link-issues', { params: {
    currentProject: project.name,
    sourceReference: `#${issue.url.split('/').at(-1)}`,
    targetReference: `#${linkedIssue.url.split('/').at(-1)}`,
    linkName: 'Related',
  } });
  // The REST link endpoint stores the link without an issue-change event.
  // Change the parent to render the new link and its observer via WebSocket.
  await postIssueChange(api, issue, 'title', 'Parent with a newly linked issue');
  const badge = page.locator('.linked-issues .issue-state');
  await expect(badge).toHaveText('Open');
  await expect(page.locator('.linked-issues')).toContainText(linkedIssue.title);

  // Only the linked issue changes now. Without WebSocketRequestHandler's
  // observe(page), this page is still subscribed solely to the original issue.
  await postIssueChange(api, linkedIssue, 'state-transitions', { state: 'Closed' });
  await expect(badge).toHaveText('Closed');
  await postIssueChange(api, linkedIssue, 'state-transitions', { state: 'Open' });
  await expect(badge).toHaveText('Open');
  expect(await page.evaluate(() => Wicket.WebSocket.INSTANCE.ws === observedSocket)).toBe(true);
  expect(await page.evaluate(() => socketUpdates.some(xml => xml.includes('Closed')))).toBe(true);
});

test('WebSocket updates include pending session feedback', async ({ page, api }) => {
  const project = await api.createProject();
  const issue = await api.createIssue(project);
  await login(page, admin.name, admin.password);
  await page.goto(`${project.name}/~issues`);
  await expect(page.locator('tr.issue')).toContainText(issue.title);
  await watchWebSocketUpdates(page);
  await expect(page.locator('#session-feedback')).not.toContainText('New iteration created');

  const formPage = await page.context().newPage();
  try {
    await formPage.goto(`${project.name}/~iterations/new`);
    await formPage.locator('.property-name input').fill('WebSocket feedback iteration');
    const submission = await formPage.locator('form.leave-confirm').evaluate(form => ({
      url: form.action,
      fields: Object.fromEntries(new FormData(form)),
    }));
    // Use the browser's session and the real form, but stop at the redirect so
    // the destination page cannot consume the success message first.
    const response = await page.context().request.post(submission.url, {
      form: submission.fields, maxRedirects: 0,
    });
    const body = await response.text();
    expect(response.status(), body).toBe(200);
    // OneDev preserves URL fragments with a JavaScript redirect response.
    expect(body).toContain('window.location.replace(redirect)');
    expect(body).not.toContain('New iteration created');
  } finally {
    await formPage.close();
  }

  await postIssueChange(api, issue, 'title', 'Issue updated with pending feedback');
  await expect(page.locator('tr.issue')).toContainText('Issue updated with pending feedback');
  await expect(page.locator('#session-feedback')).toBeVisible();
  await expect(page.locator('#session-feedback')).toContainText('New iteration created');
  expect(await page.evaluate(() => socketUpdates.some(xml =>
    xml.includes('id="session-feedback"') && xml.includes('New iteration created')))).toBe(true);
});
