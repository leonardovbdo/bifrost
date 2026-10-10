import type { ActiveProfile, UserRole } from './session'

export type RobotProfileDetail = ActiveProfile

export interface AdminUser {
  id: string
  username: string
  email: string | null
  role: UserRole
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface ParameterRow {
  id: string
  scope: string
  scopeId: string | null
  key: string
  value: Record<string, unknown>
}

export interface AuditEventRow {
  id: string
  userId: string | null
  type: string
  robotProfileId: string | null
  payload: Record<string, unknown>
  createdAt: string
}

export type AdminTab = 'profiles' | 'parameters' | 'users' | 'audit'
