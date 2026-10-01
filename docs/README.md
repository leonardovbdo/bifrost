# Documentação de Arquitetura — Bifrost (TCC / NARA)

> **Bifrost** — ponte governada entre pessoas e sistemas autônomos. Documentação do backend (docs-first). Implementação futura em `apps/backend`; protótipo de frontend próprio apenas para validar a API, se necessário.

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
| [`docs/diagrams/`](docs/diagrams/) | Diagramas (Mermaid / exports) |
| [`academic/`](academic/README.md) | Índice dos documentos acadêmicos de origem (PDFs fora do git) |

## Como usar as ADRs

Cada ADR segue o formato: **Contexto → Opções → Decisão → Consequências**.  
ADRs com status `PROPOSTA` precisam de decisão antes da implementação. ADRs `ACEITA` são a referência vigente.

## Stack declarada

| Camada | Tecnologia | Observações |
|--------|------------|-------------|
| **Backend** | Java + Spring Boot | API REST de governo (auth, perfis, params, auditoria, LLM proxy) — ADR-006 |
| **Banco de dados** | MySQL | Usuários, papéis, robot profiles, parâmetros, telemetria/auditoria |
| **Autenticação** | JWT + RBAC | Papéis `admin`, `operator`, `viewer` — ver ADR-004 |
| **ROS (tempo real)** | Rosbridge + web_video_server | Cliente no browser; backend emite session-config/ACL — ver ADR-002 |
| **Estudo de caso** | NARA / `noblenara` | Extensível via Robot Profiles — ver ADR-003 |
| **Cliente UI** | Protótipo do TCC (futuro) | Não depende de IHM de terceiros |
| **LLM** | Proxy no backend (ex.: Gemini) | Chave de API nunca no frontend |

## Leitura sugerida (ordem)

1. [`docs/architecture/bifrost-nome-e-metafora.md`](docs/architecture/bifrost-nome-e-metafora.md)
2. [`docs/architecture/system-overview.md`](docs/architecture/system-overview.md)
3. [`docs/requirements/inventario-frontend-ihm.md`](docs/requirements/inventario-frontend-ihm.md)
4. [`docs/requirements/levantamento-de-requisitos.md`](docs/requirements/levantamento-de-requisitos.md)
5. ADRs em [`docs/adrs/`](docs/adrs/) (stack vigente = **ADR-006** Java/Spring; ADR-001 histórica)
6. [`docs/architecture/backend-functional-spec.md`](docs/architecture/backend-functional-spec.md)
7. [`docs/operations/ambiente-local-nara.md`](docs/operations/ambiente-local-nara.md)

## Relação com outros repositórios

- **`noblenara`** — runtime ROS / Gazebo (estudo de caso).
- Clientes web — qualquer front que implemente o contrato da API; o TCC pode fornecer um **protótipo próprio** para testes.
