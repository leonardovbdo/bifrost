# Decisões — Parameters

Decisões fechadas na definição de metas (2026-09-30):

## Escopos MVP

| Escopo | MVP | Quem escreve | Quem lê |
|--------|-----|--------------|---------|
| `global` | Sim | `admin` | autenticados (via merge na session) |
| `user` | Sim | próprio usuário (+ admin) | próprio usuário |
| `role` | Não | — | reservado |
| `profile` | Não | — | reservado (útil com sim+físico) |

**Merge no MVP:** `user` sobrescreve `global` nas chaves aplicáveis.

## Presets de velocidade (defaults)

| Perfil | linearMax (m/s) | angularMax (rad/s) |
|--------|-----------------|--------------------|
| `safety` | 0.20 | 0.60 |
| `normal` | 0.50 | 1.00 |
| `fast` | 0.80 | 1.40 |

- Default global do preset ativo: `normal`
- Preferência `user` escolhe entre `safety` | `normal` | `fast` (não redefine os tetos absolutos dos presets; isso é `global`)

## Influência no runtime

- MVP: parameters → montagem de `limits.teleop` em `GET /me/session-config`
- Fora do MVP: validação server-side de comandos ROS (exigiria mediador além do híbrido atual)

## Chaves sugeridas

- `teleop.presets` (JSON com os três perfis) — escopo `global`
- `teleop.activeProfile` (`safety` \| `normal` \| `fast`) — escopo `global` (default) e `user` (override)
