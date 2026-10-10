import { useCallback, useEffect, useState } from 'react'
import { createUser, listUsers, patchUser } from '../../api/bifrost'
import { ApiError } from '../../api/client'
import type { AdminUser } from '../../types/admin'
import type { UserRole } from '../../types/session'

const ROLES: UserRole[] = ['admin', 'operator', 'viewer']

export function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [ok, setOk] = useState<string | null>(null)

  const [createForm, setCreateForm] = useState({
    username: '',
    password: '',
    role: 'operator' as UserRole,
    email: '',
    active: true,
  })

  const load = useCallback(async () => {
    const res = await listUsers()
    setUsers(res.items)
  }, [])

  useEffect(() => {
    void load().catch((err) => {
      setError(err instanceof Error ? err.message : 'Falha ao carregar')
    })
  }, [load])

  const onCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    setBusy(true)
    setError(null)
    setOk(null)
    try {
      await createUser({
        username: createForm.username.trim(),
        password: createForm.password,
        role: createForm.role,
        email: createForm.email.trim() || undefined,
        active: createForm.active,
      })
      setOk('Usuário criado.')
      setCreateForm({
        username: '',
        password: '',
        role: 'operator',
        email: '',
        active: true,
      })
      await load()
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : 'Falha ao criar',
      )
    } finally {
      setBusy(false)
    }
  }

  const updateUser = async (
    id: string,
    patch: { active?: boolean; role?: UserRole },
  ) => {
    setBusy(true)
    setError(null)
    setOk(null)
    try {
      await patchUser(id, patch)
      setOk('Usuário atualizado.')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao atualizar')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="admin-stack">
      <section className="panel">
        <h2>Usuários</h2>
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Role</th>
                <th>Ativo</th>
                <th>Email</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.username}</td>
                  <td>
                    <select
                      value={u.role}
                      disabled={busy}
                      onChange={(e) =>
                        void updateUser(u.id, {
                          role: e.target.value as UserRole,
                        })
                      }
                    >
                      {ROLES.map((r) => (
                        <option key={r} value={r}>
                          {r}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <input
                      type="checkbox"
                      checked={u.active}
                      disabled={busy}
                      onChange={(e) =>
                        void updateUser(u.id, { active: e.target.checked })
                      }
                    />
                  </td>
                  <td className="muted">{u.email ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel">
        <h2>Criar usuário</h2>
        <form className="admin-form" onSubmit={(e) => void onCreate(e)}>
          <label className="field">
            Username
            <input
              value={createForm.username}
              onChange={(e) =>
                setCreateForm({ ...createForm, username: e.target.value })
              }
              required
            />
          </label>
          <label className="field">
            Password
            <input
              type="password"
              value={createForm.password}
              onChange={(e) =>
                setCreateForm({ ...createForm, password: e.target.value })
              }
              required
            />
          </label>
          <label className="field">
            Role
            <select
              value={createForm.role}
              onChange={(e) =>
                setCreateForm({
                  ...createForm,
                  role: e.target.value as UserRole,
                })
              }
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            Email (opcional)
            <input
              value={createForm.email}
              onChange={(e) =>
                setCreateForm({ ...createForm, email: e.target.value })
              }
            />
          </label>
          <label className="field admin-check">
            <input
              type="checkbox"
              checked={createForm.active}
              onChange={(e) =>
                setCreateForm({ ...createForm, active: e.target.checked })
              }
            />
            Ativo
          </label>
          <button type="submit" className="btn-primary" disabled={busy}>
            Criar
          </button>
        </form>
      </section>

      {error ? (
        <p className="form-error" role="alert">
          {error}
        </p>
      ) : null}
      {ok ? <p className="form-ok">{ok}</p> : null}
    </div>
  )
}
