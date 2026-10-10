import { api } from './client'
import type { LlmAskResponse, SessionConfig, User } from '../types/session'

export function login(username: string, password: string) {
  return api<{ user: User }>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function refresh() {
  return api<{ user: User }>('/api/v1/auth/refresh', { method: 'POST' })
}

export function logout() {
  return api<void>('/api/v1/auth/logout', { method: 'POST' })
}

export function fetchMe() {
  return api<User>('/api/v1/me')
}

export function fetchSessionConfig() {
  return api<SessionConfig>('/api/v1/me/session-config')
}

export function switchActiveProfile(profileId: string) {
  return api<void>('/api/v1/me/active-profile', {
    method: 'PUT',
    body: JSON.stringify({ profileId }),
  })
}

export function setTeleopPreset(preset: string) {
  return api<unknown>('/api/v1/parameters', {
    method: 'PUT',
    body: JSON.stringify({
      scope: 'user',
      key: 'teleop.activeProfile',
      value: { value: preset },
    }),
  })
}

export function askLlm(prompt: string, context?: Record<string, unknown>) {
  return api<LlmAskResponse>('/api/v1/llm/ask', {
    method: 'POST',
    body: JSON.stringify({ prompt, context }),
  })
}

export function postGoalAudit(
  payload: Record<string, unknown>,
  robotProfileId?: string,
) {
  return api<unknown>('/api/v1/audit/events', {
    method: 'POST',
    body: JSON.stringify({
      type: 'goal_pose',
      robotProfileId: robotProfileId ?? null,
      payload,
    }),
  })
}
