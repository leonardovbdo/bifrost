# ADR-010 — Schema de Robot Profile (capabilities, topics, frames, seed NARA)

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  
**Relacionadas:** ADR-003 (modelo), ADR-009 (session-config)

---

## Contexto

A ADR-003 definiu a entidade Robot Profile, mas `capabilities` / `topics` / `frames` ainda estavam flexíveis demais para implementação segura. É preciso fechar enums, validação e o seed do estudo de caso (sim local).

---

## Decisão

### Capabilities — enum fechado (MVP)

Valores permitidos:

`teleop`, `cameras`, `slam`, `nav2`, `battery`, `rosapi`

Incluir valor fora da lista → rejeitar save (400).

### Topics — chaves enum fechado (MVP)

Chaves lógicas permitidas:

`cmd_vel`, `camera_link`, `camera_user`, `scan`, `map`, `odom`, `battery`, `goal_pose`, `joint_states`

Valores: strings de tópicos ROS absolutos (ex.: `/noblenara/alfa/cmd_vel`).  
Chave desconhecida → rejeitar save.

### Frames (TF) — presentes no schema

- Campo `frames` (JSON) no profile e exposto na session-config.
- Chaves lógicas mínimas sugeridas no MVP: `map`, `odom`, `base`, `camera` (valores = nomes TF reais).
- **Consumo na IHM é opcional:** um protótipo simples (teleop/câmera/goal) pode ignorar `frames` sem quebrar o contrato.

### Validação de consistência

Falhar o save se a ficha for inconsistente, por exemplo:

| Capability | Exige tópico |
|------------|--------------|
| `teleop` | `cmd_vel` |
| `cameras` | ao menos `camera_link` ou `camera_user` |
| `nav2` | `goal_pose` (e recomendado `map`) |
| `slam` | `map` (e recomendado `scan`) |
| `battery` | `battery` |

`rosbridge_url` e `video_base_url` **obrigatórios** no profile (ADR-009: URLs só no profile).

### Seed `nara-sim-alfa` (estudo de caso)

| Campo | Valor |
|-------|--------|
| slug | `nara-sim-alfa` |
| project / prefix | `noblenara` / `alfa` |
| environment | `sim` |
| technology | `wheelchair_nara` |
| rosbridge_url | `ws://localhost:9090` |
| video_base_url | `http://localhost:8080` |
| capabilities | `teleop`, `cameras`, `slam`, `nav2`, `battery` |
| topics | namespace `/noblenara/alfa/...` (mapa da functional spec) |
| frames | ex.: `map`→`map`, `odom`→`odom`, `base`→`base_link`, `camera`→`camera_link` |

Escopo atual do seed: **apenas simulação local**. Profile `physical` fica para cadastro posterior.

---

## Consequências

**Positivas**

- Contratos previsíveis para front e validação Bean Validation / schema
- Seed alinhado ao ambiente NARA já validado no notebook
- TF no backend sem forçar UI complexa

**Negativas**

- Novas capabilities/chaves de tópico exigem atualizar enum + docs (release consciente)
- Validação rígida pode incomodar experiments até haver processo de extensão do enum
