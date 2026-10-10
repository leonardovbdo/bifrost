# Bifrost (TCC) — Backend para Controle e Monitoramento de Sistemas Autônomos

Repositório do Trabalho de Conclusão de Curso (Bacharelado em Sistemas de Informação — IFBA Campus Vitória da Conquista).

**Produto (nome de trabalho):** **Bifrost** — *a ponte governada entre pessoas e sistemas autônomos.*  
**Discente:** Leonardo Vilasboas de Oliveira  
**Orientador:** Prof. Crescencio Rodrigues Lima Neto  
**Estudo de caso (runtime):** projeto NARA / repositório `noblenara` (cadeira de rodas autônoma em ROS)

Metáfora e posicionamento do nome: [`docs/docs/architecture/bifrost-nome-e-metafora.md`](docs/docs/architecture/bifrost-nome-e-metafora.md).

## Propósito

Desenvolver e documentar **Bifrost**, uma arquitetura de **backend** (Java + Spring Boot + PostgreSQL) que **governa** identidade, perfis de robô, parametrização, níveis de acesso e telemetria para sistemas autônomos — com o NARA como caso de uso, sem se limitar a uma única tecnologia ou a um frontend de terceiros.

A comunicação de **baixa latência** com o ROS (teleop, sensores, câmeras) permanece no modelo **híbrido**: o cliente (protótipo de UI do TCC ou outro front) fala com rosbridge / web_video_server; o backend autoriza, configura e audita.

Este repositório é **docs-first** com implementação em [`apps/backend`](apps/backend/README.md) (Java 21 + Spring Boot) e protótipo de UI em [`apps/web`](apps/web/README.md). Features de produto seguem as ADRs e packs em `docs/`.

## Stack declarada

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 21 + Spring Boot (`apps/backend`) |
| Banco | PostgreSQL 16 (`docker compose`) |
| Integração ROS | Rosbridge (cliente no browser; governo no backend) |
| Cliente de testes | Protótipo UI Vite/React (`apps/web`) |
| Runtime robótico | ROS 2 Jazzy (`noblenara` / `nara-sim`) |

## Quick start (API + UI local)

```bash
docker compose up -d
cd apps/backend && ./mvnw spring-boot:run
# outro terminal:
cd apps/web && npm install && npm run dev
# UI: http://localhost:5173 · health: http://localhost:8081/actuator/health
```

## Documentação

Toda a documentação arquitetural vive em [`docs/`](docs/README.md), organizada em `adrs/`, `architecture/`, `conventions/`, `requirements/`, `operations/` e `diagrams/`.

**Retomar o trabalho:** [`docs/docs/operations/proximos-passos.md`](docs/docs/operations/proximos-passos.md).

Changelog: [`CHANGELOG.md`](CHANGELOG.md).

Documentos acadêmicos de origem (súmula, slides, fundamentação): índice em [`docs/academic/`](docs/academic/README.md) — PDFs ficam fora do git.

## Repositórios relacionados

| Repositório | Papel |
|-------------|--------|
| `noblenara` | Runtime ROS / simulação — estudo de caso deste TCC |
| *(opcional)* outros fronts | Qualquer cliente HTTP/WS que respeite o contrato da API; **não** há dependência de um IHM externo específico |

## Status

| Fase | Situação |
|------|----------|
| Definição (ADRs, specs, convenções, ops, diagramas) | **Concluída** |
| Etapas A–D (API + console NARA) | **Na `main`** |
| Esteira pós-MVP (E–H) | **Planejada** — [esteira-pos-mvp](docs/docs/operations/esteira-pos-mvp.md) |

Retomar: [`docs/docs/operations/proximos-passos.md`](docs/docs/operations/proximos-passos.md).
