import { useCallback, useEffect, useRef, useState } from 'react'
import { Ros, Topic } from 'roslib'
import type { ActiveProfile, TeleopLimits } from '../types/session'

export type RosStatus = 'idle' | 'connecting' | 'connected' | 'error' | 'closed'

type Twist = {
  linear: { x: number; y: number; z: number }
  angular: { x: number; y: number; z: number }
}

function asStatusMessage(message: unknown): {
  level?: string
  msg?: string
  id?: string
} | null {
  if (!message || typeof message !== 'object') return null
  const m = message as Record<string, unknown>
  return {
    level: typeof m.level === 'string' ? m.level : undefined,
    msg: typeof m.msg === 'string' ? m.msg : undefined,
    id: typeof m.id === 'string' ? m.id : undefined,
  }
}

const TELEOP_KEYS = new Set([
  'w',
  'a',
  's',
  'd',
  'arrowup',
  'arrowdown',
  'arrowleft',
  'arrowright',
  ' ',
])

/** ROS 2 / Jazzy message type (rosbridge). */
const TWIST_TYPE = 'geometry_msgs/msg/Twist'

function isEditableTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false
  const tag = target.tagName
  return (
    tag === 'INPUT' ||
    tag === 'TEXTAREA' ||
    tag === 'SELECT' ||
    target.isContentEditable
  )
}

export function useRos(
  profile: ActiveProfile | null,
  enabled: boolean,
  limits: TeleopLimits | null,
) {
  const [status, setStatus] = useState<RosStatus>('idle')
  const [error, setError] = useState<string | null>(null)
  const rosRef = useRef<Ros | null>(null)
  const cmdVelRef = useRef<Topic<Twist> | null>(null)
  const keysRef = useRef<Set<string>>(new Set())
  const loopRef = useRef<number | null>(null)
  const hadKeysRef = useRef(false)
  const statusHandlerRef = useRef<((message: unknown) => void) | null>(null)
  const statusEventRef = useRef<string | null>(null)
  const advertiseIdRef = useRef<string | null>(null)

  const disconnect = useCallback(() => {
    if (loopRef.current != null) {
      window.clearInterval(loopRef.current)
      loopRef.current = null
    }
    keysRef.current.clear()
    hadKeysRef.current = false
    if (rosRef.current && statusHandlerRef.current) {
      rosRef.current.off('status', statusHandlerRef.current)
      if (statusEventRef.current) {
        rosRef.current.off(statusEventRef.current, statusHandlerRef.current)
      }
    }
    statusHandlerRef.current = null
    statusEventRef.current = null
    advertiseIdRef.current = null
    if (cmdVelRef.current) {
      try {
        cmdVelRef.current.unadvertise()
      } catch {
        // ignore
      }
      cmdVelRef.current = null
    }
    if (rosRef.current) {
      try {
        rosRef.current.close()
      } catch {
        // ignore
      }
      rosRef.current = null
    }
    setStatus('idle')
  }, [])

  useEffect(() => {
    if (!enabled || !profile?.rosbridgeUrl) {
      disconnect()
      return
    }

    setStatus('connecting')
    setError(null)
    const ros = new Ros({ url: profile.rosbridgeUrl })
    rosRef.current = ros

    const onConnection = () => {
      setStatus('connected')
      const topicName = profile.topics.cmd_vel
      if (!topicName) {
        setError('Profile sem tópico cmd_vel')
        return
      }
      const topic = new Topic<Twist>({
        ros,
        name: topicName,
        messageType: TWIST_TYPE,
      })
      topic.advertise()
      cmdVelRef.current = topic
      advertiseIdRef.current = topic.advertiseId ?? null

      const onRosStatus = (message: unknown) => {
        const statusMsg = asStatusMessage(message)
        if (!statusMsg) return
        const level = statusMsg.level?.toLowerCase()
        if (level !== 'error' && level !== 'warning') return
        const advertiseId = advertiseIdRef.current
        if (statusMsg.id && advertiseId && statusMsg.id !== advertiseId) return
        setError(
          statusMsg.msg?.trim() ||
            'rosbridge recusou cmd_vel (verifique messageType ROS 2)',
        )
      }
      statusHandlerRef.current = onRosStatus
      ros.on('status', onRosStatus)
      if (topic.advertiseId) {
        statusEventRef.current = `status:${topic.advertiseId}`
        ros.on(statusEventRef.current, onRosStatus)
      }
    }
    const onError = () => {
      setStatus('error')
      setError('Falha ao conectar no rosbridge')
    }
    const onClose = () => setStatus('closed')

    ros.on('connection', onConnection)
    ros.on('error', onError)
    ros.on('close', onClose)

    return () => {
      ros.off('connection', onConnection)
      ros.off('error', onError)
      ros.off('close', onClose)
      disconnect()
    }
  }, [enabled, profile?.id, profile?.rosbridgeUrl, profile?.topics.cmd_vel, disconnect])

  const publishTwist = useCallback(
    (linearX: number, angularZ: number) => {
      if (!cmdVelRef.current || !limits) return
      const lx = Math.max(-limits.linearMax, Math.min(limits.linearMax, linearX))
      const az = Math.max(-limits.angularMax, Math.min(limits.angularMax, angularZ))
      try {
        cmdVelRef.current.publish({
          linear: { x: lx, y: 0, z: 0 },
          angular: { x: 0, y: 0, z: az },
        })
      } catch {
        setError('Falha ao publicar Twist no rosbridge')
      }
    },
    [limits],
  )

  const stop = useCallback(() => publishTwist(0, 0), [publishTwist])

  useEffect(() => {
    if (!enabled || status !== 'connected' || !limits) return

    const releaseKeys = () => {
      if (keysRef.current.size === 0 && !hadKeysRef.current) return
      keysRef.current.clear()
      hadKeysRef.current = false
      stop()
    }

    const onKeyDown = (e: KeyboardEvent) => {
      if (isEditableTarget(e.target)) return
      if (e.repeat) return
      const key = e.key.toLowerCase()
      if (!TELEOP_KEYS.has(key)) return
      e.preventDefault()
      if (key === ' ') {
        releaseKeys()
        return
      }
      keysRef.current.add(key)
      hadKeysRef.current = true
    }
    // Always honor keyup: the press may have started outside an input.
    const onKeyUp = (e: KeyboardEvent) => {
      const key = e.key.toLowerCase()
      if (!TELEOP_KEYS.has(key) && key !== ' ') return
      keysRef.current.delete(key)
      if (keysRef.current.size === 0 && hadKeysRef.current) {
        hadKeysRef.current = false
        stop()
      }
    }
    const onFocusIn = (e: FocusEvent) => {
      if (isEditableTarget(e.target)) releaseKeys()
    }
    const onBlur = () => releaseKeys()
    const onVisibility = () => {
      if (document.visibilityState === 'hidden') releaseKeys()
    }

    window.addEventListener('keydown', onKeyDown)
    window.addEventListener('keyup', onKeyUp)
    window.addEventListener('blur', onBlur)
    document.addEventListener('focusin', onFocusIn)
    document.addEventListener('visibilitychange', onVisibility)

    loopRef.current = window.setInterval(() => {
      const keys = keysRef.current
      if (keys.size === 0) return
      let linear = 0
      let angular = 0
      if (keys.has('w') || keys.has('arrowup')) linear += limits.linearMax
      if (keys.has('s') || keys.has('arrowdown')) linear -= limits.linearMax
      if (keys.has('a') || keys.has('arrowleft')) angular += limits.angularMax
      if (keys.has('d') || keys.has('arrowright')) angular -= limits.angularMax
      publishTwist(linear, angular)
    }, 100)

    return () => {
      window.removeEventListener('keydown', onKeyDown)
      window.removeEventListener('keyup', onKeyUp)
      window.removeEventListener('blur', onBlur)
      document.removeEventListener('focusin', onFocusIn)
      document.removeEventListener('visibilitychange', onVisibility)
      if (loopRef.current != null) {
        window.clearInterval(loopRef.current)
        loopRef.current = null
      }
      releaseKeys()
    }
  }, [enabled, status, limits, publishTwist, stop])

  return { status, error, publishTwist, stop }
}

export function cameraStreamUrl(profile: ActiveProfile | null): string | null {
  if (!profile?.videoBaseUrl) return null
  const topic =
    profile.topics.camera_link ??
    profile.topics.camera_user ??
    Object.entries(profile.topics).find(([k]) => k.startsWith('camera'))?.[1]
  if (!topic) return null
  const base = profile.videoBaseUrl.replace(/\/$/, '')
  return `${base}/stream?topic=${encodeURIComponent(topic)}`
}
