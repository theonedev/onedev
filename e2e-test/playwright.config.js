import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig, devices } from '@playwright/test';

const projectDir = path.dirname(fileURLToPath(import.meta.url));
const serverDir = path.resolve(projectDir, '..');
const baseURL = process.env.E2E_BASE_URL ?? 'http://127.0.0.1:6610';

function getHostIp() {
  for (const addresses of Object.values(os.networkInterfaces())) {
    for (const address of addresses ?? []) {
      const family = address.family;
      if ((family === 'IPv4' || family === 4) && !address.internal) {
        return address.address;
      }
    }
  }
  return '127.0.0.1';
}

export default defineConfig({
  testDir: './tests',
  outputDir: './test-results',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  reporter: process.env.CI ? 'github' : 'html',
  // OneDev's Wicket UI is sensitive to concurrent logins against a single server.
  workers: 1,
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: process.env.E2E_SKIP_WEBSERVER
    ? undefined
    : {
        command: './dev.sh run',
        cwd: serverDir,
        url: baseURL,
        reuseExistingServer: true,
        timeout: 10 * 60 * 1000,
        stdout: 'pipe',
        stderr: 'pipe',
        // Unattended first-run setup; see https://docs.onedev.io/installation-guide/run-as-docker-container
        env: {
          initial_user: 'admin',
          initial_password: 'admin',
          initial_email: 'admin@example.com',
          initial_server_url: `http://${getHostIp()}:6610`,
        },
      },
});
