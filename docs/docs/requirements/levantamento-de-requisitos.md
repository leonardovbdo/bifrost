# SRS — Software Requirements Specification

**Projeto:** Backend para Controle e Monitoramento de Sistemas Autônomos (TCC)  
**Estudo de caso:** NARA / noblenara  
**Versão:** 0.1 (rascunho)  
**Data:** 2026-09  
**Status:** Em elaboração  

---

## 1. Introdução

### 1.1 Propósito

Especificar requisitos funcionais e não funcionais do backend que integra clientes de usuário, banco relacional e sistemas robóticos baseados em ROS, alinhado à súmula do TCC. O inventário de responsabilidades típicas de IHM ([`inventario-frontend-ihm.md`](inventario-frontend-ihm.md)) serve como **gap analysis**, não como compromisso de integração com um front externo.

### 1.2 Escopo

**Dentro do escopo (MVP documental + implementação futura):**

- API REST (Java + Spring Boot) para autenticação, autorização, perfis de robô, parâmetros e auditoria
- MySQL para persistência
- Modelo híbrido de integração ROS (governo no backend; tempo real no rosbridge)
- Extensibilidade a múltiplos robôs/tecnologias via *Robot Profiles*
- Proxy de assistente LLM (sem chave no browser)

**Fora do escopo do MVP:**

- Substituir Gazebo / Nav2 / pacotes ROS do `noblenara`
- Proxy WebSocket total de todos os tópicos ROS pelo backend
- Entregar ou manter IHM completa de produto de terceiros
- Reconhecimento facial em produção (fase 2)

### 1.3 Definições e siglas

| Termo | Definição |
|-------|-----------|
| Client UI | Interface web (protótipo do TCC ou outro cliente compatível com a API) |
| Robot Profile | Configuração nomeada de um robô/tecnologia (tópicos, capabilities, ambiente) |
| Session-config | Payload pós-login com ACL, perfil ativo e limites |
| Rosbridge | Ponte WebSocket entre clientes e o grafo ROS |
| RBAC | Controle de acesso baseado em papéis |
| LGPD | Lei Geral de Proteção de Dados Pessoais |

### 1.4 Documentos relacionados

- Súmula e fundamentação: ver índice em `docs/academic/` (PDFs em `~/Desktop/teste da silva/`)
- Inventário do frontend: [`inventario-frontend-ihm.md`](inventario-frontend-ihm.md)
- ADRs 001–005 em `../adrs/`
- Spec funcional: `../architecture/backend-functional-spec.md`

---

## 2. Requisitos funcionais

### RF-01 — Autenticação de usuários

O sistema deve autenticar usuários com credenciais persistidas em MySQL e emitir token JWT.

**Critérios:**

- Credenciais não ficam hardcoded no frontend
- Senhas armazenadas com hash adequado (ex.: bcrypt)
- Logout invalida uso do token no client (e, se adotado, denylist/versão de token)

### RF-02 — Autorização por níveis (RBAC)

O sistema deve distinguir pelo menos os papéis:

| Papel | Intenção |
|-------|----------|
| `admin` | Configuração de perfis, inspeção rosapi, auditoria ampla |
| `operator` | Teleop, navegação (goals), uso operacional |
| `viewer` | Monitoramento (câmeras/status) sem publicar comandos de movimento |

Mapeamento inicial de papéis de produto: administrador/pesquisador → `admin`; operação cotidiana → `operator`; monitoramento → `viewer`.

### RF-03 — Session-config

Após login, o backend deve fornecer configuração de sessão contendo:

- Papel e permissões efetivas
- Robot profile ativo (ou lista permitida)
- Mapa de tópicos autorizados (cmd_vel, câmeras, mapa, battery, …)
- Endpoints/hosts permitidos de rosbridge e web_video_server
- Limites de velocidade / parâmetros de teleop

O frontend **não** deve ser a fonte da verdade desses dados.

### RF-04 — Catálogo de Robot Profiles

O sistema deve permitir cadastrar e consultar perfis de robô, incluindo:

- Identificador (`project`, `prefix`/namespace)
- Ambiente (`sim` \| `physical`)
- Capabilities (`teleop`, `nav2`, `cameras`, `slam`, …)
- Bindings de tópicos e frames
- Metadados da tecnologia (ex.: cadeira NARA, outros robôs futuros)

### RF-05 — Parametrização

O sistema deve persistir parâmetros e preferências:

- Perfis de velocidade (segurança / normal / rápida) e limites por papel
- Preferências por usuário (quando aplicável)
- Destinos frequentes / metadados de navegação (quando houver)

### RF-06 — Integração ROS (híbrida)

- O backend **não** precisa encaminhar cada mensagem `Twist` em tempo real
- O backend **deve** autorizar quais tópicos/ações o client pode usar
- Publicação de teleop/goals e subscribe de sensores/câmeras ocorrem via rosbridge / web_video_server no client

### RF-07 — Monitoramento e telemetria

- MVP: auditoria de eventos de alto nível (login, troca de perfil, goal enviado, falhas de auth)
- Evolução: amostragem/persistência de telemetria selecionada (bateria, odometria resumida)

### RF-08 — Comandos de navegação (governo)

- Operator/admin podem solicitar goals conforme ACL
- Cada goal relevante deve poder ser auditado (quem, quando, perfil, payload resumido)

### RF-09 — Proxy de assistente inteligente

- Consultas LLM passam pelo backend
- Chaves de provedor não são expostas no frontend

### RF-10 — Administração de usuários (admin)

- CRUD básico de usuários e atribuição de papéis (escopo de implementação posterior à auth mínima)

---

## 3. Requisitos não funcionais

### RNF-01 — Latência de controle

O caminho teleop (`cmd_vel`) deve permanecer no rosbridge, evitando hop obrigatório pelo backend Spring.

### RNF-02 — Segurança

- HTTPS/TLS em ambientes não locais (meta)
- JWT com expiração
- Segredos apenas em variáveis de ambiente / secret store
- Princípio do menor privilégio (RBAC)

### RNF-03 — LGPD

- Minimização de dados pessoais
- Bases legais e retenção documentadas (ADR-005)
- Dados biométricos (face) fora do MVP ou com controles explícitos

### RNF-04 — Escalabilidade de domínio

A modelagem de Robot Profile deve permitir novos robôs sem alterar o núcleo de auth.

### RNF-05 — Observabilidade

- Logs estruturados de API
- Healthcheck (`/health`)
- Auditoria consultável por admin

### RNF-06 — Alinhamento acadêmico

Stack e objetivos devem permanecer rastreáveis ao projeto acadêmico (Java/Spring Boot após ADR-006, MySQL, Rosbridge, NARA). A súmula original citava Node/Express; a troca foi aprovada pelo orientador.

---

## 4. Atores

| Ator | Descrição |
|------|-----------|
| Administrador | Configura perfis, usuários, inspeciona sistema |
| Operador | Controla e monitora o robô autorizado |
| Visualizador | Apenas monitora |
| Sistema ROS | Runtime NARA / simulação |
| Client UI | Qualquer front que implemente o contrato da API |

---

## 5. Gap analysis (responsabilidades de client → TCC)

| Área | Padrão problemático em clients | Alvo |
|------|--------------------------------|------|
| Auth | Hardcoded / só no browser | JWT + MySQL + RBAC |
| Papéis | Toggle local de privilégio | Claims no token |
| Robôs/tópicos | Presets no client | Robot Profiles |
| LLM | Key no frontend | Proxy backend |
| Telemetria | Ephemeral | MySQL (eventos → séries) |
| ROS tempo real | Client ↔ rosbridge | Mantido (híbrido) |

Detalhamento: [`inventario-frontend-ihm.md`](inventario-frontend-ihm.md).

---

## 6. Priorização MVP

1. RF-01, RF-02, RF-03  
2. RF-04, RF-05  
3. RF-06 (contrato session-config)  
4. RF-07 (auditoria básica)  
5. RF-09  
6. RF-08 (auditoria de goals)  
7. RF-10  

Facial / biometria: pós-MVP (ADR-005).
