import { useState } from 'react'
import type { FormEvent } from 'react'
import { postGoalAudit } from '../api/bifrost'
import type { SessionConfig } from '../types/session'

interface GoalPosePanelProps {
  session: SessionConfig
  rosConnected: boolean
  onSendGoal: (x: number, y: number, yaw: number) => void
}

export function GoalPosePanel({ session, rosConnected, onSendGoal }: GoalPosePanelProps) {
  const canSend = session.permissions.canSendGoal
  const topic = session.activeProfile.topics.goal_pose
  const frameId = session.activeProfile.frames.map ?? 'map'

  const [x, setX] = useState('0')
  const [y, setY] = useState('0')
  const [yaw, setYaw] = useState('0')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [ok, setOk] = useState<string | null>(null)

  if (!canSend) {
    return (
      <section className="panel">
        <h2>Goal (Nav2)</h2>
        <p className="muted">Seu papel não pode enviar goal_pose.</p>
      </section>
    )
  }

  if (!topic) {
    return (
      <section className="panel">
        <h2>Goal (Nav2)</h2>
        <p className="muted">Profile sem tópico goal_pose.</p>
      </section>
    )
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setOk(null)
    const nx = Number(x)
    const ny = Number(y)
    const nyaw = Number(yaw)
    if (![nx, ny, nyaw].every((n) => Number.isFinite(n))) {
      setError('x, y e yaw devem ser números finitos')
      return
    }
    if (!rosConnected) {
      setError('Rosbridge desconectado')
      return
    }
    setBusy(true)
    try {
      onSendGoal(nx, ny, nyaw)
      await postGoalAudit({ x: nx, y: ny, yaw: nyaw })
      setOk(`Goal enviado (${frameId}): x=${nx} y=${ny} yaw=${nyaw}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao enviar goal')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="panel">
      <h2>Goal (Nav2)</h2>
      <p className="muted mono">{topic}</p>
      <p className="hint">frame: {frameId} · PoseStamped + audit goal_pose</p>
      <form className="goal-form" onSubmit={(e) => void handleSubmit(e)}>
        <label className="field">
          x
          <input
            value={x}
            onChange={(e) => setX(e.target.value)}
            inputMode="decimal"
            disabled={busy}
          />
        </label>
        <label className="field">
          y
          <input
            value={y}
            onChange={(e) => setY(e.target.value)}
            inputMode="decimal"
            disabled={busy}
          />
        </label>
        <label className="field">
          yaw (rad)
          <input
            value={yaw}
            onChange={(e) => setYaw(e.target.value)}
            inputMode="decimal"
            disabled={busy}
          />
        </label>
        <button type="submit" className="btn-secondary" disabled={busy || !rosConnected}>
          {busy ? 'Enviando…' : 'Enviar goal'}
        </button>
      </form>
      {error ? <p className="form-error">{error}</p> : null}
      {ok ? <p className="form-ok">{ok}</p> : null}
    </section>
  )
}
