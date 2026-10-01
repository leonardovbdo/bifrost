# ADR-013 — Observabilidade (MVP)

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  

---

## Contexto

A API Spring precisará de sinais mínimos de vida e de correlação de logs. Métricas full-stack (Prometheus/OTel) competem com o escopo do TCC se entrarem cedo demais.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — Health + correlation-id** | Barato; suficiente para demo/debug | Sem dashboard de métricas |
| B — A + Prometheus/Grafana já no MVP | Ops “de produção” | Escopo e ops extras |
| C — OpenTelemetry completo | Padrão moderno | Overkill no MVP |

---

## Decisão

Adotar **Opção A** no MVP:

1. Spring Boot Actuator: **`/health`** (liveness; readiness quando houver deps críticas como Postgres)
2. Logs estruturados ou pelo menos com **correlation-id** (filter/MDC propagando header `X-Correlation-Id` ou gerando UUID)
3. **Prometheus / OpenTelemetry / Grafana:** fase seguinte (pós-MVP ou evolução ops)

Não expor endpoints Actuator sensíveis sem autenticação quando forem habilitados além de health.

---

## Consequências

**Positivas**

- Compose/CI podem checar health depois
- Suporte a debug de request sem stack de observabilidade

**Negativas**

- Sem SLOs/métricas de latência no MVP
- Correlation depende de o client (ou gateway) enviar/aceitar o header
