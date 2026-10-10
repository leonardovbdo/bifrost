# Bifrost Web — protótipo de console NARA

Cliente leve (Vite + React + TypeScript) para validar a API Bifrost no modelo híbrido:

1. Login com cookies JWT (`credentials: include`)
2. `GET /me/session-config` → profile, permissions, limits
3. Rosbridge (roslib) + teleop WASD respeitando `limits.teleop`
4. Stream de câmera via `videoBaseUrl` (seletor `camera_user` / `camera_link`)
5. Goal Nav2: audit `goal_pose` (com `robotProfileId`) → então PoseStamped se `canSendGoal`
6. Mapa OccupancyGrid (`topics.map`) + Scan LiDAR opcional + chip bateria
7. Assistente via `POST /llm/ask` (admin/operator)

## Demo Etapa E (capabilities)

Com sim + `bridgelaunch` + API + `npm run dev`:

1. Login qualquer papel → rosbridge abre para telemetria; chip **bat** / painel **Mapa**.
2. Painel **Mapa** → OccupancyGrid (`topics.map`, obrigatório no DoD de E).
3. Painel **Scan** (recomendado) → polar LiDAR se o tópico publicar.
4. Painel **Goal** (admin/operator) → x/y/yaw (vírgula OK; blank rejeitado) → audit 201 → publish.
5. Viewer: sem teleop/goal; mapa/scan/bateria visíveis quando conectado.

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
