# Frontend / clientes — Consumption Overview

**Versão:** 0.2  
**Status:** Diretriz para qualquer cliente da API (protótipo TCC ou outro)  
**Nota:** Este TCC **não depende** de adaptar IHMs de terceiros.

---

## 1. Papel do client

O client (protótipo web do TCC ou outra UI) **não** é fonte da verdade de:

- identidade e papel
- catálogo de robôs/tópicos
- limites de teleop e permissões

O client **é** responsável por:

- UI/UX
- WebSocket rosbridge / streams de vídeo (modelo híbrido)
- input (teclado, joystick, etc.)
- aplicar a `session-config` recebida do backend

---

## 2. Bootstrap de sessão (alvo)

1. `POST /api/v1/auth/login` → JWT  
2. `GET /api/v1/me/session-config`  
3. Hidratar estado local com topics, urls, limits, permissions  
4. `connect(session.rosbridgeUrl)`  
5. Montar URLs de vídeo a partir de `videoBaseUrl` + tópicos autorizados  

---

## 3. Contratos que o client deve respeitar

- Não publicar fora dos tópicos/permissions da session-config  
- Ocultar teleop se `canTeleop === false`  
- Não expor inspeção rosapi se `canInspectRosapi === false`  
- Tratar 401/403 com logout / mensagem clara  
- Nunca embutir chaves de LLM ou segredos de infraestrutura  

---

## 4. Protótipo de frontend do TCC

Escopo sugerido (quando houver implementação):

- Login + exibição de role  
- Lista/seleção de robot profiles permitidos  
- Conexão rosbridge + teleop mínimo + uma câmera  
- Envio opcional de eventos de auditoria (ex.: goal)

Não é objetivo reproduzir uma IHM completa de produto de terceiros.

Gap analysis genérico: [`../requirements/inventario-frontend-ihm.md`](../requirements/inventario-frontend-ihm.md).
