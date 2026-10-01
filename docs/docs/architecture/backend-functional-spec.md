# Backend — Functional Spec

**Projeto:** TCC Backend NARA  
**Versão:** 0.1  
**Status:** Rascunho para implementação futura  
**Stack:** Java + Spring Boot + PostgreSQL (ADR-006 + ADR-007)  

---

## 1. Visão

API REST de governo: identidade, robot profiles, parâmetros, session-config, auditoria e proxy LLM. Integração ROS conforme ADR-002 (híbrida).

Prefixo sugerido: `/api/v1`.

---

## 2. Módulos

| Módulo | Responsabilidade |
|--------|------------------|
| `auth` | Login, JWT, hash de senha |
| `users` | CRUD admin de usuários |
| `profiles` | Robot Profiles |
| `params` | Parâmetros e preferências |
| `session` | Agregação `/me/session-config` |
| `audit` | Eventos de auditoria |
| `telemetry` | (evolução) amostras persistidas |
| `llm` | Proxy de assistente |
| `health` | Liveness/readiness |

---

## 3. Modelo de dados (inicial)

### 3.1 `users`

| Campo | Tipo | Notas |
|-------|------|-------|
| id | PK | UUID ou BIGINT |
| username | UNIQUE | |
| email | UNIQUE NULL | opcional no MVP |
| password_hash | string | bcrypt |
| role | ENUM | `admin` \| `operator` \| `viewer` |
| active | bool | |
| created_at / updated_at | timestamps | |

### 3.2 `robot_profiles`

| Campo | Tipo | Notas |
|-------|------|-------|
| id | PK | |
| slug | UNIQUE | ex.: `nara-sim-alfa` |
| display_name | string | |
| project | string | ex.: `noblenara` |
| prefix | string | ex.: `alfa` |
| environment | ENUM | `sim` \| `physical` |
| technology | string | ex.: `wheelchair_nara` |
| capabilities | JSON | |
| topics | JSON | mapa lógico → tópico ROS |
| frames | JSON | opcional |
| rosbridge_url | string | |
| video_base_url | string | |
| active | bool | |

### 3.3 `user_profile_access`

| Campo | Notas |
|-------|-------|
| user_id | FK |
| robot_profile_id | FK |
| UNIQUE(user_id, robot_profile_id) | |

### 3.4 `parameters`

| Campo | Notas |
|-------|-------|
| id | PK |
| scope | `global` \| `role` \| `user` \| `profile` |
| scope_id | nullable |
| key | ex.: `teleop.speed.normal` |
| value_json | JSON |
| UNIQUE(scope, scope_id, key) | |

### 3.5 `audit_events`

| Campo | Notas |
|-------|-------|
| id | PK |
| user_id | FK nullable |
| type | ex.: `login`, `goal_pose`, `profile_switch` |
| robot_profile_id | FK nullable |
| payload_json | resumido |
| created_at | |

---

## 4. Endpoints (contrato inicial)

### Auth

- `POST /api/v1/auth/login` `{ username, password }` → `{ accessToken, expiresIn, user }`
- `GET /api/v1/me` → usuário autenticado
- `GET /api/v1/me/session-config` → papel, profile(s), topics, urls, limits

### Profiles

- `GET /api/v1/robot-profiles` (filtrado pelo acesso do usuário; admin vê todos)
- `GET /api/v1/robot-profiles/:id`
- `POST /api/v1/robot-profiles` (`admin`)
- `PATCH /api/v1/robot-profiles/:id` (`admin`)

### Parameters

- `GET /api/v1/parameters?scope=...`
- `PUT /api/v1/parameters` (`admin` ou dono no escopo `user`)

### Audit

- `POST /api/v1/audit/events` (client autenticado; tipos permitidos)
- `GET /api/v1/audit/events` (`admin`, com filtros)

### LLM

- `POST /api/v1/llm/ask` `{ prompt, context? }` → `{ reply }`

### Health

- `GET /health` → `{ status: "ok" }`

---

## 5. Session-config (contrato)

Exemplo ilustrativo:

```json
{
  "user": { "id": "…", "username": "gipar", "role": "admin" },
  "activeProfile": {
    "id": "…",
    "slug": "nara-sim-alfa",
    "project": "noblenara",
    "prefix": "alfa",
    "environment": "sim",
    "topics": {
      "cmd_vel": "/noblenara/alfa/cmd_vel",
      "camera_link": "/noblenara/alfa/camera_link/image",
      "camera_user": "/noblenara/alfa/camera_user",
      "scan": "/noblenara/alfa/scan_filtered",
      "map": "/noblenara/alfa/map",
      "odom": "/noblenara/alfa/odom",
      "battery": "/noblenara/alfa/battery_status",
      "goal_pose": "/noblenara/alfa/goal_pose"
    },
    "capabilities": ["teleop", "cameras", "slam", "nav2"],
    "rosbridgeUrl": "ws://localhost:9090",
    "videoBaseUrl": "http://localhost:8080"
  },
  "allowedProfiles": ["…"],
  "permissions": {
    "canTeleop": true,
    "canSendGoal": true,
    "canInspectRosapi": true,
    "canManageProfiles": true
  },
  "limits": {
    "teleop": { "linearMax": 0.5, "angularMax": 1.0, "profile": "normal" }
  }
}
```

---

## 6. Regras transversais

- Todas as rotas (exceto login/health) exigem JWT Bearer
- Validação de input (Bean Validation / schemas)
- Erros JSON padronizados `{ error: { code, message } }`
- CORS configurável para a origem do protótipo/client de testes
- Migrations versionadas com Flyway
- Seeds só para desenvolvimento / bootstrap documentado
- Evitar lógica de negócio em stored procedures no MVP

---

## 7. Seed inicial sugerido

- Usuário admin de desenvolvimento (credenciais só em `.env` / seed local)
- Profile `nara-sim-alfa` compatível com a simulação já validada
- Parâmetros default de velocidade (segurança/normal/rápida)

---

## 8. Fora desta spec

- Implementação de código
- Schema SQL final (pode evoluir nas features)
- Autenticação do rosbridge propriamente dita
