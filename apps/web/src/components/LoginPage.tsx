import { useState } from 'react'
import type { FormEvent } from 'react'

interface LoginPageProps {
  onLogin: (username: string, password: string) => Promise<void>
  busy: boolean
  error: string | null
}

export function LoginPage({ onLogin, busy, error }: LoginPageProps) {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('')
  const [localError, setLocalError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setLocalError(null)
    try {
      await onLogin(username.trim(), password)
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : 'Falha no login')
    }
  }

  const message = localError ?? error

  return (
    <main className="login-shell">
      <div className="login-atmosphere" aria-hidden="true" />
      <section className="login-panel">
        <p className="brand-mark">Bifrost</p>
        <h1 className="login-title">Console NARA</h1>
        <p className="login-lead">
          Ponte governada entre operadores e a simulação — sessão via API, teleop no browser.
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            Usuário
            <input
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </label>
          <label>
            Senha
            <input
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          {message ? <p className="form-error" role="alert">{message}</p> : null}
          <button type="submit" className="btn-primary" disabled={busy}>
            {busy ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
      </section>
    </main>
  )
}
