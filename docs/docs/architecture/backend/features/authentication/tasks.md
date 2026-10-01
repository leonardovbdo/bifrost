# Tasks — Autenticação

Contratos: ADR-008, ADR-009. Skeleton: `apps/backend`.

| ID | Task | Status |
|----|------|--------|
| AUTH-01 | Migration `users` (+ `last_active_profile_id`) | `TODO` |
| AUTH-02 | Hash bcrypt + login (cookies httpOnly) | `TODO` |
| AUTH-03 | JWT access + refresh (TTL param.) | `TODO` |
| AUTH-04 | Guards `ROLE_*` | `TODO` |
| AUTH-05 | `GET /me` | `TODO` |
| AUTH-06 | `GET /me/session-config` + `PUT /me/active-profile` | `TODO` |
| AUTH-07 | Seed admin via env (primeiro boot) | `TODO` |
| AUTH-08 | `POST /auth/refresh` + logout | `TODO` |
| AUTH-09 | Testes integração (Testcontainers) | `TODO` |

Ordem sugerida: 01 → 02 → 03 → 05 → 04 → 07 → 08 → 06 → 09.
