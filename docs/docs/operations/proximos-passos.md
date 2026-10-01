# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-01  
**Estado:** Etapa C (LLM + CI Maven) na branch `feat/etapa-c-llm-ci`. Etapas A/B nas PRs #1/#2.

---

## Onde estamos

| Item | Status |
|------|--------|
| Etapa A — auth/session/profiles | DONE (PR #1) |
| Etapa B — parameters + audit | DONE (PR #2 / `feat/etapa-b-parameters-audit`) |
| Etapa C — LLM + CI Maven | DONE nesta branch |
| Etapa D — UI / demo NARA | TODO |

Branch atual de trabalho: `feat/etapa-c-llm-ci`

---

## Como retomar

```bash
git checkout feat/etapa-c-llm-ci
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET=change-me-bifrost-dev-secret-at-least-32-chars
# opcional: BIFROST_LLM_API_KEY=... (sem key usa stub)
cd apps/backend && ./mvnw spring-boot:run
```

Novos endpoints: `POST /api/v1/llm/ask`  
CI: `.github/workflows/backend.yml` (`./mvnw verify`)

---

## Próxima ação imediata

**Etapa D** — UI / demo NARA (session-config → Foxglaze/teleop; assistente via `/llm/ask`).
