import { useCallback, useEffect, useState } from 'react'
import { listAuditEvents, listUsers } from '../../api/bifrost'
import { stringifyJson } from '../../lib/jsonField'
import type { AdminUser, AuditEventRow } from '../../types/admin'

export function AdminAuditPage() {
  const [events, setEvents] = useState<AuditEventRow[]>([])
  const [users, setUsers] = useState<AdminUser[]>([])
  const [typeFilter, setTypeFilter] = useState('')
  const [userIdFilter, setUserIdFilter] = useState('')
  const [limit, setLimit] = useState(50)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadUsers = useCallback(async () => {
    const res = await listUsers()
    setUsers(res.items)
  }, [])

  const loadEvents = useCallback(async () => {
    setBusy(true)
    setError(null)
    try {
      const res = await listAuditEvents({
        type: typeFilter || undefined,
        userId: userIdFilter || undefined,
        limit,
      })
      setEvents(res.items)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao carregar audit')
    } finally {
      setBusy(false)
    }
  }, [typeFilter, userIdFilter, limit])

  useEffect(() => {
    void loadUsers().catch(() => {})
    void listAuditEvents({ limit: 50 })
      .then((res) => setEvents(res.items))
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Falha ao carregar audit')
      })
  }, [loadUsers])

  return (
    <div className="admin-stack">
      <section className="panel">
        <h2>Audit events</h2>
        <div className="admin-filters">
          <label className="field">
            Type
            <input
              value={typeFilter}
              placeholder="ex. goal_pose, login_failure"
              onChange={(e) => setTypeFilter(e.target.value)}
            />
          </label>
          <label className="field">
            User
            <select
              value={userIdFilter}
              onChange={(e) => setUserIdFilter(e.target.value)}
            >
              <option value="">(todos)</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.username}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            Limit
            <input
              type="number"
              min={1}
              max={200}
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value) || 50)}
            />
          </label>
          <button
            type="button"
            className="btn-primary"
            disabled={busy}
            onClick={() => void loadEvents()}
          >
            Filtrar
          </button>
        </div>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>When</th>
                <th>Type</th>
                <th>User</th>
                <th>Profile</th>
                <th>Payload</th>
              </tr>
            </thead>
            <tbody>
              {events.map((ev) => (
                <tr key={ev.id}>
                  <td className="mono admin-ts">
                    {new Date(ev.createdAt).toLocaleString()}
                  </td>
                  <td>{ev.type}</td>
                  <td className="mono admin-id">
                    {users.find((u) => u.id === ev.userId)?.username ??
                      ev.userId ??
                      '—'}
                  </td>
                  <td className="mono admin-id">{ev.robotProfileId ?? '—'}</td>
                  <td className="mono admin-payload">
                    {stringifyJson(ev.payload, 0)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {events.length === 0 ? (
          <p className="muted">Nenhum evento com os filtros atuais.</p>
        ) : null}
      </section>

      {error ? (
        <p className="form-error" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  )
}
