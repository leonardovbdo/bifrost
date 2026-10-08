# Esteira pós-MVP — Bifrost (após Etapas A–D)

**Atualizado:** 2026-10-08  
**Status:** Documento **normativo** da esteira de trabalho até a defesa / demo NARA completa.  
**Pré-requisito:** `main` com Etapas A–D mergeadas (auth, parameters/audit, LLM/CI, console web + câmera POV).

Este arquivo é a **fonte da verdade** do que falta e da ordem de entrega.  
Retomada rápida do ambiente: [`proximos-passos.md`](proximos-passos.md).

---

## 1. Processo (igual à esteira A→D)

1. Branch a partir de `main` (ou da etapa anterior já mergeada): `feat/etapa-e-…`, `feat/etapa-f-…`, …
2. Implementar **só** o escopo da etapa (sem misturar etapas no mesmo PR).
3. Abrir PR → `main` (ou stack se necessário).
4. Pedir `@cursoragent review`; corrigir achados coerentes; CI verde.
5. Merge; atualizar status neste documento + `CHANGELOG.md` + `proximos-passos.md`.
6. Só então abrir a branch da próxima etapa.

**Definição de pronto (DoD) de cada etapa**

- [ ] Spec/tasks da feature tocada atualizadas (`DONE` / notas)
- [ ] Testes (IT e/ou unit) cobrindo o caminho feliz + 1 negativo relevante
- [ ] Docs ops / README alinhados se o fluxo de demo mudou
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
| E — Capabilities na console | goal_pose, mapa/scan, bateria | **TODO** (próxima) |
| F — Admin + usuários | UI admin + CRUD users | **TODO** |
| G — Hardening | Rate limit, validação, logs | **TODO** |
| H — Telemetria samples | `telemetry_samples` (evolução) | **TODO** / fase 2 |

---

## 3. Etapa E — Capabilities na console (prioridade 1)

**Objetivo:** provar na UI o que o profile NARA já declara (`nav2`, `slam`/`scan`, `battery`), além de teleop/câmera.

| ID | Entrega | Notas |
|----|---------|--------|
| E-01 | UI **goal_pose** (clique no mapa ou input x/y/yaw) | Só se `permissions.canSendGoal`; publish ROS + `POST /audit/events` tipo `goal_pose` |
| E-02 | Painel/overlay **mapa** e/ou **scan** | Subscribe rosbridge aos tópicos da session-config; viewer simples ( OccupancyGrid / LaserScan ) |
| E-03 | Status **bateria** (e opcional odom) | Subscribe `battery` / `odom`; chip na console |
| E-04 | Docs de demo | Atualizar `apps/web/README.md` + `ambiente-local-nara.md` com o fluxo E |

**Fora de E:** CRUD admin, users API, rate limit.

**Branch sugerida:** `feat/etapa-e-console-capabilities`  
**Base:** `main`

---

## 4. Etapa F — Admin na UI + CRUD de usuários

**Objetivo:** governança visível sem depender só de seed/`curl`.

| ID | Entrega | Notas |
|----|---------|--------|
| F-01 | API **CRUD users** (admin) | Alinha functional spec §2 `users`; create/list/patch `active`/`role`; nunca expor hash |
| F-02 | UI admin **profiles** | Listar/criar/editar profile + grant access (consome API já existente) |
| F-03 | UI admin **parameters** | Editar presets globais / ver merge |
| F-04 | UI admin **users** | Consome F-01; viewer/operator sem acesso |
| F-05 | UI **audit** (admin) | Lista `GET /audit/events` com filtros básicos |

**Branch sugerida:** `feat/etapa-f-admin-users`  
**Base:** `main` (após merge de E, se E ainda estiver aberto use stack E→F)

---

## 5. Etapa G — Hardening

**Objetivo:** fechar achados de review / segurança de demo na LAN.

| ID | Entrega | Notas |
|----|---------|--------|
| G-01 | Rate limit `/llm/ask` | Spec LLM recomenda; 429 + audit opcional |
| G-02 | Rate limit / teto em `POST /audit/events` | Evitar flood de `goal_pose` |
| G-03 | `@Valid` + schema URLs em PATCH profiles | `ws`/`wss`, `http`/`https`; 400 em vez de 500 |
| G-04 | `@Size` em `LoginRequest` | Mitigar bcrypt DoS + truncar username no audit |
| G-05 | Log de `Exception` no `GlobalExceptionHandler` | Sem vazar stack no body |
| G-06 | (Opcional) retry câmera após `onError` | Botão “tentar de novo” sem reload |

**Branch sugerida:** `feat/etapa-g-hardening`  
**Base:** `main` após F (ou em paralelo se não houver conflito)

---

## 6. Etapa H — Telemetria persistida (fase 2 / se sobrar tempo)

| ID | Entrega | Notas |
|----|---------|--------|
| H-01 | Draft migration `telemetry_samples` | Já em `features/telemetria/tasks.md` (`TEL-01`) |
| H-02 | Ingestão amostrada (não cmd_vel por tick) | Spec telemetria |
| H-03 | Query admin básica | Fora do caminho crítico da defesa |

Não bloqueia demo NARA se E–G estiverem fechadas.

---

## 7. Explicitamente fora da esteira (não abrir PR “só por isso”)

- Autenticação do rosbridge / enforcement de `cmd_vel` no backend  
- IHM completa tipo `noblenara-ihm`  
- Histórico de chat LLM no Postgres  
- Deploy produção / multi-robô físico  
- STT/voz no browser  

Esses itens permanecem como **FASE 2** no inventário de IHM e na functional spec §8.

---

## 8. Ordem de PRs (cadeia)

```text
main
  └─ feat/etapa-e-console-capabilities     → PR (merge)
       └─ feat/etapa-f-admin-users         → PR (merge)   [ou a partir de main pós-E]
            └─ feat/etapa-g-hardening      → PR (merge)
                 └─ feat/etapa-h-telemetry (opcional)
```

Preferência: **uma etapa = um PR**. Se a defesa estiver perto, cortar em E + G-01/G-05 e deixar F/H para depois — registrar o corte neste arquivo.

---

## 9. Checklist de retomada (ambiente)

```bash
git checkout main && git pull
git checkout feat/etapa-e-console-capabilities   # ou criar a partir de main

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
