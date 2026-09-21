import { spawn } from 'node:child_process';
import { once } from 'node:events';
import { access } from 'node:fs/promises';
import net from 'node:net';
import { fileURLToPath } from 'node:url';
import nodemailer from 'nodemailer';
import { expect } from '@playwright/test';

async function freePort() {
  const server = net.createServer();
  server.listen(0, '127.0.0.1');
  await once(server, 'listening');
  const { port } = server.address();
  await new Promise(resolve => server.close(resolve));
  return port;
}

// Real SMTP and IMAP, confined to loopback. GreenMail never forwards mail.
export async function startMailServer(project, testInfo) {
  const jar = fileURLToPath(new URL('../target/greenmail-standalone-2.1.14.jar', import.meta.url));
  await access(jar).catch(() => { throw new Error('Run npm run install:mail-server before the service desk tests'); });
  const smtpPort = await freePort();
  const imapPort = await freePort();
  const user = `desk+${project.name}`;
  const password = 'e2e-mail-password';
  const address = `${user}@example.test`;
  const child = spawn('java', [
    '-Dgreenmail.verbose',
    '-Dgreenmail.hostname=127.0.0.1',
    `-Dgreenmail.smtp.port=${smtpPort}`,
    `-Dgreenmail.imap.port=${imapPort}`,
    `-Dgreenmail.users=${user}:${password}@example.test`,
    '-jar', jar,
  ], { stdio: ['ignore', 'pipe', 'pipe'] });
  let log = '';
  let failure;
  child.stdout.on('data', data => { log += data; });
  child.stderr.on('data', data => { log += data; });
  child.on('error', error => { failure = error; });
  const transport = nodemailer.createTransport({
    host: '127.0.0.1', port: smtpPort, secure: false, ignoreTLS: true,
    connectionTimeout: 5000, greetingTimeout: 5000, socketTimeout: 5000,
  });
  const stopped = new Promise(resolve => {
    child.once('exit', resolve);
    child.once('error', resolve);
  });
  const mail = {
    connector: {
      '@type': 'SmtpImapConnector', smtpHost: '127.0.0.1',
      sslSetting: { '@type': 'SmtpWithoutSsl', port: smtpPort },
      systemAddress: 'desk@example.test', concurrency: 1, timeout: 10,
      inboxPollSetting: {
        imapHost: '127.0.0.1', sslSetting: { '@type': 'ImapWithoutSsl', port: imapPort },
        imapUser: user, imapPassword: password, pollInterval: 5,
      },
    },
    async waitForInbox() {
      // OneDev ignores pre-existing messages on the first connection. Wait for
      // two NOOPs: Jakarta Mail can send the first during a slow initial scan.
      // The second proves that normal polling of the empty inbox has begun.
      await expect.poll(() => {
        if (failure) throw failure;
        expect(child.exitCode, log).toBeNull();
        return (log.match(/C: [^\r\n]*NOOP/g) ?? []).length;
      }, { timeout: 60000, message: 'OneDev should start polling the empty test inbox' }).toBeGreaterThanOrEqual(2);
    },
    async send({ subject, text, messageId }) {
      const result = await transport.sendMail({
        from: 'Customer <customer@example.test>', to: address, subject, text, messageId,
      });
      expect(result.accepted).toEqual([address]);
    },
    async close() {
      transport.close();
      child.kill('SIGTERM');
      const timeout = setTimeout(() => child.kill('SIGKILL'), 5000);
      try {
        await stopped;
      } finally {
        clearTimeout(timeout);
        await testInfo.attach('mail-server.log', { body: log, contentType: 'text/plain' });
      }
    },
  };
  try {
    await expect(async () => {
      if (failure) throw failure;
      expect(child.exitCode, log).toBeNull();
      await transport.verify();
    }).toPass({ timeout: 30000, intervals: [250, 500, 1000] });
    return mail;
  } catch (error) {
    await mail.close();
    throw error;
  }
}
