import { useCallback, useEffect, useState } from 'react'
import {
  fetchSessionConfig,
  listParameters,
  upsertParameter,
} from '../../api/bifrost'
import { ApiError } from '../../api/client'
import { parseJsonValue, stringifyJson } from '../../lib/jsonField'
import type { SessionConfig } from '../../types/session'

const PRESET_KEY = 'teleop.presets'
const ACTIVE_KEY = 'teleop.activeProfile'

function findValue(
  items: { key: string; value: Record<string, unknown> }[],
  key: string,
): Record<string, unknown> | undefined {
  return items.find((p) => p.key === key)?.value
}

export function AdminParametersPage() {
  const [presetsJson, setPresetsJson] = useState('')
  const [globalActive, setGlobalActive] = useState('normal')
  const [userActive, setUserActive] = useState<string | null>(null)
  const [mergePreview, setMergePreview] = useState<SessionConfig['limits']['teleop'] | null>(
    null,
  )
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [ok, setOk] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    const [globalRes, userRes, session] = await Promise.all([
      listParameters('global'),
      listParameters('user'),
      fetchSessionConfig(),
    ])
    const presets = findValue(globalRes.items, PRESET_KEY)
    setPresetsJson(presets ? stringifyJson(presets) : '{}')
    const gActive = findValue(globalRes.items, ACTIVE_KEY)
    const gVal = gActive?.value ?? gActive?.profile
    setGlobalActive(typeof gVal === 'string' ? gVal : 'normal')
    const uActive = findValue(userRes.items, ACTIVE_KEY)
    const uVal = uActive?.value ?? uActive?.profile
    setUserActive(typeof uVal === 'string' ? uVal : null)
    setMergePreview(session.limits.teleop)
  }, [])

  useEffect(() => {
    void refresh().catch((err) => {
      setError(err instanceof Error ? err.message : 'Falha ao carregar')
    })
  }, [refresh])

  const savePresets = async () => {
    setBusy(true)
    setError(null)
    setOk(null)
    try {
      const value = parseJsonValue(presetsJson, PRESET_KEY)
      await upsertParameter('global', PRESET_KEY, value)
      setOk('Presets globais salvos.')
      await refresh()
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : 'Falha ao salvar presets',
      )
    } finally {
      setBusy(false)
    }
  }

  const saveGlobalActive = async () => {
    setBusy(true)
    setError(null)
    setOk(null)
    try {
      await upsertParameter('global', ACTIVE_KEY, { value: globalActive })
      setOk('Default global atualizado.')
      await refresh()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao salvar default')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="admin-stack">
      <section className="panel">
        <h2>Global teleop.presets</h2>
        <p className="muted">
          Admin edita presets globais; merge efetivo aparece abaixo (user override
          de activeProfile quando existir).
        </p>
        <label className="field">
          JSON (safety / normal / fast)
          <textarea
            className="admin-textarea mono"
            rows={12}
            value={presetsJson}
            onChange={(e) => setPresetsJson(e.target.value)}
          />
        </label>
        <button
          type="button"
          className="btn-primary"
          disabled={busy}
          onClick={() => void savePresets()}
        >
          Salvar presets
        </button>
      </section>

      <section className="panel">
        <h2>Global default preset</h2>
        <label className="field">
          teleop.activeProfile (global)
          <select
            value={globalActive}
            onChange={(e) => setGlobalActive(e.target.value)}
          >
            <option value="safety">safety</option>
            <option value="normal">normal</option>
            <option value="fast">fast</option>
          </select>
        </label>
        <button
          type="button"
          className="btn-secondary"
          disabled={busy}
          onClick={() => void saveGlobalActive()}
        >
          Salvar default
        </button>
      </section>

      <section className="panel">
        <h2>Merge preview (sua sessão)</h2>
        <dl className="meta-list">
          <div>
            <dt>User override</dt>
            <dd>{userActive ?? '(nenhum — usa global)'}</dd>
          </div>
          <div>
            <dt>limits.teleop.profile</dt>
            <dd>{mergePreview?.profile ?? '—'}</dd>
          </div>
          <div>
            <dt>linearMax</dt>
            <dd>{mergePreview?.linearMax?.toFixed(2) ?? '—'}</dd>
          </div>
          <div>
            <dt>angularMax</dt>
            <dd>{mergePreview?.angularMax?.toFixed(2) ?? '—'}</dd>
          </div>
        </dl>
        <button
          type="button"
          className="btn-ghost"
          disabled={busy}
          onClick={() => void refresh()}
        >
          Atualizar preview
        </button>
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
