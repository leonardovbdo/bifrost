import { useEffect, useMemo, useState } from 'react'
import type { ActiveProfile } from '../types/session'

export type CameraOption = { key: string; topic: string; label: string }

const LABEL_BY_KEY: Record<string, string> = {
  camera_user: 'Usuário (rosto)',
  camera_link: 'Frente (link)',
}

/**
 * Encode path segments but keep `/`. Full encodeURIComponent turns `/` into
 * `%2F`, which web_video_server rejects as an invalid ROS name (it does not
 * decode the query value). Segment encoding still escapes `&` / `#` if a
 * malformed topic slips into a profile.
 */
export function encodeRosTopicQuery(topic: string): string {
  return topic
    .split('/')
    .map((segment) => encodeURIComponent(segment))
    .join('/')
}

export function mjpegStreamUrl(videoBaseUrl: string, topic: string): string {
  const base = videoBaseUrl.replace(/\/$/, '')
  return `${base}/stream?topic=${encodeRosTopicQuery(topic)}&type=mjpeg`
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

  // Seed NARA: camera_user = /…/camera_user ; camera_link = /…/camera_link/image
  push('camera_user', profile.topics.camera_user)
  push('camera_link', profile.topics.camera_link)
  return options
}

export function CameraFeed({
  profile,
  topic,
}: {
  profile: ActiveProfile
  topic: string | null
}) {
  const [failed, setFailed] = useState(false)
  const base = profile.videoBaseUrl?.replace(/\/$/, '') ?? ''
  const src = topic && base ? mjpegStreamUrl(profile.videoBaseUrl, topic) : null

  useEffect(() => {
    setFailed(false)
  }, [src])

  if (!src) {
    return (
      <div className="camera-placeholder">
        <p>Sem tópico de câmera no profile ativo.</p>
        <p className="muted">Suba web_video_server e o bridgelaunch do NARA.</p>
      </div>
    )
  }

  if (failed) {
    return (
      <div className="camera-placeholder">
        <p>Falha ao carregar o stream de câmera.</p>
        <p className="muted">
          Confira se o web_video_server está em {base} e se o tópico existe:
        </p>
        <p className="muted mono">{topic}</p>
      </div>
    )
  }

  return (
    <img
      key={src}
      className="camera-feed"
      src={src}
      alt={`Câmera ${profile.displayName}`}
      onError={() => setFailed(true)}
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
