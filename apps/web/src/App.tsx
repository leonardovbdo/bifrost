import { LoginPage } from './components/LoginPage'
import { ConsolePage } from './components/ConsolePage'
import { LlmPanel } from './components/LlmPanel'
import { useRos } from './hooks/useRos'
import { useSession } from './hooks/useSession'
import './styles.css'

export default function App() {
  const {
    authState,
    session,
    error,
    busy,
    login,
    logout,
    selectProfile,
    changeTeleopPreset,
  } = useSession()

  const teleopEnabled = Boolean(session?.permissions.canTeleop)
  const { status: rosStatus, error: rosError, stop } = useRos(
    session?.activeProfile ?? null,
    authState === 'authenticated' && teleopEnabled,
    session?.limits.teleop ?? null,
  )

  if (authState === 'loading') {
    return (
      <main className="boot-shell">
        <p className="brand-mark">Bifrost</p>
        <p className="muted">Carregando sessão…</p>
      </main>
    )
  }

  if (authState !== 'authenticated' || !session) {
    return <LoginPage onLogin={login} busy={busy} error={error} />
  }

  return (
    <ConsolePage
      session={session}
      busy={busy}
      error={error}
      rosStatus={rosStatus}
      rosError={rosError}
      onLogout={logout}
      onSelectProfile={selectProfile}
      onChangePreset={changeTeleopPreset}
      onStop={stop}
    >
      <LlmPanel session={session} />
    </ConsolePage>
  )
}
