# ADR-003 — Modelo Robot Profile (multi-tecnologia)

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  

---

## Contexto

Em interfaces web robóticas é comum derivar tópicos no cliente (`project`, `prefix`, mapas hardcoded) e alternar presets localmente. Isso acopla a UI a convenções de namespace e impede governo centralizado, versionamento e permissões por robô.

O TCC deve permitir **outras tecnologias de robôs autônomos** além da cadeira NARA, sem fork do backend.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — Robot Profile persistido** | Catálogo no PostgreSQL; bindings versionáveis; ACL por perfil; extensível | Exige modelagem e seed inicial NARA |
| B — Hardcode por projeto no backend | Rápido para um único robô | Não escala; repete o problema do front |
| C — Discovery dinâmico só via rosapi | Flexível em runtime | Instável para UI; difícil autorizar a priori; frágil offline |

---

## Decisão

Adotar **Opção A**: entidade **Robot Profile** no PostgreSQL, consumida via API.

Campos mínimos:

- `id`, `slug`, `display_name`
- `project`, `prefix` (namespace)
- `environment`: `sim` | `physical`
- `technology` (ex.: `wheelchair_nara`, `generic_diff_drive`)
- `capabilities` (JSON: teleop, nav2, cameras, slam, battery, …)
- `topics` (JSON: mapa lógico → nome ROS)
- `frames` (JSON; nomes TF — detalhe ADR-010)
- `rosbridge_url`, `video_server_base_url` **obrigatórios no profile** (ADR-009)
- `active` boolean

Schema fechado de `capabilities`/`topics` e seed NARA: **ADR-010**.

O profile **NARA sim (`noblenara`/`alfa`)** é o seed do estudo de caso.

A session-config referencia o profile ativo e a lista de profiles permitidos ao usuário.

---

## Consequências

**Positivas**

- Backendiza presets/tópicos que costumam viver só no client
- Abre caminho para multi-robô sem redesenhar auth
- Facilita testes com perfis `sim` vs `physical`

**Negativas**

- Mudanças de tópicos no ROS exigem atualização do profile
- JSON flexível exige validação de schema na aplicação
