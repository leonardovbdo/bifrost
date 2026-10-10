# Operação — Ambiente local NARA + (futuro) Backend TCC

**Versão:** 0.2  
**Status:** Baseado na validação da simulação NARA em 2026-09-29

---

## 1. Repositórios no Desktop

| Pasta | Uso |
|-------|-----|
| `~/Desktop/noblenara` | ROS 2 / simulação (`nara-sim`) — estudo de caso |
| `~/Desktop/TCC` | Docs + `apps/backend` + protótipo UI `apps/web` |

---

## 2. Pré-requisitos (simulação)

- Ubuntu 24.04
- ROS 2 Jazzy (`/opt/ros/jazzy`) + deps Gazebo/Nav2/SLAM conforme tutorial do `noblenara`
- Workspace `nara-sim` compilado (`colcon build --symlink-install`)
- Pacotes: `ros-jazzy-rosbridge-suite`, `ros-jazzy-web-video-server` (para client híbrido)

`~/.bashrc` típico:

```bash
source /opt/ros/jazzy/setup.bash
source ~/Desktop/noblenara/nara-sim/install/setup.bash
export ROS_DOMAIN_ID=0
```

---

## 3. Subir a simulação

Terminal A:

```bash
ros2 launch smartwheelchair worldmuseum.launch.py
```

Terminal B:

```bash
ros2 launch smartwheelchair noblenara.launch.py
```

---

## 4. Ponte ROS (para clients híbridos)

```bash
ros2 launch smartwheelchair bridgelaunch.xml
```

Expõe:

- rosbridge WebSocket `:9090`
- web_video_server `:8080`

---

## 5. Backend TCC + protótipo UI

PostgreSQL local:

```bash
docker compose up -d
```

Detalhes: [`docker-postgres-local.md`](docker-postgres-local.md).

1. PostgreSQL up + migrations Flyway  
2. API em `:8081` + Actuator `/health` (ADR-013)  
3. Protótipo UI: `cd apps/web && npm run dev` → `http://localhost:5173`  
4. Rosbridge permanece `:9090` (híbrido); câmera via web_video_server `:8080`  
   (tópico na query **sem** `%2F`; seletor Usuário/Frente na console)  
5. Esteira do que falta (goal/mapa/admin/…): [`esteira-pos-mvp.md`](esteira-pos-mvp.md)

### Console Etapa E (capabilities)

Com qualquer login autenticado e rosbridge up:

- Header: chip de bateria (`topics.battery` / `sensor_msgs/BatteryState`)
- Painel **Mapa**: OccupancyGrid (`topics.map` / `nav_msgs/OccupancyGrid`)
- Painel **Scan** (recomendado): polar LiDAR (`topics.scan` / `LaserScan`)
- Painel **Goal** (admin/operator): x/y/yaw → audit `goal_pose` + `robotProfileId` → PoseStamped
- Câmera: seletor POV `camera_user` / `camera_link`

Detalhes: `apps/web/README.md`.

---

## 6. Encerrar processos

```bash
pkill -f 'gz sim' || true
pkill -f rosbridge_websocket || true
pkill -f web_video_server || true
```

Verificar portas livres conforme o que estiver em uso (`9090`, `8080`, porta da API, etc.).

---

## 7. Problemas conhecidos

| Sintoma | Nota |
|---------|------|
| SSL `packages.ros.org` | Apt do ROS pode precisar do espelho OSUOSL |
| Streams / namespaces | Profile seed usa `/noblenara/alfa/...` |
