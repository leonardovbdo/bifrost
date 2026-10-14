# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-13  
**Estado:** Etapas A–H **DONE** na `main`. Etapa I (UX hub de governança) **IN PROGRESS** (`feat/ux-governance-shell`).

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
| Etapa I — UX hub de governança | **IN PROGRESS** (`feat/ux-governance-shell`) |

**Documento normativo da fila:** [`esteira-pos-mvp.md`](esteira-pos-mvp.md).

Branch de trabalho: `feat/ux-governance-shell`

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

UI: login → **Session Hub** (`#/`) → **Conectar** (`#/console`). Com NARA: `./scripts/bringup-nara-nav.sh`. No console: teleop WASD + câmera, goal_pose + audit, mapa/scan/bateria. Admin: `#/admin/*`.  
Detalhes ROS: [`ambiente-local-nara.md`](ambiente-local-nara.md).

---

## Próxima ação imediata

**Etapa I** — hub de sessão (governança) antes de conectar ao console ROS; ver [`esteira-pos-mvp.md`](esteira-pos-mvp.md).
