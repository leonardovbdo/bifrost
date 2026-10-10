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

/** ROS 2 / Jazzy message types (rosbridge). */
const TWIST_TYPE = 'geometry_msgs/msg/Twist'
const POSE_STAMPED_TYPE = 'geometry_msgs/msg/PoseStamped'
const BATTERY_STATE_TYPE = 'sensor_msgs/msg/BatteryState'
const LASER_SCAN_TYPE = 'sensor_msgs/msg/LaserScan'
const OCCUPANCY_GRID_TYPE = 'nav_msgs/msg/OccupancyGrid'

type PoseStamped = {
  header: { stamp: { sec: number; nanosec: number }; frame_id: string }
  pose: {
    position: { x: number; y: number; z: number }
    orientation: { x: number; y: number; z: number; w: number }
  }
}

export type LaserScanSample = {
  angleMin: number
  angleIncrement: number
  ranges: number[]
  rangeMax: number
}

export type OccupancyGridSample = {
  width: number
  height: number
  resolution: number
  data: number[]
}

type BatteryStateMsg = { percentage?: number }
type LaserScanMsg = {
  angle_min?: number
  angle_increment?: number
  range_max?: number
  ranges?: number[]
}
type OccupancyGridMsg = {
  info?: { width?: number; height?: number; resolution?: number }
  data?: number[]
}

function yawToQuat(yaw: number): { x: number; y: number; z: number; w: number } {
  const half = yaw / 2
  return { x: 0, y: 0, z: Math.sin(half), w: Math.cos(half) }
}

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
  canSendGoal = false,
) {
  const [status, setStatus] = useState<RosStatus>('idle')
  const [error, setError] = useState<string | null>(null)
  const [batteryPercent, setBatteryPercent] = useState<number | null>(null)
  const [scan, setScan] = useState<LaserScanSample | null>(null)
  const [mapGrid, setMapGrid] = useState<OccupancyGridSample | null>(null)
  const rosRef = useRef<Ros | null>(null)
  const cmdVelRef = useRef<Topic<Twist> | null>(null)
  const goalPoseRef = useRef<Topic<PoseStamped> | null>(null)
  const batteryRef = useRef<Topic<BatteryStateMsg> | null>(null)
  const scanRef = useRef<Topic<LaserScanMsg> | null>(null)
  const mapRef = useRef<Topic<OccupancyGridMsg> | null>(null)
  const keysRef = useRef<Set<string>>(new Set())
  const loopRef = useRef<number | null>(null)
  const hadKeysRef = useRef(false)
  const statusHandlerRef = useRef<((message: unknown) => void) | null>(null)
  const statusEventRefs = useRef<string[]>([])
  const advertiseLabelsRef = useRef<Map<string, 'cmd_vel' | 'goal_pose'>>(new Map())
  const goalRejectedRef = useRef(false)
  const scanRafRef = useRef<number | null>(null)
  const mapRafRef = useRef<number | null>(null)
  const pendingScanRef = useRef<LaserScanSample | null>(null)
  const pendingMapRef = useRef<OccupancyGridSample | null>(null)

  const clearTelemetry = useCallback(() => {
    setBatteryPercent(null)
    setScan(null)
    setMapGrid(null)
  }, [])

  const disconnect = useCallback(() => {
    if (loopRef.current != null) {
      window.clearInterval(loopRef.current)
      loopRef.current = null
    }
    keysRef.current.clear()
    hadKeysRef.current = false
    if (rosRef.current && statusHandlerRef.current) {
      rosRef.current.off('status', statusHandlerRef.current)
      for (const ev of statusEventRefs.current) {
        rosRef.current.off(ev, statusHandlerRef.current)
      }
    }
    statusHandlerRef.current = null
    statusEventRefs.current = []
    advertiseLabelsRef.current.clear()
    goalRejectedRef.current = false
    if (cmdVelRef.current) {
      try {
        cmdVelRef.current.publish({
          linear: { x: 0, y: 0, z: 0 },
          angular: { x: 0, y: 0, z: 0 },
        })
      } catch {
        // ignore
      }
      try {
        cmdVelRef.current.unadvertise()
      } catch {
        // ignore
      }
      cmdVelRef.current = null
    }
    if (goalPoseRef.current) {
      try {
        goalPoseRef.current.unadvertise()
      } catch {
        // ignore
      }
      goalPoseRef.current = null
    }
    if (batteryRef.current) {
      try {
        batteryRef.current.unsubscribe()
      } catch {
        // ignore
      }
      batteryRef.current = null
    }
    if (scanRef.current) {
      try {
        scanRef.current.unsubscribe()
      } catch {
        // ignore
      }
      scanRef.current = null
    }
    if (mapRef.current) {
      try {
        mapRef.current.unsubscribe()
      } catch {
        // ignore
      }
      mapRef.current = null
    }
    if (scanRafRef.current != null) {
      cancelAnimationFrame(scanRafRef.current)
      scanRafRef.current = null
    }
    if (mapRafRef.current != null) {
      cancelAnimationFrame(mapRafRef.current)
      mapRafRef.current = null
    }
    pendingScanRef.current = null
    pendingMapRef.current = null
    if (rosRef.current) {
      try {
        rosRef.current.close()
      } catch {
        // ignore
      }
      rosRef.current = null
    }
    clearTelemetry()
    setStatus('idle')
  }, [clearTelemetry])

  useEffect(() => {
    if (!enabled || !profile?.rosbridgeUrl) {
      disconnect()
      return
    }

    setStatus('connecting')
    setError(null)
    goalRejectedRef.current = false
    const ros = new Ros({ url: profile.rosbridgeUrl })
    rosRef.current = ros

    const onRosStatus = (message: unknown) => {
      const statusMsg = asStatusMessage(message)
      if (!statusMsg) return
      const level = statusMsg.level?.toLowerCase()
      if (level !== 'error' && level !== 'warning') return
      const label = statusMsg.id
        ? advertiseLabelsRef.current.get(statusMsg.id)
        : undefined
      if (statusMsg.id && !label) return
      const topicLabel = label ?? 'advertise'
      const text =
        statusMsg.msg?.trim() ||
        `rosbridge recusou ${topicLabel} (verifique messageType ROS 2)`
      // Only hard-fail goal on error — warning must not stick forever (audit-before-publish).
      if (label === 'goal_pose' && level === 'error') {
        goalRejectedRef.current = true
      }
      setError(text)
    }
    statusHandlerRef.current = onRosStatus
    ros.on('status', onRosStatus)

    const trackAdvertise = (
      topic: Topic<Twist> | Topic<PoseStamped>,
      label: 'cmd_vel' | 'goal_pose',
    ) => {
      const id = topic.advertiseId
      if (!id) return
      advertiseLabelsRef.current.set(id, label)
      const ev = `status:${id}`
      statusEventRefs.current.push(ev)
      ros.on(ev, onRosStatus)
    }

    const advertiseCmdVel = limits != null

    const onConnection = () => {
      setStatus('connected')

      if (advertiseCmdVel && profile.topics.cmd_vel) {
        const topic = new Topic<Twist>({
          ros,
          name: profile.topics.cmd_vel,
          messageType: TWIST_TYPE,
        })
        topic.advertise()
        cmdVelRef.current = topic
        trackAdvertise(topic, 'cmd_vel')
      }

      if (canSendGoal && profile.topics.goal_pose) {
        const goalTopic = new Topic<PoseStamped>({
          ros,
          name: profile.topics.goal_pose,
          messageType: POSE_STAMPED_TYPE,
        })
        goalTopic.advertise()
        goalPoseRef.current = goalTopic
        trackAdvertise(goalTopic, 'goal_pose')
      }

      const batteryTopicName = profile.topics.battery
      if (batteryTopicName) {
        const batteryTopic = new Topic<BatteryStateMsg>({
          ros,
          name: batteryTopicName,
          messageType: BATTERY_STATE_TYPE,
        })
        batteryTopic.subscribe((msg) => {
          const p = msg.percentage
          if (typeof p === 'number' && Number.isFinite(p) && p >= 0) {
            setBatteryPercent(p <= 1 ? p * 100 : p)
          }
        })
        batteryRef.current = batteryTopic
      }

      const scanTopicName = profile.topics.scan
      if (scanTopicName) {
        const scanTopic = new Topic<LaserScanMsg>({
          ros,
          name: scanTopicName,
          messageType: LASER_SCAN_TYPE,
        })
        scanTopic.subscribe((msg) => {
          if (
            typeof msg.angle_min !== 'number' ||
            typeof msg.angle_increment !== 'number' ||
            !Array.isArray(msg.ranges)
          ) {
            return
          }
          pendingScanRef.current = {
            angleMin: msg.angle_min,
            angleIncrement: msg.angle_increment,
            ranges: msg.ranges,
            rangeMax:
              typeof msg.range_max === 'number' && msg.range_max > 0
                ? msg.range_max
                : 10,
          }
          if (scanRafRef.current != null) return
          scanRafRef.current = requestAnimationFrame(() => {
            scanRafRef.current = null
            if (pendingScanRef.current) setScan(pendingScanRef.current)
          })
        })
        scanRef.current = scanTopic
      }

      const mapTopicName = profile.topics.map
      if (mapTopicName) {
        const mapTopic = new Topic<OccupancyGridMsg>({
          ros,
          name: mapTopicName,
          messageType: OCCUPANCY_GRID_TYPE,
        })
        mapTopic.subscribe((msg) => {
          const info = msg.info
          const data = msg.data
          if (
            !info ||
            typeof info.width !== 'number' ||
            typeof info.height !== 'number' ||
            typeof info.resolution !== 'number' ||
            !Array.isArray(data) ||
            info.width <= 0 ||
            info.height <= 0
          ) {
            return
          }
          pendingMapRef.current = {
            width: info.width,
            height: info.height,
            resolution: info.resolution,
            data,
          }
          if (mapRafRef.current != null) return
          mapRafRef.current = requestAnimationFrame(() => {
            mapRafRef.current = null
            if (pendingMapRef.current) setMapGrid(pendingMapRef.current)
          })
        })
        mapRef.current = mapTopic
      }
    }
    const onError = () => {
      setStatus('error')
      setError('Falha ao conectar no rosbridge')
      clearTelemetry()
    }
    const onClose = () => {
      keysRef.current.clear()
      hadKeysRef.current = false
      clearTelemetry()
      setStatus('closed')
    }

    ros.on('connection', onConnection)
    ros.on('error', onError)
    ros.on('close', onClose)

    return () => {
      ros.off('connection', onConnection)
      ros.off('error', onError)
      ros.off('close', onClose)
      disconnect()
    }
  }, [
    enabled,
    canSendGoal,
    // Boolean only — preset changes must not tear down rosbridge.
    limits != null,
    profile?.id,
    profile?.rosbridgeUrl,
    profile?.topics.cmd_vel,
    profile?.topics.goal_pose,
    profile?.topics.battery,
    profile?.topics.scan,
    profile?.topics.map,
    clearTelemetry,
    disconnect,
  ])

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

  const stop = useCallback(() => {
    keysRef.current.clear()
    hadKeysRef.current = false
    publishTwist(0, 0)
  }, [publishTwist])

  const sendGoalPose = useCallback(
    (x: number, y: number, yaw: number, frameId = 'map') => {
      if (goalRejectedRef.current) {
        throw new Error('rosbridge recusou goal_pose (verifique messageType ROS 2)')
      }
      if (!goalPoseRef.current) {
        throw new Error('Tópico goal_pose indisponível')
      }
      const orientation = yawToQuat(yaw)
      const msg: PoseStamped = {
        header: {
          stamp: { sec: 0, nanosec: 0 },
          frame_id: frameId,
        },
        pose: {
          position: { x, y, z: 0 },
          orientation,
        },
      }
      try {
        goalPoseRef.current.publish(msg)
      } catch {
        throw new Error('Falha ao publicar PoseStamped no rosbridge')
      }
    },
    [],
  )

  useEffect(() => {
    if (!enabled || status !== 'connected' || !limits) return

    const releaseKeys = () => {
      if (keysRef.current.size === 0 && !hadKeysRef.current) return
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
    const onPageHide = () => releaseKeys()

    window.addEventListener('keydown', onKeyDown)
    window.addEventListener('keyup', onKeyUp)
    window.addEventListener('blur', onBlur)
    window.addEventListener('pagehide', onPageHide)
    window.addEventListener('beforeunload', onPageHide)
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
      window.removeEventListener('pagehide', onPageHide)
      window.removeEventListener('beforeunload', onPageHide)
      document.removeEventListener('focusin', onFocusIn)
      document.removeEventListener('visibilitychange', onVisibility)
      if (loopRef.current != null) {
        window.clearInterval(loopRef.current)
        loopRef.current = null
      }
      releaseKeys()
    }
  }, [enabled, status, limits, publishTwist, stop])

  return {
    status,
    error,
    publishTwist,
    stop,
    sendGoalPose,
    batteryPercent,
    scan,
    mapGrid,
  }
}
