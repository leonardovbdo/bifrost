# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-10  
**Estado:** Etapas A–D na `main`. Esteira normativa: [`esteira-pos-mvp.md`](esteira-pos-mvp.md). Próxima ação de código: Etapa E.

---

## Onde estamos

| Item | Status |
|------|--------|
| Etapa A — auth/session/profiles | **DONE** (`main`) |
| Etapa B — parameters + audit | **DONE** |
| Etapa C — LLM + CI Maven | **DONE** |
| Etapa D — UI / demo NARA (+ POV câmera) | **DONE** |
| Etapa E — capabilities console (goal/mapa/bateria) | **TODO** ← próxima |
| Etapas F–H | Ver [esteira pós-MVP](esteira-pos-mvp.md) |

**Documento normativo da fila:** [`esteira-pos-mvp.md`](esteira-pos-mvp.md)  
(processo de PR/review, DoD por tipo de etapa, escopos E–H, o que fica fora).

---

## Como retomar

```bash
git checkout main && git pull
git checkout -b feat/etapa-e-console-capabilities   # se ainda não existir
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET="$(openssl rand -base64 48)"
cd apps/backend && ./mvnw spring-boot:run
# demo com placeholder: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# outro terminal:
cd apps/web && npm install && npm run dev
```

UI: `http://localhost:5173` (proxy `/api` → `:8081`)

Com NARA + `bridgelaunch`: teleop WASD + câmera (seletor usuário/frente) via session-config.  
Detalhes ROS: [`ambiente-local-nara.md`](ambiente-local-nara.md).

---

## Próxima ação imediata

1. Implementar **Etapa E** conforme [`esteira-pos-mvp.md`](esteira-pos-mvp.md) §3 (mínimo: input goal + OccupancyGrid + chip bateria).  
2. Branch `feat/etapa-e-console-capabilities` a partir de `main`.  
3. Mesma esteira: implementar → PR → `@cursoragent review` → corrigir achados coerentes → merge.
