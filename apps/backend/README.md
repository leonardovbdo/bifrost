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

Skeleton mínimo: Actuator health, Flyway baseline, filtro `X-Correlation-Id` (ADR-013). Auth/JWT e demais módulos vêm nas tasks das feature packs.

## Próximos passos

Ver o guia canônico de retomada:

→ [`../../docs/docs/operations/proximos-passos.md`](../../docs/docs/operations/proximos-passos.md)

**Próxima task de código:** AUTH-01 (migration `users`).
