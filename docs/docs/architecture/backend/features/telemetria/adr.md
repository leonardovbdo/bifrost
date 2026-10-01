# ADR da feature — Telemetria / Auditoria

## Decisão local

- **MVP = auditoria de eventos**, não time-series completa
- Alinhado a ADR-002: não persistir cada `cmd_vel`
- Dados pessoais mínimos (ADR-005)

## Opção rejeitada

Mirror completo de bag ROS no PostgreSQL — custo/complexidade incompatível com o TCC e com o objetivo de governo de produto.
