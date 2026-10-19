import { randomUUID } from 'node:crypto';
import { readFile, writeFile } from 'node:fs/promises';
import { admin, expect, test } from './fixtures.js';
import { login } from './helpers.js';
import { parseCapturedMail, startSmtpCapture } from './smtp-capture.js';

// Set this to product_name in the test server's conf/oem.properties.
const productName = process.env.E2E_PRODUCT_NAME ?? 'OneDev';

test.describe('product branding', () => {
  test('uses the configured name on the login page and preserves JavaScript namespaces', async ({ page }, testInfo) => {
    await page.goto('~login');
    await expect(page).toHaveTitle(`${productName} - Git Server with CI/CD, Kanban, and Packages`);
    await expect(page.getByRole('heading', { name: `Sign In To ${productName}`, exact: true })).toBeVisible();
    await expect(page.locator('.foot')).toHaveText(`Powered by ${productName}`);
    await expect.poll(() => page.evaluate(() => typeof window.onedev?.server?.viewState)).toBe('object');
    await expect(page.getByRole('button', { name: 'Sign in', exact: true })).toBeEnabled();
    const screenshot = testInfo.outputPath('login-branding.png');
    await page.screenshot({ path: screenshot, fullPage: true });
    await testInfo.attach('login-branding', { path: screenshot, contentType: 'image/png' });
  });

  for (const language of ['en', 'zh']) {
    test(`replaces the name after ${language} translation in descriptions and markup`, async ({ page, context, baseURL }, testInfo) => {
      await login(page, admin.name, admin.password);
      await context.addCookies([{ name: 'language', value: language, url: baseURL }]);
      await page.goto('~administration/settings/authenticator');
      const authenticatorDescription = language === 'zh'
        ? `通过 ${productName} 的内置表单登录时，提交的用户凭据可以在此处定义的认证器以及内部数据库中进行检查`
        : `When login via ${productName}'s built-in form, submitted user credentials can be checked against authenticator defined here, besides the internal database`;
      await expect(page.getByText(authenticatorDescription, { exact: true })).toBeVisible();
      // Selecting an implementation reveals its editable descriptions through
      // the normal UI. Leave the form unsaved so existing settings stay intact.
      await page.locator('form.authenticator select').first().selectOption({
        label: language === 'zh' ? '通用 LDAP' : 'Generic LDAP',
      });
      const ldapDescription = language === 'zh'
        ? `${productName} 需要搜索和确定用户 DN，以及在启用组获取时搜索用户组信息。如果这些操作需要认证，请勾选此选项并指定 'manager' DN 和密码`
        : `${productName} needs to search and determine user DN, as well as searching user group information if group retrieval is enabled. Tick this option and specify 'manager' DN and password if these operations needs to be authenticated`;
      await expect(page.getByText(ldapDescription, { exact: true })).toBeVisible();
      if (language === 'zh') {
        const screenshot = testInfo.outputPath('chinese-authenticator-branding.png');
        await page.screenshot({ path: screenshot, fullPage: true });
        await testInfo.attach('chinese-authenticator-branding', { path: screenshot, contentType: 'image/png' });
      }
    });

    test(`preserves the webhook signature header in ${language} product descriptions`, async ({ page, context, baseURL, api }) => {
      const project = await api.createProject();
      await login(page, admin.name, admin.password);
      await context.addCookies([{ name: 'language', value: language, url: baseURL }]);
      await page.goto(`${project.name}/~settings/web-hooks`);
      await page.locator('.web-hooks a.btn-primary').click();
      const editor = page.locator('form.webhook-edit');
      await expect(editor).toBeVisible();
      const description = language === 'zh'
        ? `确保发送到有效负载 URL 的 POST 请求来自 ${productName} 的密钥。设置密钥后，您将在 Webhook POST 请求中收到 X-OneDev-Signature 头部`
        : `The secret which allows you to ensure that POST requests sent to the payload URL are from ${productName}. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request`;
      await expect(editor.getByText(description, { exact: true })).toBeVisible();
      // Opening the editor exercises Ajax rendering without saving a webhook.
      await editor.locator('.modal-header button.close').click();
      await expect(editor).toHaveCount(0);
    });
  }

  test('renders branded REST help while preserving resource classes and cURL parameters', async ({ page }) => {
    await login(page, admin.name, admin.password);
    await page.goto('~help/api/io.onedev.server.rest.resource.TriggerJobResource/triggerJobViaGet');
    await expect(page.getByText(`${productName} access token with permission to trigger the job`, { exact: true })).toBeVisible();
    const example = page.locator('.method-detail code').last();
    await expect(example).toContainText('/~api/trigger-job');
    await expect(example).toContainText('access-token=');
    await expect(page.locator('a[href*="io.onedev.server.rest.resource.TriggerJobResource"]')).not.toHaveCount(0);
  });

  test('brands email template help without rewriting template source or model links', async ({ page, api }) => {
    const templates = await api.json('get', '~api/settings/notification-template');
    await login(page, admin.name, admin.password);
    await page.goto('~administration/settings/email-templates/email-verification');
    const help = page.locator('.card-body > .alert');
    await expect(help).toContainText(`root url of ${productName} server`);
    await expect(help).toContainText('productName:');
    await expect(help.locator('a[href$="/io/onedev/server/model/User.java"]')).toHaveCount(1);
    await expect(page.locator('.CodeMirror')).toBeVisible();
    const defaultTemplate = await readFile(new URL('../../server-core/src/main/java/io/onedev/server/model/support/administration/emailtemplates/default-email-verification.tpl', import.meta.url), 'utf8');
    const legacyDefault = defaultTemplate.replaceAll('${productName}', 'OneDev');
    const expectedTemplate = templates.emailVerification === legacyDefault ? defaultTemplate : templates.emailVerification;
    const displayedTemplate = await page.locator('.CodeMirror').evaluate(editor => editor.CodeMirror.getValue());
    expect(displayedTemplate).toBe(expectedTemplate);
    if (expectedTemplate === defaultTemplate) {
      expect(displayedTemplate).toContain('${productName}');
      expect(displayedTemplate).toContain('${user.name}');
      expect(displayedTemplate).toContain('${verificationUrl}');
    }
    await expect(page.getByRole('button', { name: 'Save Template', exact: true })).toBeVisible();
    expect(await api.json('get', '~api/settings/notification-template')).toEqual(templates);
  });

  test('sends a branded invitation with working links in HTML and plain text', async ({ page, browser, baseURL, api }, testInfo) => {
    const recipient = `branding-${randomUUID()}@example.test`;
    const originalResponse = await api.request.get('~api/settings/mail-service');
    expect(originalResponse.ok(), await originalResponse.text()).toBeTruthy();
    const originalBody = await originalResponse.text();
    const originalConnector = originalBody ? JSON.parse(originalBody) : null;
    const systemSetting = await api.json('get', '~api/settings/system');
    const mail = await startSmtpCapture(recipient);
    const saveConnector = async connector => {
      const response = await api.request.post('~api/settings/mail-service', {
        headers: { 'Content-Type': 'application/json' }, data: JSON.stringify(connector),
      });
      expect(response.ok(), await response.text()).toBeTruthy();
    };
    try {
      await saveConnector(mail.connector);
      await login(page, admin.name, admin.password);
      await page.goto('~administration/invitations/new');
      await page.locator('form textarea').fill(recipient);
      await page.locator('form a.btn-primary').filter({ hasText: /^Invite$/ }).click();
      await expect(page.locator('.task-feedback .result')).toHaveText('Invitations sent');
      await page.locator('.task-feedback').getByRole('button', { name: 'Ok', exact: true }).click();
      await expect(page).toHaveURL(/\/~administration\/invitations(?:[;?#]|$)/);
      await expect.poll(() => mail.messages.length).toBe(1);
      const raw = mail.messages[0];
      const mailPath = testInfo.outputPath('invitation.eml');
      await writeFile(mailPath, Buffer.from(raw, 'latin1'));
      await testInfo.attach('invitation.eml', { path: mailPath, contentType: 'message/rfc822' });
      const message = parseCapturedMail(raw);
      expect(message.headers.subject).toBe(`[Invitation] You are Invited to Use ${productName}`);
      expect(message.headers.from).toContain(`[${productName}]`);
      expect(message.headers.to).toBe(recipient);
      const parts = message.parts.flatMap(part => part.parts ?? [part]);
      const html = parts.find(part => part.headers['content-type'].startsWith('text/html'))?.body;
      const text = parts.find(part => part.headers['content-type'].startsWith('text/plain'))?.body;
      expect(html).toBeTruthy();
      const parsedHtml = await page.evaluate(source => {
        const document = new DOMParser().parseFromString(source, 'text/html');
        return { text: document.body.textContent, invitationUrl: document.querySelector('a')?.getAttribute('href') };
      }, html);
      expect(parsedHtml.text).toContain(`You are invited to use ${productName}, please visit below link to set up account:`);
      expect(text).toContain(`You are invited to use ${productName}, please visit below link to set up account:`);
      const { invitationUrl } = parsedHtml;
      expect(invitationUrl).toBeTruthy();
      expect(invitationUrl.startsWith(`${systemSetting.serverUrl}/~create-user-from-invitation/`)).toBe(true);
      expect(text).toContain(invitationUrl);
      const invitationPath = new URL(invitationUrl).pathname;
      expect(invitationPath).toMatch(/\/~create-user-from-invitation\/\d+\/[\da-f-]+$/i);
      // The advertised server URL can differ from the local test address.
      // Follow its unchanged path against this server without changing settings.
      const anonymous = await browser.newContext({ baseURL });
      try {
        const invitedPage = await anonymous.newPage();
        await invitedPage.goto(invitationPath);
        await expect(invitedPage.getByRole('heading', { name: 'Set Up Your Account', exact: true })).toBeVisible();
        await expect(invitedPage.locator('.sub-title')).toHaveText(recipient);
      } finally {
        await anonymous.close();
      }
    } finally {
      const errors = [];
      try {
        await page.goto(`~administration/invitations?query=${encodeURIComponent(recipient)}`);
        const invitation = page.locator('table.invitations tbody tr').filter({ hasText: recipient });
        if (await invitation.count()) {
          page.once('dialog', dialog => dialog.accept());
          await invitation.locator('[data-tippy-content="Cancel invitation"]').click();
          await expect(invitation).toHaveCount(0);
        }
      } catch (error) { errors.push(error); }
      try {
        await saveConnector(originalConnector);
        const response = await api.request.get('~api/settings/mail-service');
        expect(response.ok(), await response.text()).toBeTruthy();
        const restoredBody = await response.text();
        expect(restoredBody ? JSON.parse(restoredBody) : null).toEqual(originalConnector);
      } catch (error) { errors.push(error); }
      try { await mail.close(); } catch (error) { errors.push(error); }
      if (errors.length) throw new AggregateError(errors, 'Invitation fixture cleanup failed');
    }
  });
});
