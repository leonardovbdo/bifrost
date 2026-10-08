# Spec — Telemetria e Auditoria

## Auditoria (MVP)

Tabela `audit_events` conforme functional-spec.

### Retenção e purge (fechado)

| Item | Decisão |
|------|---------|
| Retenção padrão | **90 dias** (configurável, ex. `BIFROST_AUDIT_RETENTION_DAYS`) |
| Purge | **Automático** no MVP — job agendado (ex. diário) remove `created_at` anteriores à janela |
| Fallback | SQL/manual documentado em ops, só emergência |
| Teleop contínuo | **Não** auditar cada `cmd_vel` |

### Tipos obrigatórios no MVP

| type | Quem registra | Payload típico |
|------|---------------|----------------|
| `login_success` | backend | ip/user-agent resumido |
| `login_failure` | backend | username tentado (sem senha) |
| `logout` | backend | — |
| `profile_switch` | backend | profile id/slug |
| `goal_pose` | client (allowlist) + user do JWT | x,y,yaw resumidos |
| `parameter_change` | backend | key, scope |
| `llm_ask` | backend | modelo, tamanhos; sem prompt completo |

### Tipos reservados (se a feature existir)

| type | Quando |
|------|--------|
| *(nenhum reservado crítico)* | — |

`llm_ask` entrou no MVP com ADR-011.

### Regras

1. Client só pode postar tipos allowlist (`goal_pose` no MVP)
2. Backend enriquece `user_id` a partir do JWT (ignora spoof)
3. Retenção 90 dias — alinhado ADR-005 (minimização)
4. Sem PII desnecessária no payload; nunca senha/token
5. Listagem global: só `admin`

## Telemetria amostrada (evolução)

Tabela futura `telemetry_samples` (`profile_id`, `metric`, `value_json`, `captured_at`).  
Ingestão via job/bridge — **não** no caminho crítico do teleop.

## Critérios de aceite (MVP auditoria)

- [x] Login/logout/failure geram evento server-side
- [x] Profile switch e parameter_change geram evento
- [x] Goal reportado pelo client aparece na listagem admin
- [x] Job de purge remove eventos fora da janela de 90 dias
- [x] Operator não lista auditoria global
- [x] Nenhum evento por tick de teleop
