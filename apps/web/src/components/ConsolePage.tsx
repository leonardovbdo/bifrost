import type { ReactNode } from 'react'
import type { RosStatus } from '../hooks/useRos'
import type { SessionConfig } from '../types/session'
import { CameraStage } from './CameraFeed'

interface ConsolePageProps {
  session: SessionConfig
  busy: boolean
  error: string | null
  rosStatus: RosStatus
  rosError: string | null
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
  onLogout,
  onSelectProfile,
  onChangePreset,
  onStop,
  children,
}: ConsolePageProps) {
  const { user, activeProfile, allowedProfiles, permissions, limits } = session
  const teleopEnabled = permissions.canTeleop

  return (
    <div className="console-shell">
      <header className="console-top">
        <div className="console-brand">
          <span className="brand-mark">Bifrost</span>
          <span className="console-sub">console NARA</span>
        </div>
        <div className="console-user">
          <span className="chip">{user.username}</span>
          <span className="chip chip-role">{user.role}</span>
          <button type="button" className="btn-ghost" onClick={() => void onLogout()} disabled={busy}>
            Sair
          </button>
        </div>
      </header>

      <div className="console-grid">
        <aside className="console-side">
          <section className="panel">
            <h2>Profile</h2>
            <label className="field">
              Ativo
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
            <dl className="meta-list">
              <div>
                <dt>Slug</dt>
                <dd>{activeProfile.slug}</dd>
              </div>
              <div>
                <dt>Rosbridge</dt>
                <dd className="mono">{activeProfile.rosbridgeUrl}</dd>
              </div>
              <div>
                <dt>ROS</dt>
                <dd>
                  <span className={`status-dot status-${rosStatus}`} />
                  {rosStatus}
                  {rosError ? ` — ${rosError}` : ''}
                </dd>
              </div>
            </dl>
          </section>

          <section className="panel">
            <h2>Teleop</h2>
            {!teleopEnabled ? (
              <p className="muted">Seu papel não permite teleop.</p>
            ) : (
              <>
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
              </>
            )}
          </section>

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
