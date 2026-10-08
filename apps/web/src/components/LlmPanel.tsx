import { useState } from 'react'
import type { FormEvent } from 'react'
import { askLlm } from '../api/bifrost'
import type { SessionConfig } from '../types/session'

interface LlmPanelProps {
  session: SessionConfig
}

export function LlmPanel({ session }: LlmPanelProps) {
  const canAsk =
    session.user.role === 'admin' || session.user.role === 'operator'
  const [prompt, setPrompt] = useState('')
  const [reply, setReply] = useState<string | null>(null)
  const [model, setModel] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  if (!canAsk) {
    return (
      <section className="panel">
        <h2>Assistente</h2>
        <p className="muted">Disponível para admin e operator.</p>
      </section>
    )
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!prompt.trim()) return
    setBusy(true)
    setError(null)
    try {
      const result = await askLlm(prompt.trim(), {
        profileSlug: session.activeProfile.slug,
        teleopProfile: session.limits.teleop.profile,
      })
      setReply(result.reply)
      setModel(result.model)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha no LLM')
      setReply(null)
      setModel(null)
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="panel llm-panel">
      <h2>Assistente</h2>
      <p className="muted">Proxy Bifrost (`/llm/ask`) — chave só no servidor.</p>
      <form onSubmit={handleSubmit} className="llm-form">
        <textarea
          rows={3}
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Ex.: quais limites de teleop estão ativos?"
          required
        />
        <button type="submit" className="btn-secondary" disabled={busy}>
          {busy ? 'Consultando…' : 'Perguntar'}
        </button>
      </form>
      {error ? <p className="form-error">{error}</p> : null}
      {reply ? (
        <div className="llm-reply">
          <p>{reply}</p>
          {model ? <span className="chip">{model}</span> : null}
        </div>
      ) : null}
    </section>
  )
}
