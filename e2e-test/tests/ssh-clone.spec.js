import { execFile } from 'node:child_process';
import { mkdtemp, readFile, rm } from 'node:fs/promises';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import { promisify } from 'node:util';
import { expect, test as base } from './fixtures.js';

const exec = promisify(execFile);
const quote = value => `'${value.replaceAll("'", "'\\''")}'`;

const test = base.extend({
  sshProject: async ({ api }, use) => {
    const project = await api.createProject();
    const { ssh: cloneUrl } = await api.json('get', `~api/projects/${project.id}/clone-url`);
    const url = new URL(cloneUrl);
    expect(url.protocol).toBe('ssh:');
    // HTTP readiness can precede the SystemStarted listener that binds SSH.
    await expect(async () => {
      await new Promise((resolve, reject) => {
        const socket = net.createConnection({
          host: url.hostname.replace(/^\[|\]$/g, ''), port: Number(url.port || 22),
        });
        const finish = error => {
          socket.destroy();
          if (error) reject(error);
          else resolve();
        };
        socket.once('connect', () => finish());
        socket.once('error', finish);
        socket.setTimeout(5000, () => finish(new Error(`Timed out connecting to ${url.host}`)));
      });
    }, `SSH should listen at ${url.host}`).toPass({ timeout: 60000, intervals: [250, 500, 1000] });
    await use({ ...project, cloneUrl });
  },
});

for (const { name, keyType, keyArgs, signatureAlgorithm } of [
  { name: 'Ed25519', keyType: 'ed25519', keyArgs: [], signatureAlgorithm: 'ssh-ed25519' },
  { name: 'RSA with SHA-256', keyType: 'rsa', keyArgs: ['-b', '3072'], signatureAlgorithm: 'rsa-sha2-256' },
  { name: 'RSA with SHA-512', keyType: 'rsa', keyArgs: ['-b', '3072'], signatureAlgorithm: 'rsa-sha2-512' },
]) {
test(`clones over SSH using ${name}`, async ({ api, sshProject: project }) => {
  const user = await api.createUser();
  await api.authorizeUser(project, user, 'Code Reader');
  const content = `# Cloned with ${name}\n`;
  const commit = await api.putFile(project, 'README.md', content);
  const { cloneUrl } = project;

  const directory = await mkdtemp(path.join(os.tmpdir(), 'onedev-ssh-'));
  try {
    const key = path.join(directory, `id_${keyType}`);
    await exec('ssh-keygen', ['-q', '-t', keyType, ...keyArgs, '-N', '', '-f', key], { timeout: 30000 });
    const publicKey = await readFile(`${key}.pub`, 'utf8');
    expect(publicKey.startsWith(`ssh-${keyType} `)).toBeTruthy();

    // Isolate SSH from the developer's keys, agent, configuration and known hosts.
    // Only this key and signature algorithm may authenticate; no credential fallback.
    const sshCommand = [
      'ssh', '-F', '/dev/null', '-i', key,
      '-o', 'IdentityAgent=none', '-o', 'IdentitiesOnly=yes', '-o', 'BatchMode=yes',
      '-o', 'PreferredAuthentications=publickey', '-o', `PubkeyAcceptedAlgorithms=${signatureAlgorithm}`,
      '-o', 'StrictHostKeyChecking=accept-new',
      '-o', `UserKnownHostsFile=${path.join(directory, 'known_hosts')}`,
      '-o', 'GlobalKnownHostsFile=/dev/null', '-o', 'ConnectTimeout=10',
    ].map(quote).join(' ');
    const options = {
      timeout: 30000,
      env: {
        ...process.env, GIT_SSH_COMMAND: sshCommand, GIT_SSH_VARIANT: 'ssh',
        GIT_TERMINAL_PROMPT: '0', GIT_CONFIG_NOSYSTEM: '1', GIT_CONFIG_GLOBAL: '/dev/null',
      },
    };

    // An unregistered key must fail, proving the clone cannot use ambient credentials.
    await expect(exec('git', ['clone', '--branch', 'main', cloneUrl,
      path.join(directory, 'unauthorized')], options)).rejects.toMatchObject({
      stderr: expect.stringContaining('Permission denied (publickey)'),
    });

    // Deleting the fixture user also deletes this key.
    await api.json('post', '~api/ssh-keys', { data: { ownerId: user.id, content: publicKey } });
    const checkout = path.join(directory, 'checkout');
    await exec('git', ['clone', '--branch', 'main', cloneUrl, checkout], options);
    expect(await readFile(path.join(checkout, 'README.md'), 'utf8')).toBe(content);
    const { stdout } = await exec('git', ['-C', checkout, 'rev-parse', 'HEAD'], options);
    expect(stdout.trim()).toBe(commit.commitHash);
  } finally {
    await rm(directory, { recursive: true, force: true });
  }
});
}
