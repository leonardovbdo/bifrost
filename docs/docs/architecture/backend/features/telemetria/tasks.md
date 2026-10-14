# Tasks — Telemetria / Auditoria

| ID | Task | Status |
|----|------|--------|
| AUD-01 | Migration `audit_events` | `DONE` |
| AUD-02 | Emitter interno (login etc.) | `DONE` |
| AUD-03 | `POST /audit/events` allowlist | `DONE` |
| AUD-04 | `GET /audit/events` admin + filtros | `DONE` |
| AUD-05 | Job purge + config retenção 90 dias | `DONE` |
| AUD-06 | Ops: `operations/auditoria-retencao.md` | `DONE` |
| TEL-01 | Migration `telemetry_samples` | `DONE` — Etapa H (`feat/etapa-h-telemetry`) |
| TEL-02 | Ingestão amostrada `POST /telemetry/samples` | `DONE` — métricas `battery`/`pose`; rejeita `cmd_vel`; intervalo mínimo |
| TEL-03 | Query admin `GET /telemetry/samples` + UI admin | `DONE` — H-03 |
