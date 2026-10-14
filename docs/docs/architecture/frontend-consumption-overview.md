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
4. **UI de governança (Session Hub)** — mostrar papel/permissões; **ainda sem** rosbridge  
5. Sob gesto explícito do operador: `connect(session.rosbridgeUrl)`  
6. Montar URLs de vídeo a partir de `videoBaseUrl` + tópicos autorizados  
7. Desconectar / sair do console → fechar ROS e voltar ao hub  

---

## 3. Contratos que o client deve respeitar

- Não publicar fora dos tópicos/permissions da session-config  
- Ocultar teleop (e demais ações) se a permission/role não permitir — não só desabilitar com texto  
- Não expor inspeção rosapi se `canInspectRosapi === false`  
- Tratar 401/403 com logout / mensagem clara  
- Nunca embutir chaves de LLM ou segredos de infraestrutura  
- Não conectar rosbridge só porque o login autenticou  

---

## 4. Protótipo de frontend do TCC

Implementado em [`apps/web`](../../../apps/web/README.md) (Vite + React):

- Login → **Session Hub** (role, profile, capabilities ADR-010, matriz de permissões)  
- Conectar sob demanda ao console (`#/console`) + teleop WASD + câmera  
- Assistente via `/llm/ask` (admin/operator; oculto para viewer)  
- Preset de teleop (`safety`/`normal`/`fast`) via parameters  
- Admin (`#/admin/…`)  

Não é objetivo reproduzir uma IHM completa de produto de terceiros.

Esteira / status: [`../operations/esteira-pos-mvp.md`](../operations/esteira-pos-mvp.md).

Gap analysis genérico: [`../requirements/inventario-frontend-ihm.md`](../requirements/inventario-frontend-ihm.md).
