# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-01  
**Estado:** Etapa D (UI / demo NARA) na branch `feat/etapa-d-ui-demo`. Etapas A–C nas PRs #1–#3.

---

## Onde estamos

| Item | Status |
|------|--------|
| Etapa A — auth/session/profiles | DONE (PR #1) |
| Etapa B — parameters + audit | DONE (PR #2) |
| Etapa C — LLM + CI Maven | DONE (PR #3 / `feat/etapa-c-llm-ci`) |
| Etapa D — UI / demo NARA | DONE nesta branch |

Branch atual de trabalho: `feat/etapa-d-ui-demo`

---

## Como retomar

```bash
git checkout feat/etapa-a-auth-session
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET="$(openssl rand -base64 48)"
cd apps/backend && ./mvnw spring-boot:run
# demo com placeholder: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# outro terminal:
cd apps/web && npm install && npm run dev
```

UI: `http://localhost:5173` (proxy `/api` → `:8081`)

Com NARA + bridgelaunch: teleop WASD + câmera via session-config.

---

## Próxima ação imediata

Polimento / demos / merge das PRs em cadeia (A→B→C→D), ou evoluções UI (mapa, goal_pose auditado).
