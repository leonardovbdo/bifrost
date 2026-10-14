import { useCallback, useEffect, useState } from 'react'
import { listRobotProfiles, listTelemetrySamples } from '../../api/bifrost'
import { stringifyJson } from '../../lib/jsonField'
import type { RobotProfileDetail, TelemetrySampleRow } from '../../types/admin'

export function AdminTelemetryPage() {
  const [samples, setSamples] = useState<TelemetrySampleRow[]>([])
  const [profiles, setProfiles] = useState<RobotProfileDetail[]>([])
  const [metricFilter, setMetricFilter] = useState('')
  const [profileIdFilter, setProfileIdFilter] = useState('')
  const [limit, setLimit] = useState(50)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadProfiles = useCallback(async () => {
    const res = await listRobotProfiles()
    setProfiles(res.items)
  }, [])

  const loadSamples = useCallback(async () => {
    setBusy(true)
    setError(null)
    try {
      const res = await listTelemetrySamples({
        metric: metricFilter || undefined,
        robotProfileId: profileIdFilter || undefined,
        limit,
      })
      setSamples(res.items)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao carregar samples')
    } finally {
      setBusy(false)
    }
  }, [metricFilter, profileIdFilter, limit])

  useEffect(() => {
    void loadProfiles().catch(() => {})
    void listTelemetrySamples({ limit: 50 })
      .then((res) => setSamples(res.items))
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Falha ao carregar samples')
      })
  }, [loadProfiles])

  return (
    <div className="admin-stack">
      <section className="panel">
        <h2>Telemetry samples</h2>
        <p className="muted">
          Amostras persistidas (battery/pose). Não inclui cmd_vel por tick.
        </p>
        <div className="admin-filters">
          <label className="field">
            Metric
            <input
              value={metricFilter}
              placeholder="battery | pose"
              onChange={(e) => setMetricFilter(e.target.value)}
            />
          </label>
          <label className="field">
            Profile
            <select
              value={profileIdFilter}
              onChange={(e) => setProfileIdFilter(e.target.value)}
            >
              <option value="">(todos)</option>
              {profiles.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.slug}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            Limit
            <input
              type="number"
              min={1}
              max={100}
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value) || 50)}
            />
          </label>
          <button
            type="button"
            className="btn-primary"
            disabled={busy}
            onClick={() => void loadSamples()}
          >
            Filtrar
          </button>
        </div>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>When</th>
                <th>Metric</th>
                <th>Profile</th>
                <th>Value</th>
              </tr>
            </thead>
            <tbody>
              {samples.map((row) => (
                <tr key={row.id}>
                  <td className="mono admin-ts">
                    {new Date(row.capturedAt).toLocaleString()}
                  </td>
                  <td>{row.metric}</td>
                  <td className="mono admin-id">
                    {profiles.find((p) => p.id === row.robotProfileId)?.slug ??
                      row.robotProfileId}
                  </td>
                  <td className="mono admin-payload">
                    {stringifyJson(row.value, 0)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {samples.length === 0 ? (
          <p className="muted">Nenhuma amostra com os filtros atuais.</p>
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
