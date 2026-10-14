interface UiHintProps {
  id: string
  children: string
}

/** Short on-screen explanation for demo/orientador; also targeted by Playwright. */
export function UiHint({ id, children }: UiHintProps) {
  return (
    <p className="ui-hint" data-testid={`hint-${id}`} role="note">
      {children}
    </p>
  )
}
