# ADR-006 — Proposta de mudança de stack: Java + Spring Boot (em vez de Node.js + Express)

**Status:** PROPOSTA  
**Data:** 2026-09-29  
**Decisores (pendente):** Leonardo Vilasboas de Oliveira (discente); Prof. Crescencio Rodrigues Lima Neto (orientador)  
**Relacionada a:** ADR-001 (stack vigente, status `ACEITA` até esta proposta ser decidida), ADR-002 (ROS híbrido)

---

## Contexto

A ADR-001 fixou a stack do backend do TCC como **Node.js + Express + MySQL**, alinhada à súmula / slides / fundamentação.

Em seguida, a arquitetura de produto foi refinada como **backend de governo** (auth, RBAC, robot profiles, parâmetros, auditoria, proxy LLM) com integração **híbrida** ao ROS (ADR-002): o client fala com rosbridge / web_video_server; o backend não precisa mediar cada mensagem de teleop.

Nesse desenho, a afinidade “Node ↔ Roslibjs” perde peso no servidor: o runtime crítico continua no `noblenara` (ROS 2), e a API é majoritariamente REST + MySQL + JWT.

Surge a possibilidade de adotar **Java + Spring Boot** (+ MySQL) por encaixe com Clean Architecture / ports & adapters, maturidade para RBAC/JPA/migrations/testes e familiaridade do discente com esse estilo de backend — **desde que o orientador concorde**, dado o desvio em relação ao texto da súmula.

Esta ADR **não altera** a stack vigente. Enquanto o status for `PROPOSTA`, permanece válida a ADR-001.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — Manter Node.js + Express + MySQL (ADR-001)** | Fidelidade à súmula; ecossistema web ROS familiar no JS (mais no *client*); menos atrito documental | Estrutura Clean/ports exige mais disciplina manual; tipagem/tooling a definir (JS vs TS) |
| **B — Migrar para Java + Spring Boot + MySQL** | Camadas, segurança, JPA, migrations e testes bem suportados; encaixa no control plane descrito; MySQL permanece (sem trocar o BD da súmula) | Exige atualização acadêmica (súmula/slides/ADRs); curva Java se o foco recente foi JS; não “aproxima” nativamente do ROS (e não precisa, no híbrido) |
| C — Spring Boot + PostgreSQL | Ecossistema Spring muito comum com Postgres | Troca o BD declarado na súmula (MySQL) sem necessidade para o caso NARA |

### Compatibilidade com NARA / ROS (importante)

No modelo híbrido (ADR-002):

- `rosbridge` (WebSocket/JSON) e `web_video_server` (HTTP) são **agnósticos** à linguagem do backend
- O estudo de caso `noblenara` / `nara-sim` **não muda**
- O protótipo de UI do TCC (futuro) pode continuar em JS/TS para falar com rosbridge
- **Não** se exige `rcljava` no MVP

Conclusão técnica: **Spring não prejudica** a conversa com o stack robótico já validado; o impacto é de plataforma da API e de alinhamento acadêmico.

---

## Decisão

**Pendente — aguardando alinhamento com o orientador.**

Pergunta a resolver:

1. Pode-se atualizar o pré-projeto/súmula para **Java + Spring Boot + MySQL**?
2. Se sim, a ADR-001 passa a `SUBSTITUÍDA` por esta (após aceite).
3. Se não, esta ADR passa a `REJEITADA` e a ADR-001 permanece a referência.

Critério sugerido de fechamento: anotação de orientação ou e-mail/registro com a decisão do orientador + data.

---

## Consequências (se ACEITA no futuro)

**Positivas**

- Backend de governo com tooling Spring (Security, Data JPA, validation, Actuator, etc.)
- Docs de convenções alinhadas a monólito modular Java
- MySQL mantido → desvio menor que trocar também o banco

**Negativas / trabalho**

- Revisar README, stack declarada, `backend-functional-spec`, conventions e menções a Node/Express
- Atualizar materiais acadêmicos (súmula/slides) para refletir a troca
- Skeleton `apps/backend` nasce em Java, não em Node

**Se REJEITADA**

- Nenhuma alteração de stack; seguir ADR-001
- Opcional: adotar TypeScript no Node para ganhar tipagem sem mudar a narrativa da súmula

---

## Checklist pós-decisão

- [ ] Registrar data e parecer do orientador nesta ADR  
- [ ] Atualizar status → `ACEITA` ou `REJEITADA`  
- [ ] Se aceita: marcar ADR-001 como `SUBSTITUÍDA` (link para ADR-006)  
- [ ] Se aceita: propagar stack nos docs raiz / architecture / conventions  
