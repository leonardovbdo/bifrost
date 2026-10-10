# Tasks — Autenticação

Contratos: ADR-008, ADR-009. Skeleton: `apps/backend`.

| ID | Task | Status |
|----|------|--------|
| AUTH-01 | Migration `users` (+ `last_active_profile_id`) | `DONE` |
| AUTH-02 | Hash bcrypt + login (cookies httpOnly) | `DONE` |
| AUTH-03 | JWT access + refresh (TTL param.) | `DONE` |
| AUTH-04 | Guards `ROLE_*` | `DONE` |
| AUTH-05 | `GET /me` | `DONE` |
| AUTH-06 | `GET /me/session-config` + `PUT /me/active-profile` | `DONE` |
| AUTH-07 | Seed admin via env (primeiro boot) | `DONE` |
| AUTH-08 | `POST /auth/refresh` + logout | `DONE` |
| AUTH-09 | Testes integração (Testcontainers) | `DONE` (skip sem Docker) |
| AUTH-10 | API admin `GET`/`POST`/`PATCH /api/v1/users` (Etapa F-01) | `DONE` |

Ordem sugerida: 01 → 02 → 03 → 05 → 04 → 07 → 08 → 06 → 09 → 10.
