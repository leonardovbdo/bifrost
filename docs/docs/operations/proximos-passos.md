# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-09-30  
**Estado:** definição docs (Fases 0–5) **concluída**; skeleton em `apps/backend` **pronto**; implementação de domínio **ainda não iniciada**.

Use este arquivo para retomar o TCC sem reabrir o histórico de chat.

---

## Onde estamos

| Camada | Situação |
|-------|----------|
| ADRs 001–013 | Aceitas / vigentes (001 histórica Node) |
| Specs + feature packs | Documentados (`auth`, `profiles`, `parameters`, `telemetria`, `llm`) |
| Convenções | `api-http`, `git-and-releases`, `testing`, `security`, Clean/DDD |
| Ops | Compose Postgres, retenção audit, observabilidade MVP, CI markdown |
| Diagramas | Sequência session-config, C4, ER |
| Código | Skeleton Spring Boot 4.1.1 / Java 21 — health + Flyway baseline + correlation-id |

Metas locais (não versionadas): `docs/docs/requirements/metas-documentacao.md` (gitignored).

---

## Como retomar em 5 minutos

```bash
# 1) Postgres
cd ~/Desktop/TCC && docker compose up -d

# 2) API skeleton
cd apps/backend && ./mvnw spring-boot:run
# http://localhost:8081/actuator/health

# 3) (Opcional) sim NARA — ver operations/ambiente-local-nara.md
```

Leitura rápida antes de codar:

1. Este arquivo  
2. [`architecture/backend-functional-spec.md`](../architecture/backend-functional-spec.md)  
3. [`diagrams/sequencia-session-config.md`](../diagrams/sequencia-session-config.md)  
4. ADR do módulo que for implementar (links abaixo)

---

## Ordem de implementação (canônica)

Seguir **nesta ordem**. Marcar tasks `DONE` nos `tasks.md` de cada pack ao concluir.

### Etapa A — Identidade e sessão (desbloqueia o resto)

| Ordem | Task | Pack / doc |
|------:|------|------------|
| 1 | AUTH-01…09 | [`features/authentication/tasks.md`](../architecture/backend/features/authentication/tasks.md) · [ADR-008](../adrs/ADR-008-jwt-spring-security.md) |
| 2 | PROF-01…08 | [`features/robot-profiles/tasks.md`](../architecture/backend/features/robot-profiles/tasks.md) · [ADR-010](../adrs/ADR-010-robot-profile-schema.md) |
| 3 | Fechar `GET /me/session-config` + `PUT /me/active-profile` | [ADR-009](../adrs/ADR-009-session-config.md) |

**Critério de pronto A:** login com cookies, admin seed por env, seed `nara-sim-alfa`, session-config com permissions + limits defaults, ACL 403.

### Etapa B — Parameters + auditoria

| Ordem | Task | Pack / doc |
|------:|------|------------|
| 4 | PAR-01…06 | [`features/parameters/tasks.md`](../architecture/backend/features/parameters/tasks.md) |
| 5 | AUD-01…06 (+ purge 90d) | [`features/telemetria/tasks.md`](../architecture/backend/features/telemetria/tasks.md) · [ops retenção](auditoria-retencao.md) |

**Critério de pronto B:** `limits.teleop` vindos do merge global←user; eventos de login/profile_switch/parameter_change; job de purge.

### Etapa C — LLM + CI backend

| Ordem | Task | Pack / doc |
|------:|------|------------|
| 6 | LLM-01…05 | [`features/llm/tasks.md`](../architecture/backend/features/llm/tasks.md) · [ADR-011](../adrs/ADR-011-llm-proxy.md) |
| 7 | Workflow `./mvnw verify` no GitHub Actions | [ADR-012](../adrs/ADR-012-ci-cd.md) |

### Etapa D — Cliente de validação (depois da API mínima)

| Ordem | Item | Nota |
|------:|------|------|
| 8 | Protótipo UI leve do TCC | Consome cookies + session-config; conecta rosbridge/vídeo (híbrido) |
| 9 | Demo integrada com `noblenara` sim | [`ambiente-local-nara.md`](ambiente-local-nara.md) |

---

## Próxima ação imediata (quando voltar a codar)

**Começar em AUTH-01:** migration Flyway `users` (+ `last_active_profile_id`), alinhada ao ER em [`diagrams/er-postgres.md`](../diagrams/er-postgres.md) e à functional spec.

Não reinventar contratos: TTLs JWT parametrizáveis, cookies httpOnly, `ROLE_*`, URLs só no profile, enums de capabilities/topics fechados.

---

## Checklist de higiene ao retomar

- [ ] `docker compose up -d` e health da API  
- [ ] Ler ADRs do módulo (008/009/010…)  
- [ ] Atualizar `tasks.md` da feature (`TODO` → `DONE`)  
- [ ] Testes: unitários + integração Testcontainers quando tocar Postgres ([`conventions/testing.md`](../conventions/testing.md))  
- [ ] Entrada no [`CHANGELOG.md`](../../../CHANGELOG.md) se for release/tag  
- [ ] Não commitar `.env` / segredos ([`conventions/security.md`](../conventions/security.md))

---

## Fora do MVP (não bloquear as etapas A–C)

- Escopos de parameters `role` / `profile`  
- Enforçamento server-side de `cmd_vel`  
- Histórico de chat LLM no Postgres  
- Prometheus / OpenTelemetry  
- Biometria / face login (ADR-005)  
- Excalidraw / diagramas fora de Mermaid  

---

## Contato com o estado do git

Quando for versionar o pacote docs+skeleton atual: bump SemVer **MINOR** (ex. `v0.4.0`) e atualizar `CHANGELOG.md` — ver [`conventions/git-and-releases.md`](../conventions/git-and-releases.md).
