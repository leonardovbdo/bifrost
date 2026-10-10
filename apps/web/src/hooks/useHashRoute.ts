import { useEffect, useState } from 'react'
import type { AdminTab } from '../types/admin'

const ADMIN_TABS: AdminTab[] = ['profiles', 'parameters', 'users', 'audit']

export type AppView = { kind: 'console' } | { kind: 'admin'; tab: AdminTab }

function parseHash(hash: string): AppView {
  if (!hash || hash === '#' || hash === '#/') {
    return { kind: 'console' }
  }
  if (hash === '#/admin' || hash === '#/admin/') {
    return { kind: 'admin', tab: 'profiles' }
  }
  const match = /^#\/admin\/(\w+)$/.exec(hash)
  if (!match) {
    if (hash.startsWith('#/admin')) {
      return { kind: 'admin', tab: 'profiles' }
    }
    return { kind: 'console' }
  }
  const tab = match[1] as AdminTab
  if (!ADMIN_TABS.includes(tab)) {
    return { kind: 'admin', tab: 'profiles' }
  }
  return { kind: 'admin', tab }
}

export function adminHash(tab: AdminTab): string {
  return `#/admin/${tab}`
}

export function consoleHash(): string {
  return '#/'
}

export function useHashRoute(): AppView {
  const [view, setView] = useState<AppView>(() =>
    parseHash(typeof window !== 'undefined' ? window.location.hash : ''),
  )

  useEffect(() => {
    const onHash = () => setView(parseHash(window.location.hash))
    window.addEventListener('hashchange', onHash)
    return () => window.removeEventListener('hashchange', onHash)
  }, [])

  return view
}
