/** Parse a goal coordinate from form text. Blank → null; comma decimal OK; `"0"` valid. */
export function parseGoalCoord(raw: string): number | null {
  const trimmed = raw.trim()
  if (!trimmed) return null
  const normalized = trimmed.replace(',', '.')
  const n = Number(normalized)
  if (!Number.isFinite(n)) return null
  return n
}
