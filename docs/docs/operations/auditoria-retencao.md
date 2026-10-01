# Auditoria — retenção e operação

## Política

- Retenção padrão: **90 dias** (`BIFROST_AUDIT_RETENTION_DAYS`, default `90`)
- Purge: job diário na API (Spring `@Scheduled` ou equivalente)
- Eventos fora da janela são **apagados** (não arquivados no MVP)

## O que não entra

- Stream de teleop (`cmd_vel`)
- Payloads com senha, JWT completo ou API keys
- Biometria (ADR-005)

## Emergência

Se o job falhar, purge manual no PostgreSQL (exemplo para 90 dias):

```sql
DELETE FROM audit_events
WHERE created_at < NOW() - INTERVAL '90 days';
```

Ajustar o intervalo ao valor de `BIFROST_AUDIT_RETENTION_DAYS`.

## Consulta

- `GET /api/v1/audit/events` — apenas `admin`, com filtros de tipo/período/usuário
