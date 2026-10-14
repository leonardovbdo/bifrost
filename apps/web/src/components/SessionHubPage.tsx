import { adminHash, consoleHash } from '../hooks/useHashRoute'
import type { SessionConfig } from '../types/session'
import { UiHint } from './UiHint'

interface SessionHubPageProps {
  session: SessionConfig
  busy: boolean
  error: string | null
  onLogout: () => Promise<void>
  onSelectProfile: (profileId: string) => Promise<void>
  onChangePreset: (preset: string) => Promise<void>
}

const PRESETS = ['safety', 'normal', 'fast'] as const

const PROFILE_CAPABILITIES = [
  'teleop',
  'cameras',
  'slam',
  'nav2',
  'battery',
  'rosapi',
] as const

function profileCapabilityChips(declared: string[]) {
  const present = new Set(declared)
  const known = new Set<string>(PROFILE_CAPABILITIES)
  const rows: { id: string; present: boolean }[] = PROFILE_CAPABILITIES.map(
    (id) => ({
      id,
      present: present.has(id),
    }),
  )
  for (const extra of declared) {
    if (!known.has(extra)) {
      rows.push({ id: extra, present: true })
    }
  }
  return rows
}

type AuthRow = { id: string; label: string; allowed: boolean }

function authRows(session: SessionConfig): AuthRow[] {
  const { permissions, user } = session
  const canLlm = user.role === 'admin' || user.role === 'operator'
  return [
    { id: 'observe', label: 'Observação (câmera, mapa, bateria)', allowed: true },
    { id: 'teleop', label: 'Teleop', allowed: permissions.canTeleop },
    { id: 'goal', label: 'Goal Nav2', allowed: permissions.canSendGoal },
    { id: 'llm', label: 'Assistente LLM', allowed: canLlm },
    {
      id: 'admin',
      label: 'Admin',
      allowed: permissions.canManageProfiles,
    },
  ]
}

export function SessionHubPage({
  session,
  busy,
  error,
  onLogout,
  onSelectProfile,
  onChangePreset,
}: SessionHubPageProps) {
  const { user, activeProfile, allowedProfiles, permissions, limits } = session
  const rows = authRows(session)
  const profileCaps = profileCapabilityChips(activeProfile.capabilities ?? [])

  function goConsole() {
    window.location.hash = consoleHash()
  }

  return (
    <main className="hub-shell" data-testid="session-hub">
      <div className="hub-layout">
        <header className="hub-top">
          <div>
            <p className="brand-mark">Bifrost</p>
            <h1 className="hub-title">Sessão</h1>
            <p className="hub-lead">
              Confira papel e autorizações. O console NARA só abre quando você
              entrar.
            </p>
            <UiHint id="hub">
              Hub de governança: mostra role, profile e o que a session-config
              autoriza. ROS permanece desconectado até Entrar no console.
            </UiHint>
          </div>
          <button
            type="button"
            className="btn-ghost"
            onClick={() => void onLogout()}
            disabled={busy}
          >
            Sair
          </button>
        </header>

        <section className="hub-paper">
          <div className="hub-identity-row">
            <div>
              <p className="hub-kicker">Operador</p>
              <p className="hub-user">{user.username}</p>
            </div>
            <span className={`hub-role hub-role-${user.role}`}>{user.role}</span>
          </div>

          <label className="field">
            Robot profile
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

          <div>
            <p className="hub-kicker">Esta sessão autoriza</p>
            <ul className="hub-auth-list" data-testid="auth-matrix">
              {rows.map((row) => (
                <li
                  key={row.id}
                  className={`hub-auth-row${row.allowed ? '' : ' is-denied'}`}
                  data-testid={`auth-${row.id}`}
                  data-allowed={row.allowed ? 'true' : 'false'}
                >
                  <p className="hub-auth-label">{row.label}</p>
                  <span className="hub-auth-state">
                    {row.allowed ? 'sim' : 'não'}
                  </span>
                </li>
              ))}
            </ul>
          </div>

          <details className="hub-details">
            <summary>Detalhes do profile e conexão</summary>
            <div className="hub-details-body">
              <dl className="hub-meta-grid">
                <div>
                  <dt>Slug</dt>
                  <dd>{activeProfile.slug}</dd>
                </div>
                <div>
                  <dt>Ambiente</dt>
                  <dd>
                    {activeProfile.environment} · {activeProfile.project}
                  </dd>
                </div>
                <div>
                  <dt>Rosbridge</dt>
                  <dd className="mono">{activeProfile.rosbridgeUrl}</dd>
                </div>
                <div>
                  <dt>Runtime</dt>
                  <dd>
                    <span className="status-dot status-idle" />
                    desconectado
                  </dd>
                </div>
              </dl>

              <div>
                <p className="hub-kicker">Capabilities (profile)</p>
                <ul className="profile-cap-list" aria-label="Capabilities do profile">
                  {profileCaps.map((cap) => (
                    <li
                      key={cap.id}
                      className={`profile-cap${cap.present ? ' profile-cap-on' : ''}`}
                    >
                      {cap.id}
                      <span className="sr-only">
                        {cap.present ? ', declarada' : ', ausente'}
                      </span>
                    </li>
                  ))}
                </ul>
              </div>

              {permissions.canTeleop ? (
                <label className="field">
                  Preset teleop
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
                  <p className="muted hint">
                    Efetivo: linear {limits.teleop.linearMax.toFixed(2)} · angular{' '}
                    {limits.teleop.angularMax.toFixed(2)}
                  </p>
                </label>
              ) : (
                <p className="muted hint">
                  Preset na config: {limits.teleop.profile} (teleop bloqueado).
                </p>
              )}
            </div>
          </details>
        </section>

        <section className="hub-actions">
          <button
            type="button"
            className="btn-primary hub-connect"
            onClick={goConsole}
            disabled={busy}
            data-testid="enter-console"
          >
            Entrar no console
          </button>
          {user.role === 'admin' ? (
            <a className="btn-secondary hub-admin-link" href={adminHash('profiles')}>
              Admin
            </a>
          ) : null}
          {error ? (
            <p className="form-error" role="alert">
              {error}
            </p>
          ) : null}
        </section>
      </div>
    </main>
  )
}
