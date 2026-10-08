import { useEffect, useMemo, useState } from 'react'
import type { ActiveProfile } from '../types/session'

export type CameraOption = { key: string; topic: string; label: string }

const LABEL_BY_KEY: Record<string, string> = {
  camera_user: 'Usuário (rosto)',
  camera_link: 'Frente (link)',
}

export function listCameraOptions(profile: ActiveProfile): CameraOption[] {
  const options: CameraOption[] = []
  const seen = new Set<string>()

  const push = (key: string, topic: string | undefined) => {
    if (!topic || seen.has(topic)) return
    seen.add(topic)
    options.push({
      key,
      topic,
      label: LABEL_BY_KEY[key] ?? key.replace(/^camera_/, '').replace(/_/g, ' '),
    })
  }

  push('camera_user', profile.topics.camera_user)
  push('camera_link', profile.topics.camera_link)
  for (const [key, topic] of Object.entries(profile.topics)) {
    if (!key.startsWith('camera') || key === 'camera_user' || key === 'camera_link') continue
    if (key.includes('depth') || key.includes('info') || key.includes('points')) continue
    push(key, topic)
  }
  return options
}

/**
 * One persistent MJPEG stream. Do not poll snapshots — web_video_server
 * stalls under many concurrent /snapshot connections.
 * Do not encodeURIComponent(topic): server rejects "%2F...".
 */
export function CameraFeed({
  profile,
  topic,
}: {
  profile: ActiveProfile
  topic: string | null
}) {
  const base = profile.videoBaseUrl?.replace(/\/$/, '') ?? ''
  const src =
    topic && base
      ? `${base}/stream?topic=${topic}&type=mjpeg`
      : null

  if (!src) {
    return (
      <div className="camera-placeholder">
        <p>Sem tópico de câmera no profile ativo.</p>
        <p className="muted">Suba web_video_server e o bridgelaunch do NARA.</p>
      </div>
    )
  }

  return (
    <img
      key={src}
      className="camera-feed"
      src={src}
      alt={`Câmera ${profile.displayName}`}
    />
  )
}

export function CameraStage({ profile }: { profile: ActiveProfile }) {
  const options = useMemo(() => listCameraOptions(profile), [profile])
  const [selectedKey, setSelectedKey] = useState<string>(() => options[0]?.key ?? '')

  useEffect(() => {
    if (options.length === 0) {
      setSelectedKey('')
      return
    }
    if (!options.some((o) => o.key === selectedKey)) {
      setSelectedKey(options[0].key)
    }
  }, [options, selectedKey])

  const selected = options.find((o) => o.key === selectedKey) ?? options[0] ?? null

  return (
    <>
      <div className="stage-header">
        <h2>Câmera</h2>
        {options.length > 1 ? (
          <label className="camera-select">
            <span className="sr-only">Ponto de vista</span>
            <select
              value={selected?.key ?? ''}
              onChange={(e) => setSelectedKey(e.target.value)}
            >
              {options.map((o) => (
                <option key={o.key} value={o.key}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
        ) : (
          <span className="muted">{selected?.topic ?? 'sem tópico'}</span>
        )}
      </div>
      <CameraFeed profile={profile} topic={selected?.topic ?? null} />
    </>
  )
}
