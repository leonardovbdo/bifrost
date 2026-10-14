# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-13  
**Estado:** Etapas A–G na `main`. Etapa H **IN PROGRESS** (`feat/etapa-h-telemetry`). Ops NARA (PR #10) na `main`.

---

## Onde estamos

| Item | Status |
|------|--------|
| Etapa A — auth/session/profiles | **DONE** (`main`) |
| Etapa B — parameters + audit | **DONE** |
| Etapa C — LLM + CI Maven | **DONE** |
| Etapa D — UI / demo NARA (+ POV câmera) | **DONE** |
| Etapa E — capabilities console (goal/mapa/bateria) | **DONE** (`main`, PR #7) |
| Etapa F — admin + users | **DONE** (`main`, PR #8) |
| Etapa G — hardening | **DONE** (`main`, PR #9) |
| Etapa H — telemetria samples | **IN PROGRESS** (`feat/etapa-h-telemetry`) |

**Documento normativo da fila:** [`esteira-pos-mvp.md`](esteira-pos-mvp.md).

Branch atual: `feat/etapa-h-telemetry`

---

## Como retomar

```bash
git checkout main && git pull
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET="$(openssl rand -base64 48)"
cd apps/backend && ./mvnw spring-boot:run
# outro terminal:
cd apps/web && npm install && npm run dev
```

UI: `http://localhost:5173` (proxy `/api` → `:8081`)

Com NARA: preferir `./scripts/bringup-nara-nav.sh` (world → robot → bridge → slam → nav2 → relay `goal_pose`→Nav2). UI: teleop WASD + câmera (usuário/frente), goal_pose + audit, OccupancyGrid, scan LiDAR, chip bateria. Admin: `#/admin/*` (só role admin).  
Detalhes ROS: [`ambiente-local-nara.md`](ambiente-local-nara.md).

---

## Próxima ação imediata

**Etapa H** (H-01 migration `telemetry_samples`, H-02 ingestão amostrada, H-03 query admin) em `feat/etapa-h-telemetry` — ver [`esteira-pos-mvp.md`](esteira-pos-mvp.md) §6.
