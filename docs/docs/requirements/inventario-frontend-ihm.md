# Inventário — Responsabilidades típicas de uma IHM robótica (gap analysis)

**Versão:** 0.2  
**Data:** 2026-09-29  
**Status:** Referência de gap (não é roadmap de integração com IHM de terceiros)

Este documento lista responsabilidades que **costumam ficar no frontend** em interfaces de teleoperação/monitoramento ROS (observadas em IHMs existentes do ecossistema NARA e no uso real da simulação). Serve para delimitar o que o **backend do TCC** deve governar.

**Importante:** o TCC **não assume** que uma IHM externa será adaptada para consumir esta API. A validação poderá usar um **protótipo de frontend próprio**.

## Legenda de destino

| Destino | Significado |
|---------|-------------|
| **BACKEND** | Deve ser fonte da verdade na API TCC |
| **CLIENT** | Permanece no browser (UI / I/O de baixa latência) |
| **HÍBRIDO** | Client executa; backend autoriza, configura ou audita |
| **FASE 2** | Fora do MVP crítico |

---

## 1. Serviços / lógica de cliente

| Responsabilidade | Destino | Notas |
|------------------|---------|-------|
| WebSocket rosbridge, subscribe/publish | **HÍBRIDO** | Conexão no client; URL e tópicos permitidos via session-config |
| Teleop teclado/joystick → `cmd_vel` | **HÍBRIDO** | Publish no client; limites no backend |
| Captura de voz (STT) no browser | **CLIENT** (+ **HÍBRIDO** se intents forem validados) | |
| Chamadas a LLM com API key no browser | **BACKEND** | Proxy; segredo no servidor |
| Montagem de prompt com estado local | **HÍBRIDO** | |

---

## 2. Estado de produto no client (anti-padrão a evitar)

| Item | Problema se ficar só no client | Destino |
|------|--------------------------------|---------|
| Login / papel (admin vs usuário) | Trust-the-UI; sem auditoria real | **BACKEND** — JWT + RBAC |
| Presets de robô / prefix / tópicos | Acoplamento; sem catálogo central | **BACKEND** — Robot Profiles |
| Limites de velocidade / preferências | Sem consistência multi-cliente | **BACKEND** — Parameters |
| URLs de bridge/vídeo “chumbadas” | Quebra entre sim/físico | **HÍBRIDO** — session-config |

---

## 3. UI

| Responsabilidade | Destino |
|------------------|---------|
| Layout, câmeras, mapa, logs | **CLIENT** |
| Escolha de robô entre profiles permitidos | **HÍBRIDO** |
| Controles admin (CRUD profiles/users) | **CLIENT** + API **BACKEND** |
| Inspeção rosapi | **HÍBRIDO** — só `admin` |

---

## 4. Backends “mínimos” acoplados a IHM

Padrões observados em stacks legadas (ex.: login hardcoded, face + NoSQL):

| Padrão | Destino no TCC |
|--------|----------------|
| Users em código | Substituído por Auth JWT + MySQL |
| Face/biometria | **FASE 2** (ADR-005) |
| Mongo como store principal | **Não** — MySQL (ADR-006) |

---

## 5. O que um client NÃO deve ser

- Fonte da verdade de papéis e ACL  
- Catálogo definitivo de robôs/tópicos  
- Dono de segredos (LLM, DB)  
- Único lugar onde “existe” preferência/telemetria histórica  

---

## 6. Matriz resumida

```text
GOVERNO / DADOS          → Backend Bifrost (Spring Boot + MySQL)
POLÍTICA DE SESSÃO       → Backend emite; Client obedece
TEMPO REAL ROS           → Client ↔ rosbridge / web_video_server
UI / UX                  → Protótipo TCC ou outro client compatível
```

## 7. Ordem sugerida de backendização

1. Auth + RBAC + `/me/session-config`
2. Robot Profiles + topic bindings
3. Parameters / speed profiles
4. Audit de eventos
5. LLM proxy
6. Telemetria amostrada
7. Facial / biometria (fase 2)
