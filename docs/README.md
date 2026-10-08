# Documentação de Arquitetura — Bifrost (TCC / NARA)

> **Bifrost** — ponte governada entre pessoas e sistemas autônomos. Documentação do backend (docs-first) + skeleton em `apps/backend`. Protótipo de frontend próprio apenas para validar a API, se necessário.

**Retomar implementação:** [`docs/operations/proximos-passos.md`](docs/operations/proximos-passos.md).  
**Esteira pós-MVP (E–H):** [`docs/operations/esteira-pos-mvp.md`](docs/operations/esteira-pos-mvp.md).

Metáfora do nome: [`docs/architecture/bifrost-nome-e-metafora.md`](docs/architecture/bifrost-nome-e-metafora.md).

## O que é cada pasta?

| Pasta | Finalidade |
|-------|------------|
| [`docs/adrs/`](docs/adrs/) | Architecture Decision Records — decisões e trade-offs |
| [`docs/architecture/`](docs/architecture/) | Visão de sistema, spec funcional do backend e consumo por clientes |
| [`docs/architecture/backend/features/`](docs/architecture/backend/features/) | Pacotes por feature (`README`, `spec`, `adr`, `tasks`) |
| [`docs/conventions/`](docs/conventions/) | Convenções de código e de documentação |
| [`docs/operations/`](docs/operations/) | Manuais de ambiente local e operação |
| [`docs/requirements/`](docs/requirements/) | Requisitos e inventário de responsabilidades típicas de uma IHM (gap analysis) |
| [`docs/diagrams/`](docs/diagrams/) | Diagramas Mermaid (sequência, C4, ER) |
| [`academic/`](academic/README.md) | Índice dos documentos acadêmicos de origem (PDFs fora do git) |

## Como usar as ADRs

Cada ADR segue o formato: **Contexto → Opções → Decisão → Consequências**.  
ADRs com status `PROPOSTA` precisam de decisão antes da implementação. ADRs `ACEITA` são a referência vigente.

## Stack declarada

| Camada | Tecnologia | Observações |
|--------|------------|-------------|
| **Backend** | Java + Spring Boot | API REST de governo (auth, perfis, params, auditoria, LLM proxy) — ADR-006 |
| **Banco de dados** | PostgreSQL | Usuários, papéis, robot profiles, parâmetros, telemetria/auditoria |
| **Autenticação** | JWT + RBAC (cookies httpOnly) | Papéis `admin`, `operator`, `viewer` — ADR-004 / ADR-008 |
| **ROS (tempo real)** | Rosbridge + web_video_server | Cliente no browser; backend emite session-config/ACL — ADR-002 / ADR-009 |
| **Estudo de caso** | NARA / `noblenara` | Extensível via Robot Profiles — ADR-003 / ADR-010 |
| **Cliente UI** | Protótipo Vite/React (`apps/web`) | Session-config + teleop híbrido + LLM |
| **LLM** | Proxy no backend (Gemini, pluggable) | ADR-011; chave nunca no frontend; admin+operator |
| **Ops local** | Docker Compose (Postgres) | `bifrost`/`bifrost` @ `5432` — ver `operations/docker-postgres-local.md` |

## Leitura sugerida (ordem)

1. [`docs/operations/proximos-passos.md`](docs/operations/proximos-passos.md) — **onde paramos**
2. [`docs/operations/esteira-pos-mvp.md`](docs/operations/esteira-pos-mvp.md) — **fila normativa E–H**
3. [`docs/architecture/bifrost-nome-e-metafora.md`](docs/architecture/bifrost-nome-e-metafora.md)
4. [`docs/architecture/system-overview.md`](docs/architecture/system-overview.md)
5. [`docs/requirements/inventario-frontend-ihm.md`](docs/requirements/inventario-frontend-ihm.md)
6. [`docs/requirements/levantamento-de-requisitos.md`](docs/requirements/levantamento-de-requisitos.md)
7. ADRs em [`docs/adrs/`](docs/adrs/) (stack vigente = **ADR-006/007** Java/Spring + PostgreSQL; JWT/session/profile = **008–010**)
8. [`docs/architecture/backend-functional-spec.md`](docs/architecture/backend-functional-spec.md)
9. [`docs/diagrams/`](docs/diagrams/)
10. [`docs/operations/ambiente-local-nara.md`](docs/operations/ambiente-local-nara.md)
11. [`docs/operations/auditoria-retencao.md`](docs/operations/auditoria-retencao.md)

## Relação com outros repositórios

- **`noblenara`** — runtime ROS / Gazebo (estudo de caso).
- Clientes web — qualquer front que implemente o contrato da API; o TCC pode fornecer um **protótipo próprio** para testes.
