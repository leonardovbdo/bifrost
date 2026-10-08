# Bifrost Web — protótipo de console NARA

Cliente leve (Vite + React + TypeScript) para validar a API Bifrost no modelo híbrido:

1. Login com cookies JWT (`credentials: include`)
2. `GET /me/session-config` → profile, permissions, limits
3. Rosbridge (roslib) + teleop WASD respeitando `limits.teleop`
4. Stream de câmera via `videoBaseUrl` (seletor `camera_user` / `camera_link`)
5. Goal Nav2 (`goal_pose` PoseStamped) + audit `POST /audit/events` se `canSendGoal`
6. Scan LiDAR (`sensor_msgs/LaserScan`) e chip de bateria (`BatteryState`)
7. Assistente via `POST /llm/ask` (admin/operator)

## Demo Etapa E (capabilities)

Com sim + `bridgelaunch` + API + `npm run dev`:

1. Login `admin` / `operator` → chip **bat** no header (atualiza se o tópico existir).
2. Painel **Scan** → nuvem polar do LiDAR (`topics.scan`).
3. Painel **Goal** → informe x/y/yaw → publica PoseStamped e registra audit `goal_pose`.
4. Viewer: goal/teleop desabilitados; câmera e (se conectado) scan/bateria só com roles que abrem rosbridge.

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
