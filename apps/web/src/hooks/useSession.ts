import { useCallback, useEffect, useState } from 'react'
import {
  fetchSessionConfig,
  login as apiLogin,
  logout as apiLogout,
  refresh,
  setTeleopPreset,
  switchActiveProfile,
} from '../api/bifrost'
import { ApiError } from '../api/client'
import type { SessionConfig, User } from '../types/session'

type AuthState = 'loading' | 'anonymous' | 'authenticated'

/** Dedupes StrictMode double-mount refresh rotation. */
let inflightRefresh: Promise<unknown> | null = null

function refreshShared() {
  if (!inflightRefresh) {
    inflightRefresh = refresh().finally(() => {
      inflightRefresh = null
    })
  }
  return inflightRefresh
}

export function useSession() {
  const [authState, setAuthState] = useState<AuthState>('loading')
  const [user, setUser] = useState<User | null>(null)
  const [session, setSession] = useState<SessionConfig | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const clearSession = useCallback(() => {
    setUser(null)
    setSession(null)
    setAuthState('anonymous')
  }, [])

  const loadSession = useCallback(async () => {
    const config = await fetchSessionConfig()
    setSession(config)
    setUser(config.user)
    setAuthState('authenticated')
    setError(null)
    return config
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        await loadSession()
      } catch (err) {
        if (cancelled) return

        if (err instanceof ApiError && err.status === 401) {
          try {
            if (cancelled) return
            await refreshShared()
            if (cancelled) return
            await loadSession()
            return
          } catch {
            if (!cancelled) {
              clearSession()
            }
            return
          }
        }

        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : 'Falha ao carregar sessão',
          )
          clearSession()
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [loadSession, clearSession])

  const login = useCallback(
    async (username: string, password: string) => {
      setBusy(true)
      setError(null)
      try {
        await apiLogin(username, password)
        await loadSession()
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Falha no login')
        setAuthState('anonymous')
        throw err
      } finally {
        setBusy(false)
      }
    },
    [loadSession],
  )

  const logout = useCallback(async () => {
    setBusy(true)
    try {
      await apiLogout()
    } finally {
      clearSession()
      setBusy(false)
    }
  }, [clearSession])

  const selectProfile = useCallback(
    async (profileId: string) => {
      setBusy(true)
      setError(null)
      try {
        await switchActiveProfile(profileId)
        await loadSession()
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Falha ao trocar profile')
        throw err
      } finally {
        setBusy(false)
      }
    },
    [loadSession],
  )

  const changeTeleopPreset = useCallback(
    async (preset: string) => {
      setBusy(true)
      setError(null)
      try {
        await setTeleopPreset(preset)
        await loadSession()
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Falha ao alterar preset')
        throw err
      } finally {
        setBusy(false)
      }
    },
    [loadSession],
  )

  return {
    authState,
    user,
    session,
    error,
    busy,
    login,
    logout,
    selectProfile,
    changeTeleopPreset,
    reload: loadSession,
  }
}
