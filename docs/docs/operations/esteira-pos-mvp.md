# Esteira pós-MVP — Bifrost (após Etapas A–D)

**Atualizado:** 2026-10-10  
**Status:** Documento **normativo** da esteira de trabalho até a defesa / demo NARA completa.  
**Pré-requisito:** `main` com Etapas A–D mergeadas (auth, parameters/audit, LLM/CI, console web + câmera POV).

Este arquivo é a **fonte da verdade** do que falta e da ordem de entrega.  
Retomada rápida do ambiente: [`proximos-passos.md`](proximos-passos.md).

---

## 1. Processo (igual à esteira A→D)

1. Branch a partir de `main` (ou da etapa anterior já mergeada): `feat/etapa-e-…`, `feat/etapa-f-…`, …
2. Implementar **só** o escopo da etapa (sem misturar etapas no mesmo PR).
3. Abrir PR → `main` (ou stack se necessário).
4. Pedir `@cursoragent review`; corrigir achados coerentes com specs/ADRs/esteira; CI verde.
5. Merge; atualizar status neste documento + `CHANGELOG.md` + `proximos-passos.md`.
6. Só então abrir a branch da próxima etapa (caminho padrão).

**Definição de pronto (DoD) — por tipo de etapa**

| Tipo | Critério de pronto |
|------|--------------------|
| **E** (UI console) | `npm run lint` + `npm run build` em `apps/web`; demo manual no corpo do PR (goal + mapa + bateria; viewer com `canSendGoal=false` não publica/audita). Docs: `apps/web/README.md` + `ambiente-local-nara.md`. Teste de backend novo **só** se o contrato HTTP mudar (audit `goal_pose` já coberto na Etapa B). |
| **F e G** (API + UI admin / hardening) | IT feliz + 1 negativo relevante (estilo A–C); docs/ops se o fluxo mudar; review + CI. |
| **H** (telemetria) | Migration + teste de repositório/ingestão; H-03 não bloqueia. |
| **Itens opcionais** (ex.: G-06, odom em E-03) | Fora do DoD. |

Checklist comum a toda etapa:

- [ ] Arquivos de spec/tasks/README da feature tocada atualizados (`DONE` / notas) — para E: READMEs acima
- [ ] Critério de teste da tabela acima satisfeito
- [ ] Review Cursor + CI (`backend` / `web` / `markdownlint` conforme paths)
- [ ] Demo manual mínima descrita no corpo do PR

---

## 2. Onde estamos

| Etapa | Escopo | Status |
|-------|--------|--------|
| A — Auth / session / profiles | JWT, ACL, session-config | **DONE** (`main`) |
| B — Parameters + audit | Presets, audit, purge | **DONE** |
| C — LLM + CI | `/llm/ask`, workflows Maven | **DONE** |
| D — UI demo NARA | Console, teleop, câmera, LLM | **DONE** (+ POV câmera) |
| E — Capabilities na console | goal_pose, mapa, bateria | **DONE** (`main`, PR #7) |
| F — Admin + usuários | UI admin + CRUD users | **IN PROGRESS** (`feat/etapa-f-admin-users`, PR pendente) |
| G — Hardening | Rate limit, validação, logs | **TODO** |
| H — Telemetria samples | `telemetry_samples` (evolução) | **TODO** / opcional |

---

## 3. Etapa E — Capabilities na console (prioridade 1)

**Objetivo:** provar na UI o que o profile NARA já declara (`nav2`, `slam`, `battery`), além de teleop/câmera.  
(ADR-010: `slam` exige tópico `map`; `scan` é recomendado, não capability.)

| ID | Entrega (mínimo) | Notas |
|----|------------------|-------|
| E-01 | Input **x/y/yaw** → `goal_pose` | Obrigatório. Clique no mapa só se o viewer de `map` existir. Só com `permissions.canSendGoal`. Publish `geometry_msgs/msg/PoseStamped` no tópico da session-config + `POST /api/v1/audit/events` `{ type: "goal_pose", payload: { x, y, yaw } }` (com `robotProfileId` quando disponível). |
| E-02 | Viewer **OccupancyGrid** (`map`) | Obrigatório (`slam`). LaserScan (`scan`) recomendado, não bloqueia o DoD. |
| E-03 | Chip **bateria** | Obrigatório. `odom` opcional, fora do DoD. |
| E-04 | Docs de demo | `apps/web/README.md` + `ambiente-local-nara.md` |

**Fora de E:** CRUD admin, users API, rate limit.

**Branch sugerida:** `feat/etapa-e-console-capabilities`  
**Base:** `main`

---

## 4. Etapa F — Admin na UI + CRUD de usuários

**Objetivo:** governança visível sem depender só de seed/`curl`.

| ID | Entrega | Notas |
|----|---------|--------|
| F-01 | API **users** (admin) | Mínimo: `GET`/`POST /api/v1/users`, `PATCH /api/v1/users/{id}` com `active` e/ou `role`; nunca expor hash. (Functional spec §2 nomeia o módulo; §4 ainda não lista o HTTP — este item **é** o contrato.) |
| F-02 | UI admin **profiles** | Listar/criar/editar profile + grant (`POST /api/v1/robot-profiles/{id}/access`). Listar/revogar ACL fica **fora de F** (API ainda não existe). Depende de F-01 para escolher usuário no grant. |
| F-03 | UI admin **parameters** | Editar presets globais / ver merge |
| F-04 | UI admin **users** | Consome F-01; viewer/operator sem acesso |
| F-05 | UI **audit** (admin) | Lista `GET /audit/events` com filtros básicos |

**Branch sugerida:** `feat/etapa-f-admin-users`  
**Base:** `main` após merge de E (F **não** depende de código de E; pode nascer de `main` em paralelo. Stack E→F só para adiantar.)

---

## 5. Etapa G — Hardening

**Objetivo:** fechar achados de review / segurança de demo na LAN.

| ID | Entrega | Notas |
|----|---------|--------|
| G-01 | Rate limit `/llm/ask` | Spec LLM recomenda; 429 + audit opcional |
| G-02 | Rate limit / teto em `POST /audit/events` | Evitar flood de `goal_pose` (depende de E existir na demo) |
| G-03 | `@Valid` + schema URLs em PATCH profiles | `ws`/`wss`, `http`/`https`; 400 em vez de 500 |
| G-04 | `@Size` em `LoginRequest` | Mitigar bcrypt DoS + truncar username no audit |
| G-05 | Log de `Exception` no `GlobalExceptionHandler` | Sem vazar stack no body |
| G-06 | (Opcional) retry câmera após `onError` | Botão “tentar de novo” sem reload; fora do DoD |

**Branch sugerida:** `feat/etapa-g-hardening`  
**Base:** `main` após F no caminho padrão (G-01/G-02 podem adiantar se a defesa apertar — ver §8).

---

## 6. Etapa H — Telemetria persistida (opcional / se sobrar tempo)

| ID | Entrega | Notas |
|----|---------|--------|
| H-01 | Draft migration `telemetry_samples` | Já em `features/telemetria/tasks.md` (`TEL-01`) |
| H-02 | Ingestão amostrada (não cmd_vel por tick) | Spec telemetria |
| H-03 | Query admin básica | Fora do caminho crítico da defesa; não bloqueia DoD de H |

Não bloqueia demo NARA se E–G (ou o corte de defesa) estiverem fechadas.  
Não confundir com “FASE 2” do inventário de IHM — é evolução do módulo de telemetria.

---

## 7. Explicitamente fora desta esteira (não abrir PR “só por isso”)

| Item | Onde está documentado |
|------|------------------------|
| Auth do rosbridge / enforcement de `cmd_vel` no backend | Functional spec §8 (fora do MVP de implementação) |
| IHM completa tipo `noblenara-ihm` | Fora do escopo Bifrost console |
| Histórico de chat LLM no Postgres | Spec LLM: fora do MVP |
| Deploy produção / multi-robô físico | Fora do MVP local/demo |
| STT/voz no browser | Inventário IHM: CLIENT (não é o “FASE 2” de biometria) |
| Face / biometria | Inventário IHM: FASE 2 explícita |

---

## 8. Ordem de PRs (cadeia)

**Caminho padrão:** merge de E → F → G → H (opcional).

```text
main
  └─ feat/etapa-e-console-capabilities     → PR (merge)
       └─ feat/etapa-f-admin-users         → PR (merge)   [ou main em paralelo]
            └─ feat/etapa-g-hardening      → PR (merge)
                 └─ feat/etapa-h-telemetry (opcional)
```

- Preferência: **uma etapa = um PR**.
- F pode nascer de `main` em paralelo; stack só para adiantar.
- **Corte de defesa** (se o prazo apertar): **E + G-01 + G-02**. Fica fora: F, H, G-03/G-04/G-05/G-06 — registrar o corte neste arquivo quando aplicado.

---

## 9. Checklist de retomada (ambiente)

```bash
git checkout main && git pull
git checkout feat/etapa-f-admin-users            # etapa atual; ou criar a partir de main

docker compose up -d                             # Postgres
# se sem Docker: Postgres local documentado em .data / micromamba (não versionar .data/)

export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET="$(openssl rand -base64 48)"
cd apps/backend && ./mvnw spring-boot:run

cd apps/web && npm install && npm run dev        # :5173

# NARA (outro terminal / Desktop/noblenara):
# ros2 launch smartwheelchair worldmuseum.launch.py
# ros2 launch smartwheelchair noblenara.launch.py
# ros2 launch smartwheelchair bridgelaunch.xml   # :9090 + :8080
```

Login demo: `admin` / `change-me`. Câmera: seletor Usuário / Frente (tópico **sem** `%2F` na query).

---

## 10. Manutenção deste documento

Ao **abrir** uma etapa: marcar status `IN PROGRESS` e branch.  
Ao **mergear**: marcar `DONE` + link do PR; atualizar [`proximos-passos.md`](proximos-passos.md) (próxima ação = etapa seguinte).  
Não duplicar listas longas no README raiz — só apontar para cá.
