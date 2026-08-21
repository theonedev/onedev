import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig, devices } from '@playwright/test';

const projectDir = path.dirname(fileURLToPath(import.meta.url));
const serverDir = path.resolve(projectDir, '..');
const baseURL = process.env.E2E_BASE_URL ?? 'http://127.0.0.1:6610';

export default defineConfig({
  testDir: './tests',
  outputDir: './test-results',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  reporter: process.env.CI ? 'github' : 'html',
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
      },
});
