# ER — PostgreSQL (MVP)

Modelo lógico alinhado à functional spec + ADRs 007–011.

```mermaid
erDiagram
  users ||--o{ user_profile_access : allows
  robot_profiles ||--o{ user_profile_access : granted
  users ||--o| robot_profiles : last_active
  users ||--o{ parameters : user_scope
  users ||--o{ audit_events : generates
  robot_profiles ||--o{ audit_events : context

  users {
    uuid id PK
    string username UK
    string email UK
    string password_hash
    string role
    boolean active
    uuid last_active_profile_id FK
    timestamptz created_at
    timestamptz updated_at
  }

  robot_profiles {
    uuid id PK
    string slug UK
    string display_name
    string project
    string prefix
    string environment
    string technology
    json capabilities
    json topics
    json frames
    string rosbridge_url
    string video_base_url
    boolean active
  }

  user_profile_access {
    uuid user_id FK
    uuid robot_profile_id FK
  }

  parameters {
    uuid id PK
    string scope
    uuid scope_id
    string key
    json value_json
  }

  audit_events {
    uuid id PK
    uuid user_id FK
    string type
    uuid robot_profile_id FK
    json payload_json
    timestamptz created_at
  }
```

Notas:

- `parameters.scope`: `global` | `user` no MVP (`scope_id` null se global)
- Refresh tokens (se persistidos) podem ganhar tabela própria na implementação — fora deste ER mínimo
