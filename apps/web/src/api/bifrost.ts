import { api } from './client'
import type {
  AdminUser,
  AuditEventRow,
  ParameterRow,
  RobotProfileDetail,
} from '../types/admin'
import type { LlmAskResponse, SessionConfig, User, UserRole } from '../types/session'

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

export function listRobotProfiles() {
  return api<{ items: RobotProfileDetail[] }>('/api/v1/robot-profiles')
}

export function getRobotProfile(id: string) {
  return api<RobotProfileDetail>(`/api/v1/robot-profiles/${id}`)
}

export type RobotProfileCreateBody = {
  slug: string
  displayName: string
  project: string
  prefix: string
  environment: string
  technology: string
  capabilities: string[]
  topics: Record<string, string>
  frames?: Record<string, string>
  rosbridgeUrl: string
  videoBaseUrl: string
  active?: boolean
  grantAccessToCreator?: boolean
}

export function createRobotProfile(body: RobotProfileCreateBody) {
  return api<RobotProfileDetail>('/api/v1/robot-profiles', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export type RobotProfilePatchBody = Partial<
  Omit<RobotProfileCreateBody, 'slug' | 'grantAccessToCreator'>
>

export function patchRobotProfile(id: string, body: RobotProfilePatchBody) {
  return api<RobotProfileDetail>(`/api/v1/robot-profiles/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  })
}

export function grantProfileAccess(profileId: string, userId: string) {
  return api<void>(`/api/v1/robot-profiles/${profileId}/access`, {
    method: 'POST',
    body: JSON.stringify({ userId }),
  })
}

export function listUsers() {
  return api<{ items: AdminUser[] }>('/api/v1/users')
}

export type CreateUserBody = {
  username: string
  password: string
  role: UserRole
  email?: string
  active?: boolean
}

export function createUser(body: CreateUserBody) {
  return api<AdminUser>('/api/v1/users', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export type PatchUserBody = {
  active?: boolean
  role?: UserRole
}

export function patchUser(id: string, body: PatchUserBody) {
  return api<AdminUser>(`/api/v1/users/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  })
}

export function listParameters(scope: 'global' | 'user') {
  return api<{ items: ParameterRow[] }>(
    `/api/v1/parameters?scope=${encodeURIComponent(scope)}`,
  )
}

export function upsertParameter(
  scope: 'global' | 'user',
  key: string,
  value: Record<string, unknown>,
) {
  return api<ParameterRow>('/api/v1/parameters', {
    method: 'PUT',
    body: JSON.stringify({ scope, key, value }),
  })
}

export function listAuditEvents(filters: {
  type?: string
  userId?: string
  limit?: number
}) {
  const params = new URLSearchParams()
  if (filters.type?.trim()) params.set('type', filters.type.trim())
  if (filters.userId?.trim()) params.set('userId', filters.userId.trim())
  if (filters.limit != null) params.set('limit', String(filters.limit))
  const qs = params.toString()
  return api<{ items: AuditEventRow[] }>(
    `/api/v1/audit/events${qs ? `?${qs}` : ''}`,
  )
}
