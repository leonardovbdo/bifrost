# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-10  
**Estado:** Etapas A–G na `main`. Esteira normativa E–G **DONE**. H opcional: [`esteira-pos-mvp.md`](esteira-pos-mvp.md).

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
| Etapa H — telemetria samples | opcional — ver [esteira](esteira-pos-mvp.md) |

**Documento normativo da fila:** [`esteira-pos-mvp.md`](esteira-pos-mvp.md).

Branch atual: `main`

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

Com NARA + `bridgelaunch`: teleop WASD + câmera (seletor usuário/frente) via session-config, goal_pose + audit, OccupancyGrid, scan LiDAR, chip bateria. Admin: `#/admin/*` (só role admin).  
Detalhes ROS: [`ambiente-local-nara.md`](ambiente-local-nara.md).

---

## Próxima ação imediata

Esteira E–G fechada. Se sobrar tempo: **Etapa H** (telemetria persistida) conforme [`esteira-pos-mvp.md`](esteira-pos-mvp.md) §6 — opcional, não bloqueia demo/defesa.
