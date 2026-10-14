# Bifrost Web — protótipo de console NARA

Cliente leve (Vite + React + TypeScript) para validar a API Bifrost no modelo híbrido:

1. Login com cookies JWT (`credentials: include`)
2. `GET /me/session-config` → **Session Hub** (`#/`): role, profile, capabilities (ADR-010) e matriz de permissões — **sem ROS**
3. **Conectar ao console** (`#/console`) → rosbridge + câmera / teleop / goal / mapa
4. Stream de câmera via `videoBaseUrl` (seletor `camera_user` / `camera_link`)
5. Goal Nav2: audit `goal_pose` → PoseStamped se `canSendGoal` (painel só se permitido)
6. Mapa OccupancyGrid + Scan LiDAR + bateria
7. Assistente via `POST /llm/ask` (só admin/operator; painel oculto para viewer)
8. **Admin** (`#/admin/…`, só `admin`): profiles, parameters, users, audit, telemetry
9. **Desconectar** volta ao hub e encerra ROS

## Demo Etapa I (hub de governança)

1. Login → hub (`#/`): capabilities do profile + matriz permitido/bloqueado; ROS **idle**. Sair no console e entrar de novo também abre o hub.
2. **Conectar ao console NARA** → `#/console` (rosbridge + câmera).
3. Viewer: sem Teleop / Goal / LLM no console; só observação.
4. **Desconectar** → hub de novo.

## Demo Etapa F (admin UI)

Com API + `npm run dev`, login **admin**:

1. Header **Admin** → `#/admin/profiles`: listar/criar/editar robot profile + **Grant** (`POST …/access`).
2. **Parameters**: editar `teleop.presets` / default global; preview de merge (`limits.teleop` da session-config).
3. **Users**: listar/criar; alterar `role` / `active` (`GET`/`POST`/`PATCH /api/v1/users`).
4. **Audit**: `GET /api/v1/audit/events` com filtros type / user / limit.
5. **Telemetry** (Etapa H): `#/admin/telemetry` lista samples; ingestão `POST /api/v1/telemetry/samples` (battery/pose, intervalo mínimo; sem cmd_vel).

Viewer/operator: link Admin ausente; `#/admin` mostra acesso negado.

## Demo Etapa E (capabilities)

Com sim via `./scripts/bringup-nara-nav.sh` na raiz do repo (SLAM, Nav2 e relay `goal_pose`; ver `docs/docs/operations/ambiente-local-nara.md`) + API + `npm run dev`:

1. Login → hub → **Conectar** → rosbridge; status **bat** / painel **Mapa**.
2. Painel **Mapa** → OccupancyGrid (`topics.map`, obrigatório no DoD de E).
3. Painel **Scan** (recomendado) → polar LiDAR se o tópico publicar.
4. Painel **Goal** (admin/operator) → x/y/yaw → audit 201 → publish.
5. Viewer: sem teleop/goal/LLM; mapa/scan/bateria quando conectado.

## Pré-requisitos

- Backend em `http://localhost:8081` (ver `apps/backend`)
- (Opcional) NARA + rosbridge `:9090` + web_video_server `:8080`

## Rodar

```bash
npm install
npm run dev
# http://localhost:5173
```

O Vite faz proxy de `/api` → `8081`. Alternativa: `VITE_API_BASE=http://localhost:8081`.

## Build

```bash
npm run build
```

## Escopo MVP

Não é uma IHM completa: foca em session-config, teleop mínimo e demo do LLM.

Teleop publica `geometry_msgs/msg/Twist` (ROS 2 / Jazzy) e só captura
teclas fora de `input`/`textarea`/`select`.
