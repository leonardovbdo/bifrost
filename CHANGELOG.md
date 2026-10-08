# Changelog

Todas as mudanças notáveis deste projeto são documentadas aqui.
Formato baseado em [Keep a Changelog](https://keepachangelog.com/).
Versionamento: [SemVer](https://semver.org/)
(ver `docs/docs/conventions/git-and-releases.md`).

## [Unreleased]

### Added

- Etapa D: protótipo UI `apps/web` (login, session-config, teleop,
  câmera, LLM) + CI Node (`feat/etapa-d-ui-demo`)
- Etapa C: proxy LLM `/llm/ask` (Gemini + stub), audit `llm_ask`,
  CI Maven (`feat/etapa-c-llm-ci`)
- Etapa B: parameters + audit events, purge 90d
  (`feat/etapa-b-parameters-audit`)
- Etapa A: auth JWT/cookies, session-config, robot profiles
  (`feat/etapa-a-auth-session`, PR #1)
- ADRs 008–013 (JWT, session-config, robot profile schema, LLM, CI/CD,
  observabilidade)
- Feature packs: `parameters`, `llm`; specs/tasks auth, profiles e
  telemetria alinhados
- Convenções: `api-http`, `git-and-releases`, `testing`, `security`
- Ops: Compose Postgres, retenção de auditoria, observabilidade MVP,
  guia `proximos-passos`
- Diagramas Mermaid (sequência session-config, C4, ER)
- Skeleton `apps/backend` (Java 21, Spring Boot, Flyway baseline,
  correlation-id)
- CI: workflow `docs` (markdownlint)
- `CHANGELOG.md`, `.env.example`

### Fixed

- Etapa D: envelope de erro da API, teleop sem capturar inputs,
  Twist ROS 2, deadman/blur e bootstrap de sessão (StrictMode)
- Etapa D: soltar teleop ao focar inputs e status de advertise
  rosbridge
- Etapa D: publicar Twist zero no disconnect antes do unadvertise
- Config markdownlint-cli2 (`config` + MD024 siblings_only)
- Etapa C: default Gemini `gemini-3.6-flash`, API key em header,
  LLM fora da transação e limite prompt+context
- Etapa B: `login_failure` com REQUIRES_NEW; Stop limpa teclas;
  presets teleop numéricos; goal_pose só com canSendGoal;
  ip/userAgent do request sempre sobrescritos
- Etapa B: cookies JWT com `Path=/api`; payload `goal_pose`
  valida x/y/yaw sem nulls; login/logout/session-config
  expiram cookie legado `Path=/`
- Auth: JWT secret placeholder só com profile `dev`; reuso de
  refresh revoga todas as sessões (`REQUIRES_NEW`); access exige
  sessão ativa e role/`active` do banco; teleop manda zero no
  `pagehide`
- Etapa D: câmera MJPEG com encode só por segmento (mantém `/`;
  web_video_server rejeita `%2F`); seletor `camera_user` /
  `camera_link`; `onError` quando o stream falha

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

- Repositório Bifrost docs-first; ADR-001 (histórica Node); visão inicial
  do sistema
