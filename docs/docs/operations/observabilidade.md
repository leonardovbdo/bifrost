# Observabilidade — MVP

Conforme [ADR-013](../adrs/ADR-013-observabilidade.md):

| Sinal | MVP |
|-------|-----|
| Health | Actuator `/health` (e readiness com Postgres quando a API existir) |
| Logs | Incluir **correlation-id** (header `X-Correlation-Id` ou UUID gerado) |
| Métricas | Prometheus / OpenTelemetry — **depois** |

Não expor Actuator além de health sem autenticação.
