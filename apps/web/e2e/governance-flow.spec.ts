import { expect, test } from '@playwright/test'

/** Prefer BIFROST_* from repo `.env` (export before `npm run test:e2e`). */
const adminUser =
  process.env.BIFROST_E2E_ADMIN_USER ??
  process.env.BIFROST_ADMIN_USERNAME ??
  'admin'
const adminPass =
  process.env.BIFROST_E2E_ADMIN_PASS ??
  process.env.BIFROST_ADMIN_PASSWORD ??
  'change-me'
const viewerUser =
  process.env.BIFROST_E2E_VIEWER_USER ??
  process.env.BIFROST_VIEWER_USERNAME ??
  'viewer'
const viewerPass =
  process.env.BIFROST_E2E_VIEWER_PASS ??
  process.env.BIFROST_VIEWER_PASSWORD ??
  'change-me'

async function login(page: import('@playwright/test').Page, user: string, pass: string) {
  await page.goto('/')
  await expect(page.getByTestId('hint-login')).toBeVisible()
  await page.getByTestId('login-username').fill(user)
  await page.getByTestId('login-password').fill(pass)
  await page.getByTestId('login-submit').click()
  await expect(page.getByTestId('session-hub')).toBeVisible({ timeout: 20_000 })
}

test.describe('Bifrost governance UX', () => {
  test('login shows identity hint then session hub', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByTestId('hint-login')).toContainText(/JWT|identidade|API Bifrost/i)
    await expect(page.getByTestId('login-form')).toBeVisible()

    await login(page, adminUser, adminPass)

    await expect(page.getByTestId('hint-hub')).toContainText(/governança|session-config|desconectado/i)
    await expect(page.getByTestId('auth-matrix')).toBeVisible()
    await expect(page.getByTestId('auth-teleop')).toHaveAttribute('data-allowed', 'true')
    await expect(page.getByTestId('auth-admin')).toHaveAttribute('data-allowed', 'true')
    await expect(page.getByTestId('enter-console')).toBeVisible()
  })

  test('admin: hub → console ops → disconnect back to hub', async ({ page }) => {
    await login(page, adminUser, adminPass)

    await page.getByTestId('enter-console').click()
    await expect(page.getByTestId('live-console')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('hint-console')).toContainText(/rosbridge|Desconectar|hub/i)
    await expect(page.getByTestId('console-rail')).toBeVisible()
    await expect(page.getByTestId('teleop-block')).toBeVisible()

    await page.getByTestId('disconnect').click()
    await expect(page.getByTestId('session-hub')).toBeVisible()
    await expect(page.getByTestId('live-console')).toHaveCount(0)
  })

  test('admin shell explains governance (no ROS)', async ({ page }) => {
    await login(page, adminUser, adminPass)
    await page.goto('/#/admin/profiles')
    await expect(page.getByTestId('admin-shell')).toBeVisible()
    await expect(page.getByTestId('hint-admin')).toContainText(/governança|rosbridge|admin/i)
  })

  test('viewer: teleop/goal/llm blocked; console hides teleop', async ({ page }) => {
    await login(page, viewerUser, viewerPass)

    await expect(page.getByTestId('auth-teleop')).toHaveAttribute('data-allowed', 'false')
    await expect(page.getByTestId('auth-goal')).toHaveAttribute('data-allowed', 'false')
    await expect(page.getByTestId('auth-llm')).toHaveAttribute('data-allowed', 'false')
    await expect(page.getByTestId('auth-admin')).toHaveAttribute('data-allowed', 'false')

    await page.getByTestId('enter-console').click()
    await expect(page.getByTestId('live-console')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByTestId('teleop-block')).toHaveCount(0)
    await expect(page.getByRole('tab', { name: 'Assistente' })).toHaveCount(0)
  })
})
