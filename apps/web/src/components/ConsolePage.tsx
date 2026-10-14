import { useState, type ReactNode } from 'react'
import { adminHash, sessionHash } from '../hooks/useHashRoute'
import type { RosStatus } from '../hooks/useRos'
import type { SessionConfig } from '../types/session'
import { CameraStage } from './CameraFeed'

type StageTab = 'camera' | 'map' | 'scan' | 'llm'

interface ConsolePageProps {
  session: SessionConfig
  busy: boolean
  error: string | null
  rosStatus: RosStatus
  rosError: string | null
  batteryPercent: number | null
  onLogout: () => Promise<void>
  onChangePreset: (preset: string) => Promise<void>
  onStop: () => void
  goalSlot?: ReactNode
  mapSlot?: ReactNode
  scanSlot?: ReactNode
  llmSlot?: ReactNode
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
  onChangePreset,
  onStop,
  goalSlot,
  mapSlot,
  scanSlot,
  llmSlot,
}: ConsolePageProps) {
  const { user, activeProfile, permissions, limits } = session
  const teleopEnabled = permissions.canTeleop
  const [tab, setTab] = useState<StageTab>('camera')

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

  const tabs: { id: StageTab; label: string; show: boolean }[] = [
    { id: 'camera', label: 'Câmera', show: true },
    { id: 'map', label: 'Mapa', show: Boolean(mapSlot) },
    { id: 'scan', label: 'Scan', show: Boolean(scanSlot) },
    { id: 'llm', label: 'Assistente', show: Boolean(llmSlot) },
  ]

  return (
    <div className="console-shell">
      <header className="console-top">
        <div className="console-brand">
          <span className="brand-mark">Bifrost</span>
          <span className="console-sub">{activeProfile.displayName}</span>
        </div>
        <div className="console-top-meta">
          <span
            className="console-stat"
            title={rosError ?? rosStatus}
          >
            <span className={`status-dot status-${rosStatus}`} />
            <strong>ROS</strong>
            {rosStatus}
          </span>
          <span className="console-stat">
            <strong>Bat</strong>
            {batteryLabel}
          </span>
          <span className="console-stat">
            <strong>{user.role}</strong>
          </span>
          {user.role === 'admin' ? (
            <a className="btn-ghost admin-nav-link" href={adminHash('profiles')}>
              Admin
            </a>
          ) : null}
          <a className="btn-ghost" href={sessionHash()}>
            Sessão
          </a>
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

      <div className="console-body">
        <aside className="console-rail">
          {teleopEnabled ? (
            <section className="rail-block">
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
              <p className="hint">
                lin {limits.teleop.linearMax.toFixed(2)} · ang{' '}
                {limits.teleop.angularMax.toFixed(2)}
              </p>
              <p className="hint">WASD / setas · espaço = stop</p>
              <button type="button" className="btn-secondary" onClick={onStop}>
                Stop
              </button>
            </section>
          ) : null}

          {goalSlot ? <div className="rail-block">{goalSlot}</div> : null}

          {rosError ? (
            <p className="form-error console-ros-error" role="alert">
              {rosError}
            </p>
          ) : null}
          {error ? (
            <p className="form-error" role="alert">
              {error}
            </p>
          ) : null}
        </aside>

        <section className="console-stage">
          <div className="stage-toolbar">
            <div className="stage-tabs" role="tablist" aria-label="Viewport">
              {tabs
                .filter((t) => t.show)
                .map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    role="tab"
                    aria-selected={tab === t.id}
                    className={`stage-tab${tab === t.id ? ' stage-tab-active' : ''}`}
                    onClick={() => setTab(t.id)}
                  >
                    {t.label}
                  </button>
                ))}
            </div>
          </div>

          <div
            className={`stage-viewport${tab === 'camera' ? ' is-camera' : ' is-panel'}`}
          >
            {tab === 'camera' ? <CameraStage profile={activeProfile} /> : null}
            {tab === 'map' ? mapSlot : null}
            {tab === 'scan' ? scanSlot : null}
            {tab === 'llm' ? llmSlot : null}
          </div>
        </section>
      </div>
    </div>
  )
}
