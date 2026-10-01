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

Skeleton: Actuator health, Flyway, correlation-id, **auth/session (Etapa A)**.

## Auth local (Etapa A)

```bash
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin
export BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET=change-me-bifrost-dev-secret-at-least-32-chars
./mvnw spring-boot:run
```

- `POST /api/v1/auth/login` → cookies `BIFROST_ACCESS` / `BIFROST_REFRESH`
- `GET /api/v1/me/session-config`
- `PUT /api/v1/me/active-profile`

## Próximos passos

→ [`../../docs/docs/operations/proximos-passos.md`](../../docs/docs/operations/proximos-passos.md)

**Próximo código sugerido:** PROF-03/04 ou Etapa B (parameters).
