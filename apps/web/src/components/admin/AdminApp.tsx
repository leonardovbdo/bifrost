import type { AdminTab } from '../../types/admin'
import type { SessionConfig } from '../../types/session'
import { AdminAuditPage } from './AdminAuditPage'
import { AdminParametersPage } from './AdminParametersPage'
import { AdminProfilesPage } from './AdminProfilesPage'
import { AdminShell } from './AdminShell'
import { AdminUsersPage } from './AdminUsersPage'

interface AdminAppProps {
  session: SessionConfig
  tab: AdminTab
  busy?: boolean
  onLogout: () => Promise<void>
}

export function AdminApp({ session, tab, busy, onLogout }: AdminAppProps) {
  let page = <AdminProfilesPage />
  if (tab === 'parameters') page = <AdminParametersPage />
  if (tab === 'users') page = <AdminUsersPage />
  if (tab === 'audit') page = <AdminAuditPage />

  return (
    <AdminShell session={session} tab={tab} busy={busy} onLogout={onLogout}>
      {page}
    </AdminShell>
  )
}
