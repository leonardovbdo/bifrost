# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-14  
**Estado:** Etapas A–H **DONE** na `main` (H no PR #11). Ops NARA (PR #10) na `main`. Esteira E–H fechada.

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
| Etapa H — telemetria samples | **DONE** (`main`, PR #11) |

**Documento normativo da fila:** [`esteira-pos-mvp.md`](esteira-pos-mvp.md).

Branch de trabalho: `main`

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

Nenhuma etapa E–H aberta. Telemetria amostrada está na `main` (PR #11): `POST/GET /api/v1/telemetry/samples` e `#/admin/telemetry`. Fora da esteira: ver [`esteira-pos-mvp.md`](esteira-pos-mvp.md) §7.
