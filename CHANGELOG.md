# Changelog

Todas as mudanças notáveis deste projeto são documentadas aqui.  
Formato baseado em [Keep a Changelog](https://keepachangelog.com/). Versionamento: [SemVer](https://semver.org/) (ver `docs/docs/conventions/git-and-releases.md`).

## [Unreleased]

### Added

- Etapa A: auth JWT/cookies, refresh, `/me`, session-config, seeds admin + `nara-sim-alfa` (`feat/etapa-a-auth-session`)


### Added

- ADRs 008–013 (JWT, session-config, robot profile schema, LLM, CI/CD, observabilidade)
- Feature packs: `parameters`, `llm`; specs/tasks auth, profiles e telemetria alinhados
- Convenções: `api-http`, `git-and-releases`, `testing`, `security`
- Ops: Compose Postgres, retenção de auditoria, observabilidade MVP, guia `proximos-passos`
- Diagramas Mermaid (sequência session-config, C4, ER)
- Skeleton `apps/backend` (Java 21, Spring Boot, Flyway baseline, correlation-id)
- CI: workflow `docs` (markdownlint)
- `CHANGELOG.md`, `.env.example`

### Changed

- README raiz e `docs/README` com status e ordem de retomada
- Functional spec e ADR-003 alinhados às novas decisões

## [0.3.0] — 2026-09

### Added

- ADR-006 Java/Spring; ADR-007 PostgreSQL
- Convenção Clean Architecture / DDD (`backend-architecture-and-code`)

## [0.2.0] — 2026-09

### Added

- ADRs 002–005 (ROS híbrido, robot profile, auth/RBAC, LGPD)
- Specs de arquitetura e feature packs iniciais (auth, profiles, telemetria)

## [0.1.0] — 2026-09

### Added

- Repositório Bifrost docs-first; ADR-001 (histórica Node); visão inicial do sistema
