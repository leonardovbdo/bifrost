export type UserRole = 'admin' | 'operator' | 'viewer'

export interface User {
  id: string
  username: string
  role: UserRole
}

export interface ProfileSummary {
  id: string
  slug: string
  displayName: string
}

export interface ActiveProfile {
  id: string
  slug: string
  displayName: string
  project: string
  prefix: string
  environment: string
  technology: string
  capabilities: string[]
  topics: Record<string, string>
  frames: Record<string, string>
  rosbridgeUrl: string
  videoBaseUrl: string
  active: boolean
}

export interface Permissions {
  canTeleop: boolean
  canSendGoal: boolean
  canInspectRosapi: boolean
  canManageProfiles: boolean
}

export interface TeleopLimits {
  linearMax: number
  angularMax: number
  profile: string
}

export interface SessionConfig {
  schemaVersion: number
  user: User
  activeProfile: ActiveProfile
  allowedProfiles: ProfileSummary[]
  permissions: Permissions
  limits: { teleop: TeleopLimits }
}

export interface LlmAskResponse {
  reply: string
  model: string
}
