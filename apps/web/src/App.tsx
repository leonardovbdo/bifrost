import { AdminApp } from './components/admin/AdminApp'
import { AdminForbidden } from './components/admin/AdminForbidden'
import { LoginPage } from './components/LoginPage'
import { ConsolePage } from './components/ConsolePage'
import { SessionHubPage } from './components/SessionHubPage'
import { useHashRoute } from './hooks/useHashRoute'
import { GoalPosePanel } from './components/GoalPosePanel'
import { LlmPanel } from './components/LlmPanel'
import { MapPanel } from './components/MapPanel'
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

  const route = useHashRoute()

  const isAdminRoute = route.kind === 'admin'
  const isConsoleRoute = route.kind === 'console'
  const teleopEnabled = Boolean(session?.permissions.canTeleop)
  const canSendGoal = Boolean(session?.permissions.canSendGoal)
  const canAskLlm =
    session?.user.role === 'admin' || session?.user.role === 'operator'

  const rosEnabled = authState === 'authenticated' && isConsoleRoute
  const rosTeleopLimits =
    !isConsoleRoute || !teleopEnabled ? null : (session?.limits.teleop ?? null)
  const rosCanSendGoal = isConsoleRoute && canSendGoal
  const {
    status: rosStatus,
    error: rosError,
    stop,
    sendGoalPose,
    batteryPercent,
    scan,
    mapGrid,
  } = useRos(
    session?.activeProfile ?? null,
    rosEnabled,
    rosTeleopLimits,
    rosCanSendGoal,
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

  if (isAdminRoute) {
    if (session.user.role !== 'admin') {
      return <AdminForbidden />
    }
    return (
      <AdminApp
        session={session}
        tab={route.tab}
        busy={busy}
        onLogout={logout}
      />
    )
  }

  if (isConsoleRoute) {
    return (
      <ConsolePage
        session={session}
        busy={busy}
        error={error}
        rosStatus={rosStatus}
        rosError={rosError}
        batteryPercent={batteryPercent}
        onLogout={logout}
        onChangePreset={changeTeleopPreset}
        onStop={stop}
        goalSlot={
          canSendGoal ? (
            <GoalPosePanel
              session={session}
              rosConnected={rosStatus === 'connected'}
              onSendGoal={(x, y, yaw) =>
                sendGoalPose(
                  x,
                  y,
                  yaw,
                  session.activeProfile.frames.map ?? 'map',
                )
              }
            />
          ) : null
        }
        mapSlot={
          <MapPanel session={session} rosStatus={rosStatus} mapGrid={mapGrid} />
        }
        scanSlot={
          <ScanPanel session={session} rosStatus={rosStatus} scan={scan} />
        }
        llmSlot={canAskLlm ? <LlmPanel session={session} /> : null}
      />
    )
  }

  return (
    <SessionHubPage
      session={session}
      busy={busy}
      error={error}
      onLogout={logout}
      onSelectProfile={selectProfile}
      onChangePreset={changeTeleopPreset}
    />
  )
}
