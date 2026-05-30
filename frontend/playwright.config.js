import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: './tests/reports/playwright', open: 'never' }],
    ['json', { outputFile: './tests/reports/playwright/results.json' }],
    ['list']
  ],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 30000
  },
  // globalSetup: './tests/e2e/setup/globalSetup.js',
  // globalTeardown: './tests/e2e/setup/globalTeardown.js',
  projects: [
    // Chromium（Chrome/Edge）
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
      }
    },
    // Firefox
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    },
    // WebKit（Safari）
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] }
    }
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 300000
  }
})
