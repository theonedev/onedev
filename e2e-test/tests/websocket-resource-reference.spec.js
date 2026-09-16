import { admin, expect, test } from './fixtures.js';
import { login } from './helpers.js';

test('the custom WebSocket resource defers pushed DOM updates until Ajax completes', async ({ page, api }, testInfo) => {
  const project = await api.createProject();
  const issue = await api.createIssue(project, { title: 'Title before the Ajax refresh' });
  const updatedTitle = 'Newer title delivered by WebSocket';
  const pageErrors = [];
  page.on('pageerror', error => pageErrors.push(error.message));

  await login(page, admin.name, admin.password);
  await page.goto(`${project.name}/~issues`);
  const row = page.locator('tr.issue');
  await expect(row).toContainText(issue.title);
  const script = page.locator('script[src*="wicket-websocket-jquery"]');
  await expect(script).toHaveCount(1);
  const scriptUrl = await script.getAttribute('src');
  await page.waitForFunction(() => window.Wicket?.WebSocket?.INSTANCE?.ws?.readyState === WebSocket.OPEN);
  await page.waitForFunction(() => onedev.server.ajaxRequests.count === 0);
  await page.evaluate(() => {
    window.socketRace = { updates: [], applied: [] };
    window.originalSocket = Wicket.WebSocket.INSTANCE.ws;
    originalSocket.addEventListener('message', event => {
      if (typeof event.data === 'string' && event.data.includes('<ajax-response>')) {
        socketRace.updates.push({ xml: event.data, ajaxCount: onedev.server.ajaxRequests.count });
      }
    });
    $(document).on('afterElementReplace.e2e', () => {
      socketRace.applied.push({
        title: document.querySelector('tr.issue')?.textContent,
        ajaxCount: onedev.server.ajaxRequests.count,
      });
    });
  });

  let releaseAjax;
  const ajaxGate = new Promise(resolve => { releaseAjax = resolve; });
  let intercepted = false;
  let ajaxBody;
  const holdAjaxResponse = async route => {
    if (intercepted || route.request().headers()['wicket-ajax'] !== 'true') {
      await route.continue();
      return;
    }
    intercepted = true;
    // Finish processing on the server before holding the response. Otherwise
    // Wicket's page lock could prevent the WebSocket push from being generated.
    const response = await route.fetch();
    ajaxBody = await response.text();
    await ajaxGate;
    await route.fulfill({ response });
  };
  await page.route('**/*', holdAjaxResponse);
  try {
    // Refresh the list without changing its query, retaining the old title in
    // the Ajax response while an independent REST request changes the issue.
    await page.locator('.issue-list button[data-tippy-content="Query"]').click();
    await expect.poll(() => ajaxBody).toContain(issue.title);
    expect(ajaxBody).toContain('<ajax-response>');
    expect(await page.evaluate(() => onedev.server.ajaxRequests.count)).toBeGreaterThan(0);

    const response = await api.request.post(`~api/issues/${issue.id}/title`, {
      data: JSON.stringify(updatedTitle), headers: { 'Content-Type': 'application/json' },
    });
    expect(response.ok(), await response.text()).toBeTruthy();
    await expect.poll(() => page.evaluate(title => socketRace.updates.some(update =>
      update.xml.includes(title) && update.ajaxCount > 0), updatedTitle)).toBe(true);

    // This observation window intentionally spans several 100-ms retry ticks:
    // receiving a push must not replace the DOM while Ajax is still pending.
    await page.waitForTimeout(350);
    await expect(row).toContainText(issue.title);
    expect(await page.evaluate(() => onedev.server.ajaxRequests.count)).toBeGreaterThan(0);
    expect(await page.evaluate(title => socketRace.applied.some(update =>
      update.title?.includes(title)), updatedTitle)).toBe(false);

    releaseAjax();
    await page.waitForFunction(() => onedev.server.ajaxRequests.count === 0);
    await expect(row).toContainText(updatedTitle);
    await expect.poll(() => page.evaluate(title => socketRace.applied.some(update =>
      update.title?.includes(title) && update.ajaxCount === 0), updatedTitle)).toBe(true);
    expect(await page.evaluate(() => Wicket.WebSocket.INSTANCE.ws === originalSocket)).toBe(true);
    expect(pageErrors).toEqual([]);

    // The reference must select OneDev's adjacent script, not Wicket's copy
    // under res/js. The race above also checks that the selected script runs.
    expect(new URL(scriptUrl, page.url()).pathname).not.toContain('/res/js/');
  } finally {
    releaseAjax();
    await page.unrouteAll({ behavior: 'wait' });
    await testInfo.attach('websocket-ajax-race', {
      body: JSON.stringify({ scriptUrl, ajaxBody, pageErrors, ...await page.evaluate(() => socketRace) }, null, 2),
      contentType: 'application/json',
    });
  }
});
