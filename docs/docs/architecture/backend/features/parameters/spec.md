# Spec — Parameters

## Escopo

- CRUD/leitura de parameters com escopos `global` e `user`
- Seed dos presets de teleop
- Integração na montagem de `limits` da session-config

## Fora de escopo (MVP)

- Escopos `role` e `profile`
- Enforçamento de `cmd_vel` no backend/ROS
- UI rica de “editor genérico” de qualquer chave (pode ser API-first)

## Modelo

Tabela `parameters` (ver functional spec):

| Campo | Notas |
|-------|--------|
| scope | `global` \| `user` (MVP) |
| scope_id | NULL se global; `user_id` se user |
| key | string estável |
| value_json | JSON |

## Regras

1. Só `admin` cria/atualiza escopo `global`
2. Usuário autenticado atualiza **apenas** o próprio escopo `user`
3. `viewer` pode ter preferência `user` (ex.: preset), mas `canTeleop=false` continua valendo
4. Merge: para `limits.teleop`, resolver preset efetivo = user.`teleop.activeProfile` se existir, senão global
5. Valores numéricos dos presets vêm do `global` `teleop.presets` (seed); user não altera os números no MVP — só o nome do preset
6. Session-config sempre inclui `limits.teleop` preenchido após o merge

## Endpoints

- `GET /api/v1/parameters?scope=global`
- `GET /api/v1/parameters?scope=user` (próprio)
- `PUT /api/v1/parameters` — body `{ scope, key, value }` com checagem de papel/dono

## Critérios de aceite

- [ ] Seed global com três presets e default `normal`
- [ ] Operator altera `teleop.activeProfile` no escopo user
- [ ] Operator **não** altera `teleop.presets` global
- [ ] session-config reflete o preset efetivo em `limits.teleop`
- [ ] Admin altera presets globais e isso afeta novos session-config
