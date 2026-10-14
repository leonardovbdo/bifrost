import { defineConfig, devices } from '@playwright/test'

/**
 * E2E against a running stack:
 * - Vite :5173 (proxy /api → :8081)
 * - Backend :8081 + Postgres
 *
 *   npm run test:e2e
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [['list']],
  timeout: 60_000,
  use: {
    baseURL: process.env.BIFROST_E2E_BASE_URL ?? 'http://localhost:5173',
    trace: 'on-first-retry',
    ...devices['Desktop Chrome'],
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
