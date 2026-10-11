const API_BASE = import.meta.env.VITE_API_BASE ?? ''

export class ApiError extends Error {
  readonly status: number
  readonly code?: string

  constructor(status: number, message: string, code?: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

type ErrorEnvelope = {
  code?: string
  message?: string
  error?: { code?: string; message?: string }
}

async function parseError(res: Response): Promise<ApiError> {
  try {
    const body = (await res.json()) as ErrorEnvelope
    const message = body.error?.message ?? body.message ?? res.statusText
    const code = body.error?.code ?? body.code
    return new ApiError(res.status, message, code)
  } catch {
    return new ApiError(res.status, res.statusText)
  }
}

export async function api<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
    credentials: 'include',
  })

  if (!res.ok) {
    throw await parseError(res)
  }

  // 201 Created / 204 No Content often have an empty body (e.g. POST /audit/events).
  if (res.status === 204 || res.status === 205) {
    return undefined as T
  }

  const text = await res.text()
  if (!text) {
    return undefined as T
  }

  return JSON.parse(text) as T
}
