# Bifrost Backend

API de governo (control-plane) — Java **21**, Spring Boot, PostgreSQL, Flyway.

Contratos e ADRs: [`docs/`](../../docs/README.md). Convenções Clean Architecture: [`backend-architecture-and-code.md`](../../docs/docs/conventions/backend-architecture-and-code.md).

## Pré-requisitos

- JDK 21+
- Docker (Postgres): na raiz do monorepo, `docker compose up -d`

## Rodar

```bash
./mvnw spring-boot:run
```

- API: `http://localhost:8081`
- Health: `http://localhost:8081/actuator/health`

Credenciais DB default: ver [`.env.example`](../../.env.example) (`bifrost` / `bifrost`).

## Build / test

```bash
./mvnw verify
```

Integração com Testcontainers entra junto das features (auth, profiles, …).

## Pacotes

```text
com.bifrost.backend
├── domain/
├── application/
└── infrastructure/
```

Skeleton + Etapas A–C: auth/session/profiles, parameters/audit, LLM proxy + CI.

## Auth / LLM local

```bash
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin
export BIFROST_ADMIN_PASSWORD=change-me
# Secret único (≥32 chars). Placeholder público só com profile `dev`.
export BIFROST_JWT_SECRET="$(openssl rand -base64 48)"
# opcional: export BIFROST_LLM_API_KEY=...
./mvnw spring-boot:run
# demo local com placeholder: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

- `POST /api/v1/auth/login` → cookies `BIFROST_ACCESS` / `BIFROST_REFRESH`
- `GET /api/v1/me/session-config`
- `PUT /api/v1/me/active-profile`
- `POST /api/v1/llm/ask` `{ "prompt": "…", "context": {} }` → `{ "reply", "model" }` (admin/operator)

## Próximos passos

→ [`../../docs/docs/operations/proximos-passos.md`](../../docs/docs/operations/proximos-passos.md)

**Próximo código sugerido:** Etapa D (UI / demo NARA).
