import { useCallback, useEffect, useState } from 'react'
import {
  createRobotProfile,
  grantProfileAccess,
  listRobotProfiles,
  listUsers,
  patchRobotProfile,
} from '../../api/bifrost'
import { ApiError } from '../../api/client'
import {
  parseCapabilities,
  parseJsonObject,
  stringifyJson,
} from '../../lib/jsonField'
import type { AdminUser, RobotProfileDetail } from '../../types/admin'

const EMPTY_TOPICS = stringifyJson({
  cmd_vel: '/noblenara/alfa/cmd_vel',
  camera_link: '/noblenara/alfa/camera_link/image',
  camera_user: '/noblenara/alfa/camera_user',
  scan: '/noblenara/alfa/scan_filtered',
  map: '/noblenara/alfa/map',
  odom: '/noblenara/alfa/odom',
  battery: '/noblenara/alfa/battery_status',
  goal_pose: '/noblenara/alfa/goal_pose',
})

const EMPTY_FRAMES = stringifyJson({
  map: 'map',
  odom: 'odom',
  base: 'base_link',
  camera: 'camera_link',
})

type Mode = 'create' | 'edit'

function profileToForm(p: RobotProfileDetail) {
  return {
    slug: p.slug,
    displayName: p.displayName,
    project: p.project,
    prefix: p.prefix,
    environment: p.environment,
    technology: p.technology,
    capabilities: p.capabilities.join(', '),
    topicsJson: stringifyJson(p.topics),
    framesJson: stringifyJson(p.frames),
    rosbridgeUrl: p.rosbridgeUrl,
    videoBaseUrl: p.videoBaseUrl,
    active: p.active,
  }
}

const blankForm = {
  slug: '',
  displayName: '',
  project: 'noblenara',
  prefix: 'alfa',
  environment: 'sim',
  technology: 'wheelchair_nara',
  capabilities: 'teleop, cameras, slam, nav2, battery',
  topicsJson: EMPTY_TOPICS,
  framesJson: EMPTY_FRAMES,
  rosbridgeUrl: 'ws://localhost:9090',
  videoBaseUrl: 'http://localhost:8080',
  active: true,
}

export function AdminProfilesPage() {
  const [profiles, setProfiles] = useState<RobotProfileDetail[]>([])
  const [users, setUsers] = useState<AdminUser[]>([])
  const [mode, setMode] = useState<Mode>('create')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [form, setForm] = useState(blankForm)
  const [grantUserId, setGrantUserId] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [ok, setOk] = useState<string | null>(null)

  const load = useCallback(async () => {
    const [profRes, userRes] = await Promise.all([
      listRobotProfiles(),
      listUsers(),
    ])
    setProfiles(profRes.items)
    setUsers(userRes.items)
  }, [])

  useEffect(() => {
    void load().catch((err) => {
      setError(err instanceof Error ? err.message : 'Falha ao carregar')
    })
  }, [load])

  const startCreate = () => {
    setMode('create')
    setSelectedId(null)
    setForm(blankForm)
    setError(null)
    setOk(null)
  }

  const startEdit = (p: RobotProfileDetail) => {
    setMode('edit')
    setSelectedId(p.id)
    setForm(profileToForm(p))
    setGrantUserId('')
    setError(null)
    setOk(null)
  }

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setBusy(true)
    setError(null)
    setOk(null)
    try {
      const topics = parseJsonObject(form.topicsJson, 'topics')
      const frames = parseJsonObject(form.framesJson, 'frames')
      const capabilities = parseCapabilities(form.capabilities)

      if (mode === 'create') {
        if (!form.slug.trim()) throw new Error('slug obrigatório')
        await createRobotProfile({
          slug: form.slug.trim(),
          displayName: form.displayName.trim(),
          project: form.project.trim(),
          prefix: form.prefix.trim(),
          environment: form.environment,
          technology: form.technology.trim(),
          capabilities,
          topics,
          frames,
          rosbridgeUrl: form.rosbridgeUrl.trim(),
          videoBaseUrl: form.videoBaseUrl.trim(),
          active: form.active,
          grantAccessToCreator: true,
        })
        setOk('Profile criado.')
        startCreate()
      } else if (selectedId) {
        await patchRobotProfile(selectedId, {
          displayName: form.displayName.trim(),
          project: form.project.trim(),
          prefix: form.prefix.trim(),
          environment: form.environment,
          technology: form.technology.trim(),
          capabilities,
          topics,
          frames,
          rosbridgeUrl: form.rosbridgeUrl.trim(),
          videoBaseUrl: form.videoBaseUrl.trim(),
          active: form.active,
        })
        setOk('Profile atualizado.')
      }
      await load()
    } catch (err) {
      const msg =
        err instanceof ApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : 'Falha ao salvar'
      setError(msg)
    } finally {
      setBusy(false)
    }
  }

  const onGrant = async () => {
    if (!selectedId || !grantUserId) return
    setBusy(true)
    setError(null)
    setOk(null)
    try {
      await grantProfileAccess(selectedId, grantUserId)
      setOk('Acesso concedido.')
      setGrantUserId('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha no grant')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="admin-stack">
      <section className="panel">
        <div className="admin-row">
          <h2>Robot profiles</h2>
          <button type="button" className="btn-secondary" onClick={startCreate}>
            Novo
          </button>
        </div>
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Slug</th>
                <th>Nome</th>
                <th>Ativo</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {profiles.map((p) => (
                <tr key={p.id}>
                  <td className="mono">{p.slug}</td>
                  <td>{p.displayName}</td>
                  <td>{p.active ? 'sim' : 'não'}</td>
                  <td>
                    <button
                      type="button"
                      className="btn-ghost"
                      onClick={() => startEdit(p)}
                    >
                      Editar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel">
        <h2>{mode === 'create' ? 'Criar profile' : 'Editar profile'}</h2>
        <form className="admin-form" onSubmit={(e) => void onSubmit(e)}>
          {mode === 'create' ? (
            <label className="field">
              Slug
              <input
                value={form.slug}
                onChange={(e) => setForm({ ...form, slug: e.target.value })}
                required
              />
            </label>
          ) : (
            <p className="muted">
              Slug: <span className="mono">{form.slug}</span>
            </p>
          )}
          <label className="field">
            Display name
            <input
              value={form.displayName}
              onChange={(e) =>
                setForm({ ...form, displayName: e.target.value })
              }
              required
            />
          </label>
          <div className="admin-form-grid">
            <label className="field">
              Project
              <input
                value={form.project}
                onChange={(e) => setForm({ ...form, project: e.target.value })}
                required
              />
            </label>
            <label className="field">
              Prefix
              <input
                value={form.prefix}
                onChange={(e) => setForm({ ...form, prefix: e.target.value })}
                required
              />
            </label>
            <label className="field">
              Environment
              <select
                value={form.environment}
                onChange={(e) =>
                  setForm({ ...form, environment: e.target.value })
                }
              >
                <option value="sim">sim</option>
                <option value="physical">physical</option>
              </select>
            </label>
            <label className="field">
              Technology
              <input
                value={form.technology}
                onChange={(e) =>
                  setForm({ ...form, technology: e.target.value })
                }
                required
              />
            </label>
          </div>
          <label className="field">
            Capabilities (vírgulas ou JSON array)
            <input
              value={form.capabilities}
              onChange={(e) =>
                setForm({ ...form, capabilities: e.target.value })
              }
            />
          </label>
          <label className="field">
            Topics (JSON)
            <textarea
              className="admin-textarea mono"
              rows={6}
              value={form.topicsJson}
              onChange={(e) => setForm({ ...form, topicsJson: e.target.value })}
            />
          </label>
          <label className="field">
            Frames (JSON)
            <textarea
              className="admin-textarea mono"
              rows={4}
              value={form.framesJson}
              onChange={(e) => setForm({ ...form, framesJson: e.target.value })}
            />
          </label>
          <label className="field">
            Rosbridge URL
            <input
              value={form.rosbridgeUrl}
              onChange={(e) =>
                setForm({ ...form, rosbridgeUrl: e.target.value })
              }
              required
            />
          </label>
          <label className="field">
            Video base URL
            <input
              value={form.videoBaseUrl}
              onChange={(e) =>
                setForm({ ...form, videoBaseUrl: e.target.value })
              }
              required
            />
          </label>
          <label className="field admin-check">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Ativo
          </label>
          <button type="submit" className="btn-primary" disabled={busy}>
            {mode === 'create' ? 'Criar' : 'Salvar'}
          </button>
        </form>

        {mode === 'edit' && selectedId ? (
          <div className="admin-grant">
            <h3>Grant access</h3>
            <p className="muted">
              Concede ACL ao usuário (listar/revogar fora do escopo F).
            </p>
            <div className="admin-row">
              <label className="field admin-grant-select">
                Usuário
                <select
                  value={grantUserId}
                  onChange={(e) => setGrantUserId(e.target.value)}
                >
                  <option value="">—</option>
                  {users.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.username} ({u.role})
                    </option>
                  ))}
                </select>
              </label>
              <button
                type="button"
                className="btn-secondary"
                disabled={busy || !grantUserId}
                onClick={() => void onGrant()}
              >
                Grant
              </button>
            </div>
          </div>
        ) : null}

        {error ? (
          <p className="form-error" role="alert">
            {error}
          </p>
        ) : null}
        {ok ? <p className="form-ok">{ok}</p> : null}
      </section>
    </div>
  )
}
