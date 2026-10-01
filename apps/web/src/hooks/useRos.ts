import { useCallback, useEffect, useRef, useState } from 'react'
import { Ros, Topic } from 'roslib'
import type { ActiveProfile, TeleopLimits } from '../types/session'

export type RosStatus = 'idle' | 'connecting' | 'connected' | 'error' | 'closed'

type Twist = {
  linear: { x: number; y: number; z: number }
  angular: { x: number; y: number; z: number }
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

  const disconnect = useCallback(() => {
    if (loopRef.current != null) {
      window.clearInterval(loopRef.current)
      loopRef.current = null
    }
    keysRef.current.clear()
    cmdVelRef.current = null
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
      if (topicName) {
        cmdVelRef.current = new Topic<Twist>({
          ros,
          name: topicName,
          messageType: 'geometry_msgs/Twist',
        })
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
      cmdVelRef.current.publish({
        linear: { x: lx, y: 0, z: 0 },
        angular: { x: 0, y: 0, z: az },
      })
    },
    [limits],
  )

  const stop = useCallback(() => publishTwist(0, 0), [publishTwist])

  useEffect(() => {
    if (!enabled || status !== 'connected' || !limits) return

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.repeat) return
      const key = e.key.toLowerCase()
      if (!['w', 'a', 's', 'd', 'arrowup', 'arrowdown', 'arrowleft', 'arrowright', ' '].includes(key)) {
        return
      }
      e.preventDefault()
      if (key === ' ') {
        keysRef.current.clear()
        stop()
        return
      }
      keysRef.current.add(key)
    }
    const onKeyUp = (e: KeyboardEvent) => {
      keysRef.current.delete(e.key.toLowerCase())
    }

    window.addEventListener('keydown', onKeyDown)
    window.addEventListener('keyup', onKeyUp)

    loopRef.current = window.setInterval(() => {
      const keys = keysRef.current
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
      if (loopRef.current != null) {
        window.clearInterval(loopRef.current)
        loopRef.current = null
      }
      stop()
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
