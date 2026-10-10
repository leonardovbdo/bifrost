import { useEffect, useRef } from 'react'
import type { OccupancyGridSample } from '../hooks/useRos'
import type { RosStatus } from '../hooks/useRos'
import type { SessionConfig } from '../types/session'

interface MapPanelProps {
  session: SessionConfig
  rosStatus: RosStatus
  mapGrid: OccupancyGridSample | null
}

export function MapPanel({ session, rosStatus, mapGrid }: MapPanelProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const topic = session.activeProfile.topics.map

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas || !mapGrid) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const { width, height, data } = mapGrid
    canvas.width = width
    canvas.height = height
    const img = ctx.createImageData(width, height)
    // OccupancyGrid row 0 is map +y at origin; canvas row 0 is top — flip Y.
    for (let y = 0; y < height; y++) {
      for (let x = 0; x < width; x++) {
        const i = y * width + x
        const v = data[i] ?? -1
        let gray = 128
        if (v === 0) gray = 240
        else if (v === 100) gray = 20
        else if (v < 0) gray = 90
        else gray = 240 - Math.round((v / 100) * 220)
        const o = ((height - 1 - y) * width + x) * 4
        img.data[o] = gray
        img.data[o + 1] = gray
        img.data[o + 2] = gray
        img.data[o + 3] = 255
      }
    }
    ctx.putImageData(img, 0, 0)
  }, [mapGrid])

  if (!topic) {
    return (
      <section className="panel">
        <h2>Mapa (SLAM)</h2>
        <p className="muted">Profile sem tópico map.</p>
      </section>
    )
  }

  let empty: string
  if (rosStatus !== 'connected') {
    empty =
      rosStatus === 'connecting'
        ? 'Conectando ao rosbridge…'
        : 'Rosbridge desconectado — mapa indisponível.'
  } else {
    empty = 'Aguardando OccupancyGrid…'
  }

  return (
    <section className="panel">
      <h2>Mapa (SLAM)</h2>
      <p className="muted mono">{topic}</p>
      {mapGrid ? (
        <canvas
          ref={canvasRef}
          className="map-canvas"
          aria-label="Visualização OccupancyGrid"
        />
      ) : (
        <p className="muted">{empty}</p>
      )}
    </section>
  )
}
