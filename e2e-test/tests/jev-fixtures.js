import http from 'node:http';
import { once } from 'node:events';
import { expect, test as base, admin } from './fixtures.js';
import { login } from './helpers.js';

const fakeApiKey = 'e2e-fake-jev-key';
const settingsUrl = '~administration/settings/jev';

async function setJevEnabled(page, value) {
  const checkbox = page.locator('input.enable');
  if (await checkbox.isChecked() !== value) {
    // OneDev styles the native checkbox as a switch with a visible label.
    await checkbox.locator('..').click();
    await expect(checkbox).toBeChecked({ checked: value });
    await expect(page.locator('input[type=password]')).toBeVisible({ visible: value });
    return true;
  }
  return false;
}

// Jev is called by Java, so page.route() cannot intercept it. Configure Jev's
// base URL through the real settings page to point at this local HTTP mock.
export const test = base.extend({
  jev: async ({ page, api }, use) => {
    const requests = [];
    const server = http.createServer(async (request, response) => {
      const chunks = [];
      for await (const chunk of request) chunks.push(chunk);
      requests.push({
        method: request.method, url: request.url, headers: request.headers,
        body: JSON.parse(Buffer.concat(chunks).toString()), response,
      });
    });
    server.listen(0, '127.0.0.1');
    await once(server, 'listening');
    const baseUrl = `http://127.0.0.1:${server.address().port}/v1`;

    let originalJev;
    let originalIssue;
    try {
      await login(page, admin.name, admin.password);
      await page.goto(settingsUrl);
      const enabled = page.locator('input.enable');
      originalJev = { enabled: await enabled.isChecked() };
      if (originalJev.enabled) {
        originalJev.baseUrl = await page.locator('.property-baseUrl input').inputValue();
        originalJev.key = await page.locator('input[type=password]').inputValue();
        originalJev.timeout = await page.locator('.property-timeoutSeconds input').inputValue();
      } else {
        await setJevEnabled(page, true);
        await expect(page.locator('.property-baseUrl input')).toHaveValue('https://api.typesafe.ai/v1');
      }
      await page.locator('.property-baseUrl input').fill(baseUrl);
      await page.locator('input[type=password]').fill(fakeApiKey);
      await page.getByRole('button', { name: 'Save Settings' }).click();
      await expect(page.getByText('Jev settings have been saved')).toBeVisible();
      await expect(page.locator('.property-baseUrl input')).toHaveValue(baseUrl);

      originalIssue = await api.json('get', '~api/settings/issue');
      const issueSetting = structuredClone(originalIssue);
      for (const name of ['Type', 'Priority']) {
        const field = issueSetting.fieldSpecs.find(field => field.name === name);
        expect(field, `Test server needs the default ${name} choice field`).toBeTruthy();
        field.allowEmpty = false;
        field.defaultValueProvider = null;
      }
      const response = await api.request.post('~api/settings/issue', { data: issueSetting });
      expect(response.ok(), await response.text()).toBeTruthy();

      await use({
        requests,
        async nextRequest(index = 0) {
          await expect.poll(() => requests.length).toBeGreaterThan(index);
          const request = requests[index];
          expect(request.method).toBe('POST');
          expect(request.url).toBe('/v1/systemone');
          expect(request.headers.authorization).toBe(`Bearer ${fakeApiKey}`);
          expect(request.headers['content-type']).toBe('application/json');
          expect(request.body.model).toBe('jev-latest');
          return request;
        },
        reply(request, selections = { Type: ['Bug', 0.99], Priority: ['Major', 0.99] }, status = 200) {
          const answers = {};
          for (const [id, question] of Object.entries(request.body.questions)) {
            expect(question.type).toBe('choice');
            const name = question.instructions.match(/issue field '([^']+)'/)[1];
            const [value, confidence] = selections[name] ?? [];
            expect(value, `Missing mock answer for ${name}`).toBeTruthy();
            const choice = value === 'unknown' ? 'unknown'
              : Object.entries(question.criteria).find(([, label]) => label === value)?.[0];
            expect(choice, `Choice ${value} should be offered`).toBeTruthy();
            answers[id] = { choice, confidence };
          }
          request.response.writeHead(status, { 'Content-Type': 'application/json' });
          request.response.end(JSON.stringify({ answers }));
        },
      });
    } finally {
      // Release any request held by a failed assertion before navigating away.
      for (const { response } of requests) {
        if (!response.writableEnded) {
          response.writeHead(503);
          response.end();
        }
      }
      try {
        if (originalIssue) {
          const response = await api.request.post('~api/settings/issue', { data: originalIssue });
          expect(response.ok(), await response.text()).toBeTruthy();
        }
      } finally {
        try {
          if (originalJev) {
            await page.goto(settingsUrl);
            const changed = await setJevEnabled(page, originalJev.enabled);
            if (originalJev.enabled) {
              await page.locator('.property-baseUrl input').fill(originalJev.baseUrl);
              await page.locator('input[type=password]').fill(originalJev.key);
              await page.locator('.property-timeoutSeconds input').fill(originalJev.timeout);
            }
            if (changed || originalJev.enabled) {
              await page.getByRole('button', { name: 'Save Settings' }).click();
              await expect(page.getByText('Jev settings have been saved')).toBeVisible();
            }
          }
        } finally {
          server.closeAllConnections();
          await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
        }
      }
    }
  },
});
