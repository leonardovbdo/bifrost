# ADR-009 — Contrato `session-config`

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  
**Relacionadas:** ADR-002, ADR-003, ADR-004, ADR-008, ADR-010

---

## Contexto

No modelo híbrido (ADR-002), o browser conecta ao rosbridge/vídeo com base no que o backend autoriza. É necessário um contrato único `GET /api/v1/me/session-config` para hidratar qualquer client sem hardcode de tópicos/URLs/permissões.

---

## Decisão

### Multi-profile e ativo

- Um usuário pode ter **vários** robot profiles via ACL (`user_profile_access`).
- O **profile ativo** é persistido no backend (ex.: `users.last_active_profile_id`), sempre validado contra a ACL.
- Troca de ativo: endpoint dedicado (ex.: `PUT /api/v1/me/active-profile`) → auditar `profile_switch`.
- Se o ativo salvo for inválido/inativo/sem ACL, o backend escolhe um fallback permitido ou responde erro claro de configuração.

### Conteúdo mínimo do JSON

- `schemaVersion` (inteiro; MVP = `1`)
- Identidade resumida (`user` / role)
- `activeProfile` (ficha completa necessária ao client: topics, capabilities, frames, URLs, …)
- `allowedProfiles` (lista resumida id/slug/displayName)
- `permissions` booleanas **no MVP**:
  - `canTeleop`
  - `canSendGoal`
  - `canInspectRosapi`
  - `canManageProfiles`
- `limits.teleop` **no MVP** (ex.: `linearMax`, `angularMax`, `profile` de velocidade)

### URLs ROS

- `rosbridgeUrl` e `videoBaseUrl` vêm **somente do robot profile** (não de env global).
- Trocar de profile ativo muda as URLs automaticamente.

### Permissions

Derivadas da role (e regras de produto), não editáveis pelo client:

| Role | canTeleop | canSendGoal | canInspectRosapi | canManageProfiles |
|------|-----------|-------------|------------------|-------------------|
| admin | true | true | true | true |
| operator | true | true | true* | false |
| viewer | false | false | false* | false |

\*Ajuste fino de `canInspectRosapi` para operator/viewer pode ser relaxado depois; MVP parte da tabela acima (operator com inspect, viewer sem).

### Erros de ACL

- Pedido a profile existente **sem** permissão → **403 Forbidden** (não mascarar como 404).

### Limits

- Sempre presentes no MVP; valores podem vir de defaults de config e, na Fase 2, de parameters.
- No modelo híbrido, o enforçamento forte no ROS não é garantido pelo Spring; o contract + client disciplinado (e auditoria) são o mínimo do MVP.

---

## Exemplo (ilustrativo)

```json
{
  "schemaVersion": 1,
  "user": { "id": "…", "username": "op1", "role": "operator" },
  "activeProfile": {
    "id": "…",
    "slug": "nara-sim-alfa",
    "rosbridgeUrl": "ws://localhost:9090",
    "videoBaseUrl": "http://localhost:8080",
    "topics": { "cmd_vel": "/noblenara/alfa/cmd_vel" },
    "capabilities": ["teleop", "cameras", "slam", "nav2"],
    "frames": { "map": "map", "base": "base_link" }
  },
  "allowedProfiles": [{ "id": "…", "slug": "nara-sim-alfa", "displayName": "NARA Sim Alfa" }],
  "permissions": {
    "canTeleop": true,
    "canSendGoal": true,
    "canInspectRosapi": true,
    "canManageProfiles": false
  },
  "limits": {
    "teleop": { "linearMax": 0.5, "angularMax": 1.0, "profile": "normal" }
  }
}
```

---

## Consequências

**Positivas**

- Client único contrato para sim/lab via troca de profile
- ACL e preferência de ativo auditáveis
- Limits e permissions explícitas desde o MVP

**Negativas**

- Profiles mal cadastrados (URL errada) quebram a sessão ROS no browser
- 403 revela existência do profile a quem já conhece o id
