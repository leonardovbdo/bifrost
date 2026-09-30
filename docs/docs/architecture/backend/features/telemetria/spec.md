# Spec — Telemetria e Auditoria

## Auditoria (MVP)

Tabela `audit_events` conforme functional-spec.

### Tipos iniciais

| type | Quem envia | Payload típico |
|------|------------|----------------|
| `login_success` | backend | ip/user-agent resumido |
| `login_failure` | backend | username tentado |
| `profile_switch` | client ou backend | profile id |
| `goal_pose` | client | x,y,yaw resumidos |
| `llm_ask` | backend | tamanho prompt / modelo |
| `parameter_change` | backend | key |

### Regras

1. Client só pode postar tipos allowlist
2. Backend enriquece `user_id` a partir do JWT (ignora spoof)
3. Retention padrão sugerida: 90 dias (ajustável) — política LGPD ADR-005
4. Sem PII desnecessária no payload

## Telemetria amostrada (evolução)

Tabela futura `telemetry_samples` (`profile_id`, `metric`, `value_json`, `captured_at`).  
Ingestão via job/bridge — **não** no caminho crítico do teleop.

## Critérios de aceite (MVP auditoria)

- [ ] Login gera evento server-side
- [ ] Goal reportado pelo client aparece na listagem admin
- [ ] Operator não lista auditoria global
