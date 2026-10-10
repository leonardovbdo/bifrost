import { consoleHash } from '../../hooks/useHashRoute'

export function AdminForbidden() {
  return (
    <main className="boot-shell">
      <p className="brand-mark">Bifrost</p>
      <p className="form-error">Área admin restrita ao papel admin.</p>
      <p className="muted">
        <a href={consoleHash()}>Voltar ao console</a>
      </p>
    </main>
  )
}
