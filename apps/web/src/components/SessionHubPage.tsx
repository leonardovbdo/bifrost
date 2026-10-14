import { adminHash, consoleHash } from '../hooks/useHashRoute'
import type { SessionConfig } from '../types/session'

interface SessionHubPageProps {
  session: SessionConfig
  busy: boolean
  error: string | null
  onLogout: () => Promise<void>
  onSelectProfile: (profileId: string) => Promise<void>
  onChangePreset: (preset: string) => Promise<void>
}

const PRESETS = ['safety', 'normal', 'fast'] as const

/** ADR-010 closed enum. Order is the governance matrix, not the profile JSON order. */
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
  const rows: { id: string; present: boolean }[] = PROFILE_CAPABILITIES.map((id) => ({
    id,
    present: present.has(id),
  }))
  for (const extra of declared) {
    if (!known.has(extra)) {
      rows.push({ id: extra, present: true })
    }
  }
  return rows
}

type CapRow = {
  id: string
  label: string
  allowed: boolean
  detail: string
}

function capabilityRows(session: SessionConfig): CapRow[] {
  const { permissions, user } = session
  const canLlm = user.role === 'admin' || user.role === 'operator'
  return [
    {
      id: 'observe',
      label: 'Observação',
      allowed: true,
      detail: 'Câmera, mapa, scan e bateria após conectar',
    },
    {
      id: 'teleop',
      label: 'Teleop',
      allowed: permissions.canTeleop,
      detail: permissions.canTeleop
        ? 'WASD / setas com limits do preset'
        : 'Papel viewer — só observação',
    },
    {
      id: 'goal',
      label: 'Goal Nav2',
      allowed: permissions.canSendGoal,
      detail: permissions.canSendGoal
        ? 'Publica goal_pose + audit'
        : 'Sem permissão canSendGoal',
    },
    {
      id: 'llm',
      label: 'Assistente LLM',
      allowed: canLlm,
      detail: canLlm ? 'Proxy /llm/ask' : 'Só admin e operator',
    },
    {
      id: 'admin',
      label: 'Admin / profiles',
      allowed: permissions.canManageProfiles,
      detail: permissions.canManageProfiles
        ? 'Governança de profiles, users, audit'
        : 'Reservado ao admin',
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
  const caps = capabilityRows(session)
  const profileCaps = profileCapabilityChips(activeProfile.capabilities ?? [])

  function goConsole() {
    window.location.hash = consoleHash()
  }

  return (
    <main className="hub-shell">
      <div className="hub-atmosphere" aria-hidden="true" />
      <div className="hub-layout">
        <header className="hub-brand-block">
          <p className="brand-mark">Bifrost</p>
          <h1 className="hub-title">Sessão</h1>
          <p className="hub-lead">
            Governança primeiro: confira papel, profile e o que você pode fazer.
            A simulação NARA só entra depois de conectar.
          </p>
        </header>

        <section className="hub-card panel">
          <div className="hub-identity">
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

          <dl className="meta-list hub-meta">
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
                desconectado — ainda não conectou
              </dd>
            </div>
          </dl>

          <p className="hub-kicker">Capabilities do profile</p>
          <ul className="profile-cap-list" aria-label="Capabilities do profile">
            {profileCaps.map((cap) => (
              <li
                key={cap.id}
                className={`profile-cap${cap.present ? ' profile-cap-on' : ' profile-cap-off'}`}
              >
                {cap.id}
                <span className="sr-only">
                  {cap.present ? ', declarada' : ', ausente'}
                </span>
              </li>
            ))}
          </ul>

          {permissions.canTeleop ? (
            <label className="field">
              Preset teleop (aplica na session-config)
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
              <p className="muted hub-preset-note">
                Efetivo agora: linear {limits.teleop.linearMax.toFixed(2)} ·
                angular {limits.teleop.angularMax.toFixed(2)}
              </p>
            </label>
          ) : (
            <p className="muted hub-preset-note">
              Limits teleop na config: {limits.teleop.profile} (sem efeito —
              teleop bloqueado).
            </p>
          )}
        </section>

        <section className="hub-card panel">
          <h2>O que esta sessão autoriza</h2>
          <ul className="cap-list">
            {caps.map((row) => (
              <li
                key={row.id}
                className={`cap-row${row.allowed ? ' cap-allowed' : ' cap-denied'}`}
              >
                <span className="cap-mark" aria-hidden="true">
                  {row.allowed ? '●' : '○'}
                </span>
                <div>
                  <p className="cap-label">{row.label}</p>
                  <p className="cap-detail">{row.detail}</p>
                </div>
                <span className="cap-badge">
                  {row.allowed ? 'permitido' : 'bloqueado'}
                </span>
              </li>
            ))}
          </ul>
        </section>

        <section className="hub-actions">
          <button
            type="button"
            className="btn-primary hub-connect"
            onClick={goConsole}
            disabled={busy}
          >
            Conectar ao console NARA
          </button>
          {user.role === 'admin' ? (
            <a className="btn-secondary hub-admin-link" href={adminHash('profiles')}>
              Abrir Admin
            </a>
          ) : null}
          <button
            type="button"
            className="btn-ghost"
            onClick={() => void onLogout()}
            disabled={busy}
          >
            Sair
          </button>
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
