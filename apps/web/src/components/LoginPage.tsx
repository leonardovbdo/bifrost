import { useState } from 'react'
import type { FormEvent } from 'react'
import { UiHint } from './UiHint'

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
        <h1 className="login-title">Entrar</h1>
        <p className="login-lead">
          Ponte governada entre operadores e o robô. A sessão define o que você
          pode fazer antes de qualquer conexão ROS.
        </p>
        <UiHint id="login">
          Tela de identidade: autentica na API Bifrost (JWT em cookie). Depois
          disso você cai no hub de sessão — ainda sem rosbridge/câmera.
        </UiHint>

        <form className="login-form" onSubmit={handleSubmit} data-testid="login-form">
          <label>
            Usuário
            <input
              autoComplete="username"
              name="username"
              data-testid="login-username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </label>
          <label>
            Senha
            <input
              type="password"
              name="password"
              data-testid="login-password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          {message ? <p className="form-error" role="alert">{message}</p> : null}
          <button
            type="submit"
            className="btn-primary"
            disabled={busy}
            data-testid="login-submit"
          >
            {busy ? 'Entrando…' : 'Continuar'}
          </button>
        </form>
      </section>
    </main>
  )
}
