import { useEffect, useRef } from 'react'
import type { LaserScanSample, RosStatus } from '../hooks/useRos'
import type { SessionConfig } from '../types/session'

interface ScanPanelProps {
  session: SessionConfig
  rosStatus: RosStatus
  scan: LaserScanSample | null
}

export function ScanPanel({ session, rosStatus, scan }: ScanPanelProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const topic = session.activeProfile.topics.scan

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas || !scan) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const w = canvas.width
    const h = canvas.height
    const cx = w / 2
    const cy = h * 0.85
    const scale = Math.min(cx, cy) / Math.max(scan.rangeMax, 0.1)

    ctx.clearRect(0, 0, w, h)
    ctx.fillStyle = '#0b1220'
    ctx.fillRect(0, 0, w, h)

    ctx.strokeStyle = 'rgba(62, 207, 178, 0.25)'
    ctx.beginPath()
    ctx.arc(cx, cy, scale * scan.rangeMax, Math.PI, 0)
    ctx.stroke()

    ctx.fillStyle = '#3ecfb2'
    for (let i = 0; i < scan.ranges.length; i += 2) {
      const r = scan.ranges[i]
      if (!Number.isFinite(r) || r <= 0 || r > scan.rangeMax) continue
      const angle = scan.angleMin + i * scan.angleIncrement
      const x = cx - Math.sin(angle) * r * scale
      const y = cy - Math.cos(angle) * r * scale
      ctx.fillRect(x, y, 2, 2)
    }

    ctx.fillStyle = '#f8fafc'
    ctx.beginPath()
    ctx.arc(cx, cy, 4, 0, Math.PI * 2)
    ctx.fill()
  }, [scan])

  if (!topic) {
    return (
      <section className="panel">
        <h2>Scan (LiDAR)</h2>
        <p className="muted">Profile sem tópico scan.</p>
      </section>
    )
  }

  let empty: string
  if (rosStatus !== 'connected') {
    empty =
      rosStatus === 'connecting'
        ? 'Conectando ao rosbridge…'
        : 'Rosbridge desconectado — scan indisponível.'
  } else {
    empty = 'Aguardando LaserScan…'
  }

  return (
    <section className="panel">
      <h2>Scan (LiDAR)</h2>
      <p className="muted mono">{topic}</p>
      {scan ? (
        <canvas
          ref={canvasRef}
          className="scan-canvas"
          width={320}
          height={220}
          aria-label="Visualização LaserScan"
        />
      ) : (
        <p className="muted">{empty}</p>
      )}
    </section>
  )
}
