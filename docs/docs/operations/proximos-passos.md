# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-01  
**Estado:** Etapa B (parameters + audit) na branch `feat/etapa-b-parameters-audit`. Etapa A na PR #1.

---

## Onde estamos

| Item | Status |
|------|--------|
| Etapa A — auth/session/profiles | DONE (PR #1) |
| Etapa B — parameters + audit | DONE nesta branch |
| Etapa C — LLM + CI Maven | **TODO** |
| Etapa D — UI / demo NARA | TODO |

Branch atual de trabalho: `feat/etapa-b-parameters-audit`

---

## Como retomar

```bash
git checkout feat/etapa-b-parameters-audit
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET=change-me-bifrost-dev-secret-at-least-32-chars
cd apps/backend && ./mvnw spring-boot:run
```

Novos endpoints: `/api/v1/parameters`, `/api/v1/audit/events`

---

## Próxima ação imediata

**Etapa C** — proxy LLM (ADR-011) + workflow GitHub Actions `./mvnw verify`.
