import assert from 'node:assert/strict';
import { execFile } from 'node:child_process';
import { randomInt, randomUUID } from 'node:crypto';
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import { promisify } from 'node:util';
import * as client from 'openid-client';
import { expect, test as base } from './fixtures.js';

const exec = promisify(execFile);

const test = base.extend({
  clientAuth: ['none', { option: true }],
  protocol: ['oauth2', { option: true }],
  oauth: async ({ api, page, clientAuth, protocol }, use) => {
    const metadata = await api.json('get', '.well-known/openid-configuration');
    const issuer = new URL(metadata.issuer);
    const serverURL = `${issuer.href.replace(/\/$/, '')}/`;
    const user = await api.createUser();
    const selected = await api.createProject();
    const unselected = await api.createProject();
    await api.authorizeUser(selected, user, 'Project Owner');
    await api.authorizeUser(unselected, user, 'Project Owner');

    // Use the advertised issuer for browser sessions as well as token requests.
    // It may use a different hostname from E2E_BASE_URL.
    await page.goto(`${serverURL}~login`);
    await page.getByRole('textbox', { name: 'Login name or email address' }).fill(user.userName);
    await page.locator('form input[type=password]').fill(user.password);
    await page.getByRole('button', { name: 'Sign in', exact: true }).click();
    await page.waitForURL(url => !url.pathname.includes('~login'));

    const callback = new URL(`http://127.0.0.1:${randomInt(49152, 65536)}/callback`);
    // Only the application's landing page is intercepted. Discovery, consent,
    // token exchange, JWKS, userinfo, REST and Git all use the real server.
    await page.route(url => url.origin === callback.origin && url.pathname === callback.pathname,
      route => route.fulfill({ contentType: 'text/html', body: '<p>Authorization callback received.</p>' }));
    await page.goto(`${serverURL}~my/oauth-applications/new`);
    const applicationName = `oauth-e2e-${randomUUID()}`;
    await page.locator('.property-name input').fill(applicationName);
    await page.locator('.property-homepage input').fill('https://example.com');
    const confidential = page.locator('.property-confidential input[type=checkbox]');
    if (await confidential.isChecked() !== (clientAuth !== 'none'))
      await confidential.locator('..').click();
    await expect(confidential).toBeChecked({ checked: clientAuth !== 'none' });
    // Exercise RFC 8252's variable loopback port for public applications.
    await page.locator('.property-redirectUris textarea').fill(clientAuth === 'none'
      ? 'http://127.0.0.1/callback' : callback.href);
    await page.getByRole('button', { name: 'Save', exact: true }).click();
    const clientIdLink = page.locator('[aria-label="Copy client ID"]');
    await expect(clientIdLink).toBeVisible();
    const clientId = (await clientIdLink.locator('..').locator('span').innerText()).trim();
    const applicationURL = page.url();
    let secret;
    if (clientAuth !== 'none') {
      const copy = page.locator('[aria-label="Copy client secret"]');
      await expect(copy).toBeVisible();
      secret = (await copy.locator('..').locator('span').innerText()).trim();
      expect(Boolean(secret) && !/^\*+$/.test(secret)).toBe(true);
    }
    await expect(page.getByRole('link', { name: 'Rotate Secret', exact: true })).toHaveCount(0);

    const authentication = clientAuth === 'none' ? client.None()
      : clientAuth === 'basic' ? client.ClientSecretBasic(secret) : client.ClientSecretPost(secret);
    const execute = [client.enableNonRepudiationChecks];
    // The E2E runner supports explicitly configured HTTP development servers.
    if (issuer.protocol === 'http:') execute.push(client.allowInsecureRequests);
    const config = await client.discovery(issuer, clientId, undefined, authentication, {
      algorithm: protocol, execute,
    });
    expect(config.serverMetadata().supportsPKCE('S256')).toBe(true);
    const roleScope = name => {
      const scope = Object.entries(config.serverMetadata().onedev_scope_descriptions)
        .find(([, description]) => description === `Role: ${name}`)?.[0];
      expect(scope, `Discovery must advertise the ${name} role`).toBeTruthy();
      return scope;
    };

    await use({
      page, config, user, selected, unselected, callback, clientId, applicationName, applicationURL, serverURL,
      roleScope,
      async authorize({ scope, deny = false } = {}) {
        const verifier = client.randomPKCECodeVerifier();
        const state = client.randomState();
        const nonce = protocol === 'oidc' ? client.randomNonce() : undefined;
        scope ??= protocol === 'oidc' ? 'openid profile email offline_access'
          : `profile ${roleScope('Code Reader')}`;
        const url = client.buildAuthorizationUrl(config, {
          redirect_uri: callback.href, scope, state,
          code_challenge: await client.calculatePKCECodeChallenge(verifier), code_challenge_method: 'S256',
          ...(nonce ? { nonce } : {}),
        });
        await page.goto(url.href);
        await expect(page.getByText(applicationName, { exact: true })).toBeVisible();
        if (scope.split(' ').some(value => value.startsWith('role:'))) {
          await page.locator('.select2-search--inline textarea').fill(selected.name);
          await page.getByRole('option', { name: selected.name, exact: true }).click();
          await expect(page.locator('.select2-selection__choice')).toContainText(selected.name);
        } else {
          await expect(page.locator('select[multiple]')).toHaveCount(0);
        }
        await page.getByRole('button', { name: deny ? 'Deny' : 'Authorize', exact: true }).click();
        await page.waitForURL(url => url.origin === callback.origin && url.pathname === callback.pathname);
        return {
          url: new URL(page.url()),
          checks: { pkceCodeVerifier: verifier, expectedState: state,
            ...(nonce ? { expectedNonce: nonce, idTokenExpected: true } : {}) },
        };
      },
      async get(token, resource) {
        // No browser cookies or admin fixture credentials can mask a token failure.
        return fetch(new URL(resource, serverURL), {
          headers: { Authorization: `Bearer ${token}` }, redirect: 'manual', signal: AbortSignal.timeout(10000),
        });
      },
    });
    // FixturesApi deletes the dedicated user, cascading to its applications,
    // grants and tokens, even when a test fails before explicit revocation.
  },
});

// These flows display a one-time secret and navigate with authorization codes.
// Do not save those credentials in Playwright traces or failure screenshots.
test.use({ trace: 'off', screenshot: 'off' });

for (const protocol of ['oauth2', 'oidc']) {
  for (const clientAuth of ['none', 'basic', 'post']) {
    test.describe(`${protocol} with ${clientAuth} client authentication`, () => {
      test.use({ protocol, clientAuth });

      test('validates consent, PKCE, access, refresh rotation and revocation', async ({ oauth }) => {
        const { config, selected, unselected, user } = oauth;
        const transaction = await oauth.authorize();
        const wrongState = new URL(transaction.url);
        wrongState.searchParams.set('state', 'incorrect-state');
        await assert.rejects(client.authorizationCodeGrant(config, wrongState, transaction.checks),
          error => error.code === 'OAUTH_INVALID_RESPONSE' && /state/.test(error.cause?.message));
        await assert.rejects(client.authorizationCodeGrant(config, transaction.url, {
          ...transaction.checks, pkceCodeVerifier: client.randomPKCECodeVerifier(),
        }), error => error.error === 'invalid_grant');
        const first = await client.authorizationCodeGrant(config, transaction.url, transaction.checks);
        expect(first.token_type.toLowerCase()).toBe('bearer');
        expect(first.expires_in).toBeGreaterThan(0);
        expect(first.expires_in).toBeLessThanOrEqual(3600);
        expect(typeof first.refresh_token).toBe('string');

        if (protocol === 'oidc') {
          expect(first.claims()).toMatchObject({
            sub: String(user.id), iss: config.serverMetadata().issuer, nonce: transaction.checks.expectedNonce,
          });
          const info = await client.fetchUserInfo(config, first.access_token, first.claims().sub);
          expect(info).toMatchObject({ preferred_username: user.userName,
            email: `${user.userName}@example.com`, email_verified: expect.any(Boolean) });
        } else {
          expect(first.id_token).toBeUndefined();
          expect((await oauth.get(first.access_token, `~api/projects/${selected.id}`)).status).toBe(200);
          expect((await oauth.get(first.access_token, `~api/projects/${unselected.id}`)).status).toBe(403);
          const projects = await (await oauth.get(first.access_token, '~api/projects?offset=0&count=100')).json();
          expect(projects.map(project => project.id)).toEqual([selected.id]);
        }

        const second = await client.refreshTokenGrant(config, first.refresh_token);
        // Boolean assertions avoid printing token values on a regression.
        expect(first.access_token !== second.access_token).toBe(true);
        expect(first.refresh_token !== second.refresh_token).toBe(true);
        if (protocol === 'oidc') {
          expect(second.claims()?.sub).toBe(first.claims().sub);
          await client.fetchUserInfo(config, second.access_token, first.claims().sub);
        } else {
          expect((await oauth.get(second.access_token, `~api/projects/${selected.id}`)).status).toBe(200);
        }
        const protectedPath = protocol === 'oidc' ? '~oauth/userinfo' : `~api/projects/${selected.id}`;
        expect((await oauth.get(first.access_token, protectedPath)).status).toBe(401);
        await client.tokenRevocation(config, second.refresh_token);
        expect((await oauth.get(second.access_token, protectedPath)).status).toBe(401);
        await assert.rejects(client.refreshTokenGrant(config, second.refresh_token),
          error => error.error === 'invalid_grant');
        await oauth.page.goto(`${oauth.serverURL}~my/authorized-applications`);
        await expect(oauth.page.getByRole('link', { name: oauth.applicationName, exact: true })).toHaveCount(0);
      });
    });
  }
}

test('denied consent returns access_denied and creates no authorization', async ({ oauth }) => {
  const transaction = await oauth.authorize({ deny: true });
  expect(transaction.url.searchParams.get('error')).toBe('access_denied');
  expect(transaction.url.searchParams.get('state')).toBe(transaction.checks.expectedState);
  expect(transaction.url.searchParams.has('code')).toBe(false);
  await oauth.page.goto(`${oauth.serverURL}~my/authorized-applications`);
  await expect(oauth.page.getByRole('link', { name: oauth.applicationName, exact: true })).toHaveCount(0);
});

test('replaying a rotated refresh token deletes the entire authorization', async ({ oauth }) => {
  const transaction = await oauth.authorize();
  const first = await client.authorizationCodeGrant(oauth.config, transaction.url, transaction.checks);
  const second = await client.refreshTokenGrant(oauth.config, first.refresh_token);
  await assert.rejects(client.refreshTokenGrant(oauth.config, first.refresh_token),
    error => error.error === 'invalid_grant');
  expect((await oauth.get(second.access_token, `~api/projects/${oauth.selected.id}`)).status).toBe(401);
  await assert.rejects(client.refreshTokenGrant(oauth.config, second.refresh_token),
    error => error.error === 'invalid_grant');
});

// Exercise Git itself with the issued credentials, without Python or an external
// credential helper. Authentication is isolated from local Git configuration.
test('Git HTTP enforces reader/writer roles, project isolation, refresh and revocation', async ({ oauth, api }) => {
  const { selected, unselected, config } = oauth;
  const content = '# OAuth Git fixture\n';
  await api.putFile(selected, 'README.md', content);
  await api.putFile(unselected, 'README.md', content);
  const selectedURL = new URL(selected.name, oauth.serverURL).href;
  const unselectedURL = new URL(unselected.name, oauth.serverURL).href;
  const directory = await mkdtemp(path.join(os.tmpdir(), 'onedev-oauth-git-'));
  const checkout = path.join(directory, 'checkout');
  const git = async (token, args) => {
    try {
      return { code: 0, ...await exec('git', ['-c', 'credential.helper=', ...args], {
        timeout: 30000,
        env: { ...process.env, GIT_TERMINAL_PROMPT: '0', GIT_CONFIG_NOSYSTEM: '1',
          GIT_CONFIG_GLOBAL: '/dev/null', GIT_CONFIG_COUNT: '1',
          GIT_CONFIG_KEY_0: 'http.extraHeader',
          GIT_CONFIG_VALUE_0: `Authorization: Basic ${Buffer.from(`oauth2:${token}`).toString('base64')}` },
      }) };
    } catch (error) {
      if (typeof error.code !== 'number') throw error;
      return { code: error.code, stdout: error.stdout, stderr: error.stderr };
    }
  };
  const success = async (token, args) => {
    const result = await git(token, args);
    expect(result.code, result.stderr).toBe(0);
    return result.stdout;
  };
  try {
    const transaction = await oauth.authorize();
    const reader = await client.authorizationCodeGrant(config, transaction.url, transaction.checks);
    await success(reader.access_token, ['ls-remote', selectedURL]);
    await success(reader.access_token, ['clone', '--branch', 'main', selectedURL, checkout]);
    expect(await readFile(path.join(checkout, 'README.md'), 'utf8')).toBe(content);
    await success(reader.access_token, ['-C', checkout, 'fetch', 'origin']);
    expect((await git(reader.access_token, ['ls-remote', unselectedURL])).stderr)
      .toContain('Project not found or inaccessible');
    await writeFile(path.join(checkout, 'oauth-test.txt'), 'OAuth write permission fixture\n');
    await success(reader.access_token, ['-C', checkout, 'add', 'oauth-test.txt']);
    await success(reader.access_token, ['-C', checkout, '-c', 'user.name=OAuth Test',
      '-c', 'user.email=oauth-test@example.com', '-c', 'commit.gpgsign=false',
      'commit', '-m', 'Test OAuth Git write permission']);
    const push = ['-C', checkout, 'push', 'origin', 'HEAD:refs/heads/oauth-check'];
    const denied = await git(reader.access_token, push);
    expect(denied.code).not.toBe(0);
    expect(denied.stderr).toContain('403');

    const refreshed = await client.refreshTokenGrant(config, reader.refresh_token);
    expect(reader.access_token !== refreshed.access_token).toBe(true);
    expect(reader.refresh_token !== refreshed.refresh_token).toBe(true);
    await success(refreshed.access_token, ['-C', checkout, 'fetch', 'origin']);
    expect((await oauth.get(reader.access_token, `~api/projects/${selected.id}`)).status).toBe(401);
    await client.tokenRevocation(config, refreshed.refresh_token);

    const writeTransaction = await oauth.authorize({ scope: `profile ${oauth.roleScope('Code Writer')}` });
    const writer = await client.authorizationCodeGrant(config, writeTransaction.url, writeTransaction.checks);
    await success(writer.access_token, push);
    const head = (await success(writer.access_token, ['-C', checkout, 'rev-parse', 'HEAD'])).trim();
    const refs = await success(writer.access_token, ['ls-remote', selectedURL, 'refs/heads/oauth-check']);
    expect(refs.startsWith(`${head}\t`)).toBe(true);
    expect((await git(writer.access_token, ['ls-remote', unselectedURL])).stderr)
      .toContain('Project not found or inaccessible');
    await client.tokenRevocation(config, writer.refresh_token);
    const revoked = await git(writer.access_token, ['ls-remote', selectedURL]);
    expect(revoked.code).not.toBe(0);
    expect(revoked.stderr).toMatch(/401|Authentication failed|terminal prompts disabled/);
    await assert.rejects(client.refreshTokenGrant(config, writer.refresh_token),
      error => error.error === 'invalid_grant');
  } finally {
    await rm(directory, { recursive: true, force: true });
  }
});

test.describe('confidential application secrets', () => {
  test.use({ clientAuth: 'post' });

  test('rotation hides its link while displaying the new secret on details and list pages', async ({ oauth }) => {
    const { page } = oauth;
    const rotate = page.getByRole('link', { name: 'Rotate Secret', exact: true });
    const copy = page.locator('[aria-label="Copy client secret"]');
    const transaction = await oauth.authorize();
    const tokens = await client.authorizationCodeGrant(oauth.config, transaction.url, transaction.checks);
    await page.goto(oauth.applicationURL);
    await expect(copy).toHaveCount(0);
    await expect(rotate).toBeVisible();
    page.once('dialog', dialog => dialog.accept());
    await rotate.click();
    await expect(copy).toBeVisible();
    await expect(rotate).toHaveCount(0);
    expect((await oauth.get(tokens.access_token, `~api/projects/${oauth.selected.id}`)).status).toBe(401);

    await page.goto(`${oauth.serverURL}~my/oauth-applications`);
    const row = page.getByRole('row').filter({ has: page.getByRole('link', { name: oauth.applicationName, exact: true }) });
    await expect(row.locator('[aria-label="Copy client secret"]')).toHaveCount(0);
    page.once('dialog', dialog => dialog.accept());
    await row.getByRole('link', { name: 'Rotate Secret', exact: true }).click();
    await expect(row.locator('[aria-label="Copy client secret"]')).toBeVisible();
    await expect(row.getByRole('link', { name: 'Rotate Secret', exact: true })).toHaveCount(0);
  });
});
