import { LoginPage } from './components/LoginPage'
import { ConsolePage } from './components/ConsolePage'
import { GoalPosePanel } from './components/GoalPosePanel'
import { LlmPanel } from './components/LlmPanel'
import { ScanPanel } from './components/ScanPanel'
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
  const canSendGoal = Boolean(session?.permissions.canSendGoal)
  const rosEnabled =
    authState === 'authenticated' && (teleopEnabled || canSendGoal)
  const {
    status: rosStatus,
    error: rosError,
    stop,
    sendGoalPose,
    batteryPercent,
    scan,
  } = useRos(
    session?.activeProfile ?? null,
    rosEnabled,
    teleopEnabled ? (session?.limits.teleop ?? null) : null,
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
      batteryPercent={batteryPercent}
      onLogout={logout}
      onSelectProfile={selectProfile}
      onChangePreset={changeTeleopPreset}
      onStop={stop}
    >
      <GoalPosePanel
        session={session}
        rosConnected={rosStatus === 'connected'}
        onSendGoal={(x, y, yaw) =>
          sendGoalPose(x, y, yaw, session.activeProfile.frames.map ?? 'map')
        }
      />
      <ScanPanel session={session} scan={scan} />
      <LlmPanel session={session} />
    </ConsolePage>
  )
}
