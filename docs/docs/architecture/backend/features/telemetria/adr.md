# ADR da feature — Telemetria / Auditoria

## Decisão local

- **MVP = auditoria de eventos**, não time-series completa
- Alinhado a ADR-002: não persistir cada `cmd_vel`
- Dados pessoais mínimos (ADR-005)
- **Retenção 90 dias** + **purge automático** (scheduler)
- Tipos MVP: `login_success`, `login_failure`, `logout`, `profile_switch`, `goal_pose`, `parameter_change`
- `llm_ask` só se o proxy LLM estiver no MVP

## Opção rejeitada

Mirror completo de bag ROS no PostgreSQL — custo/complexidade incompatível com o TCC e com o objetivo de governo de produto.

Purge apenas manual no MVP — rejeitado: política ficaria só no papel.
