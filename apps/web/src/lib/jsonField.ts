export function stringifyJson(value: unknown, spaces = 2): string {
  return JSON.stringify(value, null, spaces)
}

export function parseJsonObject(raw: string, label: string): Record<string, string> {
  let parsed: unknown
  try {
    parsed = JSON.parse(raw)
  } catch {
    throw new Error(`${label}: JSON inválido`)
  }
  if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error(`${label}: deve ser um objeto JSON`)
  }
  const out: Record<string, string> = {}
  for (const [k, v] of Object.entries(parsed as Record<string, unknown>)) {
    if (typeof v !== 'string') {
      throw new Error(`${label}: valores devem ser strings`)
    }
    out[k] = v
  }
  return out
}

export function parseJsonValue(raw: string, label: string): Record<string, unknown> {
  let parsed: unknown
  try {
    parsed = JSON.parse(raw)
  } catch {
    throw new Error(`${label}: JSON inválido`)
  }
  if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error(`${label}: deve ser um objeto JSON`)
  }
  return parsed as Record<string, unknown>
}

export function parseCapabilities(raw: string): string[] {
  const trimmed = raw.trim()
  if (!trimmed) {
    throw new Error('capabilities: informe ao menos uma')
  }
  if (trimmed.startsWith('[')) {
    const parsed = JSON.parse(trimmed) as unknown
    if (!Array.isArray(parsed) || parsed.some((c) => typeof c !== 'string')) {
      throw new Error('capabilities: array JSON de strings')
    }
    return parsed as string[]
  }
  return trimmed
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}
