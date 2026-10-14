import { useState, type KeyboardEvent, type ReactNode } from 'react'
import { adminHash, sessionHash } from '../hooks/useHashRoute'
import type { RosStatus } from '../hooks/useRos'
import type { SessionConfig } from '../types/session'
import { CameraStage } from './CameraFeed'
import { UiHint } from './UiHint'

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
  const visibleTabs = tabs.filter((t) => t.show)
  const activeTab = visibleTabs.some((t) => t.id === tab) ? tab : 'camera'
  function onTabKey(event: KeyboardEvent<HTMLButtonElement>, index: number) {
    if (
      event.key !== 'ArrowRight' &&
      event.key !== 'ArrowLeft' &&
      event.key !== 'Home' &&
      event.key !== 'End'
    ) {
      return
    }
    event.preventDefault()
    const last = visibleTabs.length - 1
    const nextIndex =
      event.key === 'ArrowRight'
        ? (index + 1) % visibleTabs.length
        : event.key === 'ArrowLeft'
          ? (index - 1 + visibleTabs.length) % visibleTabs.length
          : event.key === 'Home'
            ? 0
            : last
    const next = visibleTabs[nextIndex]
    setTab(next.id)
    document.getElementById(`stage-tab-${next.id}`)?.focus()
  }

  return (
    <div className="console-shell" data-testid="live-console">
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
          <button
            type="button"
            className="btn-secondary"
            onClick={disconnect}
            data-testid="disconnect"
          >
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
        <aside className="console-rail" data-testid="console-rail">
          <UiHint id="console">
            Console ops: rosbridge sobe aqui. Rail = teleop/goal; tabs = câmera,
            mapa, scan e assistente. Desconectar volta ao hub e fecha o ROS.
          </UiHint>
          {teleopEnabled ? (
            <section className="rail-block" data-testid="teleop-block">
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
              {visibleTabs.map((t, index) => (
                <button
                  key={t.id}
                  id={`stage-tab-${t.id}`}
                  type="button"
                  role="tab"
                  aria-selected={activeTab === t.id}
                  aria-controls={`stage-panel-${t.id}`}
                  tabIndex={activeTab === t.id ? 0 : -1}
                  className={`stage-tab${activeTab === t.id ? ' stage-tab-active' : ''}`}
                  onClick={() => setTab(t.id)}
                  onKeyDown={(event) => onTabKey(event, index)}
                >
                  {t.label}
                </button>
              ))}
            </div>
          </div>

          <div className="stage-viewport">
            <div
              id="stage-panel-camera"
              role="tabpanel"
              aria-labelledby="stage-tab-camera"
              inert={activeTab !== 'camera' ? true : undefined}
              className={`stage-pane is-camera${activeTab === 'camera' ? '' : ' is-parked'}`}
            >
              <CameraStage profile={activeProfile} />
            </div>
            {mapSlot ? (
              <div
                id="stage-panel-map"
                role="tabpanel"
                aria-labelledby="stage-tab-map"
                hidden={activeTab !== 'map'}
                className="stage-pane is-panel"
              >
                {mapSlot}
              </div>
            ) : null}
            {scanSlot ? (
              <div
                id="stage-panel-scan"
                role="tabpanel"
                aria-labelledby="stage-tab-scan"
                hidden={activeTab !== 'scan'}
                className="stage-pane is-panel"
              >
                {scanSlot}
              </div>
            ) : null}
            {llmSlot ? (
              <div
                id="stage-panel-llm"
                role="tabpanel"
                aria-labelledby="stage-tab-llm"
                hidden={activeTab !== 'llm'}
                className="stage-pane is-panel"
              >
                {llmSlot}
              </div>
            ) : null}
          </div>
        </section>
      </div>
    </div>
  )
}
