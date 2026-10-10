import type { ReactNode } from 'react'
import { adminHash, consoleHash } from '../../hooks/useHashRoute'
import type { AdminTab } from '../../types/admin'
import type { SessionConfig } from '../../types/session'

const TABS: { id: AdminTab; label: string }[] = [
  { id: 'profiles', label: 'Profiles' },
  { id: 'parameters', label: 'Parameters' },
  { id: 'users', label: 'Users' },
  { id: 'audit', label: 'Audit' },
]

interface AdminShellProps {
  session: SessionConfig
  tab: AdminTab
  busy?: boolean
  onLogout: () => Promise<void>
  children: ReactNode
}

export function AdminShell({
  session,
  tab,
  busy,
  onLogout,
  children,
}: AdminShellProps) {
  return (
    <div className="admin-shell">
      <header className="console-top">
        <div className="console-brand">
          <span className="brand-mark">Bifrost</span>
          <span className="console-sub">admin</span>
        </div>
        <div className="console-user">
          <span className="chip">{session.user.username}</span>
          <span className="chip chip-role">{session.user.role}</span>
          <a className="btn-ghost admin-nav-link" href={consoleHash()}>
            Console
          </a>
          <button
            type="button"
            className="btn-ghost"
            onClick={() => void onLogout()}
            disabled={busy}
          >
            Sair
          </button>
        </div>
      </header>

      <nav className="admin-tabs" aria-label="Admin sections">
        {TABS.map((t) => (
          <a
            key={t.id}
            href={adminHash(t.id)}
            className={`admin-tab${tab === t.id ? ' admin-tab-active' : ''}`}
          >
            {t.label}
          </a>
        ))}
      </nav>

      <main className="admin-main">{children}</main>
    </div>
  )
}
