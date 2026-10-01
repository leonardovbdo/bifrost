# Sequência — Login → session-config → ROS (híbrido)

Fluxo canônico do MVP (ADR-002, ADR-008, ADR-009).

```mermaid
sequenceDiagram
  autonumber
  actor U as Operador
  participant UI as Protótipo UI
  participant API as Bifrost API
  participant DB as PostgreSQL
  participant RB as Rosbridge
  participant VV as web_video_server

  U->>UI: Login (username/password)
  UI->>API: POST /api/v1/auth/login
  API->>DB: Validar user + role
  API-->>UI: Set-Cookie access/refresh + user
  UI->>API: GET /api/v1/me/session-config
  API->>DB: ACL profiles + ativo + parameters
  API-->>UI: schemaVersion, activeProfile, permissions, limits
  UI->>RB: WebSocket (rosbridgeUrl do profile)
  UI->>VV: HTTP vídeo (videoBaseUrl + topics)
  Note over UI,RB: Twist/goals no browser; API não media cada cmd_vel
  U->>UI: Troca de robot profile
  UI->>API: PUT /api/v1/me/active-profile
  API->>DB: Salva last_active_profile_id + audit
  API-->>UI: 204/200
  UI->>API: GET /api/v1/me/session-config
  API-->>UI: Novo activeProfile (URLs/topics)
```
