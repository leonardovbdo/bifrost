# Spec — Robot Profiles

## Escopo

- Persistência de robot profiles
- Associação user ↔ profiles
- Exposição na session-config
- Seed do estudo de caso NARA simulação

## Schema lógico de `topics`

Chaves lógicas sugeridas:

`cmd_vel`, `camera_link`, `camera_user`, `scan`, `map`, `odom`, `battery`, `goal_pose`, `joint_states`

Valores: nomes absolutos ROS (ex.: `/noblenara/alfa/cmd_vel`).

## Capabilities

Lista livre versionável, exemplos: `teleop`, `cameras`, `slam`, `nav2`, `battery`, `rosapi`.

## Regras

1. Profile inativo não entra em session-config
2. Non-admin só lista profiles com ACL
3. Mudança de profile ativo no client deve ser permitida só dentro de `allowedProfiles`
4. URLs de rosbridge/vídeo podem ser sobrescritas por profile

## Critérios de aceite

- [ ] Seed NARA retorna tópicos compatíveis com a simulação validada
- [ ] Admin cria profile para outra tecnologia sem alterar código de auth
- [ ] Operator sem ACL não vê profile alheio
