# Spec — Robot Profiles

## Escopo

- Persistência de robot profiles
- Associação user ↔ profiles (ACL)
- Exposição na session-config (ADR-009)
- Schema fechado capabilities/topics + frames (ADR-010)
- Seed do estudo de caso NARA **sim local** (`nara-sim-alfa`)

## Schema — `capabilities` (enum)

`teleop` | `cameras` | `slam` | `nav2` | `battery` | `rosapi`

## Schema — chaves de `topics` (enum)

`cmd_vel` | `camera_link` | `camera_user` | `scan` | `map` | `odom` | `battery` | `goal_pose` | `joint_states`

Valores: nomes absolutos ROS (ex.: `/noblenara/alfa/cmd_vel`).

## Schema — `frames` (TF)

JSON com nomes lógicos → frame ROS (ex.: `map`, `odom`, `base`, `camera`).  
IHM simples **pode ignorar**; campo existe para evolução (pose no mapa, etc.).

## URLs

`rosbridge_url` e `video_base_url` **obrigatórios por profile** (sem herança de env global).

## Regras

1. Profile inativo não entra em session-config
2. Non-admin só lista profiles com ACL
3. Ativo só dentro de `allowedProfiles`; preferência salva no backend
4. Save rejeita capability/topic desconhecido
5. Save rejeita inconsistência (ex.: `teleop` sem `cmd_vel`) — ver ADR-010
6. Pedido a profile sem ACL → 403

## Seed `nara-sim-alfa`

- `environment: sim`
- `rosbridge_url: ws://localhost:9090`
- `video_base_url: http://localhost:8080`
- Namespace de tópicos `/noblenara/alfa/...`

## Critérios de aceite

- [ ] Seed NARA retorna tópicos/URLs compatíveis com simulação local validada
- [ ] Admin cria profile para outra tecnologia sem alterar código de auth
- [ ] Operator sem ACL recebe 403 ao tentar ativar profile alheio
- [ ] Validação bloqueia profile `teleop` sem `cmd_vel`
