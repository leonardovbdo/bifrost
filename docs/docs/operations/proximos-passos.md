# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-14  
**Estado:** Etapas A–J **DONE** na `main` (J no PR #13). Esteira E–J fechada.

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
| Etapa I — UX hub de governança | **DONE** (`main`, PR #12) |
| Etapa J — UX dual surface | **DONE** (`main`, PR #13) |

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

UI: login → hub claro (`#/`) → **Entrar no console** escuro (`#/console`). Com NARA: `./scripts/bringup-nara-nav.sh`. Admin: `#/admin/*`.  
Detalhes ROS: [`ambiente-local-nara.md`](ambiente-local-nara.md).

---

## Próxima ação imediata

Nenhuma etapa E–J aberta. Dual surface está na `main` (PR #13): login → hub claro (`#/`) → **Entrar no console** (`#/console`). Fora da esteira: ver [`esteira-pos-mvp.md`](esteira-pos-mvp.md) §7.
