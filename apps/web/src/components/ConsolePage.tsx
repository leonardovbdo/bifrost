import type { ReactNode } from 'react'
import { adminHash, sessionHash } from '../hooks/useHashRoute'
import type { RosStatus } from '../hooks/useRos'
import type { SessionConfig } from '../types/session'
import { CameraStage } from './CameraFeed'

interface ConsolePageProps {
  session: SessionConfig
  busy: boolean
  error: string | null
  rosStatus: RosStatus
  rosError: string | null
  batteryPercent: number | null
  onLogout: () => Promise<void>
  onSelectProfile: (profileId: string) => Promise<void>
  onChangePreset: (preset: string) => Promise<void>
  onStop: () => void
  children?: ReactNode
}

const PRESETS = ['safety', 'normal', 'fast'] as const

export function ConsolePage({
  session,
  busy,
  error,
  rosStatus,
  rosError,
  batteryPercent,
  onLogout,
  onSelectProfile,
  onChangePreset,
  onStop,
  children,
}: ConsolePageProps) {
  const { user, activeProfile, allowedProfiles, permissions, limits } = session
  const teleopEnabled = permissions.canTeleop
  const batteryLabel =
    batteryPercent != null
      ? `${batteryPercent.toFixed(0)}%`
      : rosStatus === 'connected'
        ? '—'
        : rosStatus === 'connecting'
          ? '…'
          : 'off'

  function disconnect() {
    window.location.hash = sessionHash()
  }

  return (
    <div className="console-shell">
      <header className="console-top">
        <div className="console-brand">
          <span className="brand-mark">Bifrost</span>
          <span className="console-sub">console ao vivo</span>
        </div>
        <div className="console-user">
          <span
            className="console-status-line"
            title={rosError ?? activeProfile.topics.battery ?? rosStatus}
          >
            <span className={`status-dot status-${rosStatus}`} />
            {rosStatus}
            <span className="console-sep">·</span>
            bat {batteryLabel}
          </span>
          <span className="chip chip-role">{user.role}</span>
          {user.role === 'admin' ? (
            <a className="btn-ghost admin-nav-link" href={adminHash('profiles')}>
              Admin
            </a>
          ) : null}
          <button type="button" className="btn-secondary" onClick={disconnect}>
            Desconectar
          </button>
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

      <div className="console-grid">
        <aside className="console-side">
          <section className="panel">
            <h2>Sessão</h2>
            <p className="console-profile-name">{activeProfile.displayName}</p>
            <label className="field">
              Profile
              <select
                value={activeProfile.id}
                disabled={busy || allowedProfiles.length < 2}
                onChange={(e) => void onSelectProfile(e.target.value)}
              >
                {allowedProfiles.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.displayName}
                  </option>
                ))}
              </select>
            </label>
            <p className="muted mono console-ros-url">{activeProfile.rosbridgeUrl}</p>
            {rosError ? (
              <p className="form-error console-ros-error" role="alert">
                {rosError}
              </p>
            ) : null}
          </section>

          {teleopEnabled ? (
            <section className="panel">
              <h2>Teleop</h2>
              <label className="field">
                Preset
                <select
                  value={limits.teleop.profile}
                  disabled={busy}
                  onChange={(e) => void onChangePreset(e.target.value)}
                >
                  {PRESETS.map((p) => (
                    <option key={p} value={p}>
                      {p}
                    </option>
                  ))}
                </select>
              </label>
              <p className="muted">
                lim linear {limits.teleop.linearMax.toFixed(2)} · angular{' '}
                {limits.teleop.angularMax.toFixed(2)}
              </p>
              <p className="hint">WASD / setas · espaço = stop</p>
              <button type="button" className="btn-secondary" onClick={onStop}>
                Stop
              </button>
            </section>
          ) : null}

          {children}
        </aside>

        <section className="console-stage panel stage-panel">
          <CameraStage profile={activeProfile} />
          {error ? <p className="form-error">{error}</p> : null}
        </section>
      </div>
    </div>
  )
}
