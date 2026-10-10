# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-10-10  
**Estado:** Etapas A–D na `main`. Trabalho atual: Etapa E (`feat/etapa-e-console-capabilities`). Esteira normativa: [`esteira-pos-mvp.md`](esteira-pos-mvp.md).

---

## Onde estamos

| Item | Status |
|------|--------|
| Etapa A — auth/session/profiles | **DONE** (`main`) |
| Etapa B — parameters + audit | **DONE** |
| Etapa C — LLM + CI Maven | **DONE** |
| Etapa D — UI / demo NARA (+ POV câmera) | **DONE** |
| Etapa E — capabilities console (goal/mapa/bateria) | **em progresso** (PR) |
| Etapas F–H | Ver [esteira pós-MVP](esteira-pos-mvp.md) |

**Documento normativo da fila:** [`esteira-pos-mvp.md`](esteira-pos-mvp.md)  
(processo de PR/review, DoD por tipo de etapa, escopos E–H, o que fica fora).

Branch atual: `feat/etapa-e-console-capabilities`

---

## Como retomar

```bash
git checkout main && git pull
git checkout feat/etapa-e-console-capabilities
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET="$(openssl rand -base64 48)"
cd apps/backend && ./mvnw spring-boot:run
# outro terminal:
cd apps/web && npm install && npm run dev
```

UI: `http://localhost:5173` (proxy `/api` → `:8081`)

Com NARA + `bridgelaunch`: teleop WASD + câmera (seletor usuário/frente) via session-config, goal_pose + audit, OccupancyGrid, scan LiDAR, chip bateria.  
Detalhes ROS: [`ambiente-local-nara.md`](ambiente-local-nara.md).

---

## Próxima ação imediata

1. Fechar **Etapa E** (PR + `@cursoragent review` + merge).  
2. Abrir `feat/etapa-f-admin-users` e seguir [`esteira-pos-mvp.md`](esteira-pos-mvp.md).
