# ADR-001 — Stack Node.js + Express + MySQL

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira (discente), alinhamento com súmula TCC I  

> **Nota:** existe proposta de revisão desta decisão em [`ADR-006-proposta-stack-java-spring.md`](ADR-006-proposta-stack-java-spring.md) (`PROPOSTA`, aguardando orientador). Enquanto a ADR-006 não for aceita, **esta ADR-001 permanece vigente**.

---

## Contexto

A súmula, os slides e a fundamentação teórica do TCC definem explicitamente:

- API REST com **Node.js** e **Express**
- Persistência em **MySQL**
- Integração com ROS via **Rosbridge**
- Estudo de caso **NARA**

Backends mínimos acoplados a IHMs (ex.: FastAPI + Mongo só para login/face) divergem do compromisso acadêmico (Node/Express/MySQL) e não cobrem governo de perfis/RBAC/telemetria.

É necessário registrar formalmente a stack do repositório TCC para evitar deriva tecnológica.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — Node.js + Express + MySQL** | Alinhada à súmula; ecossistema maduro para API REST; MySQL relacional adequado a usuários/papéis/auditoria | Não reaproveita stacks Python/NoSQL de IHMs externas |
| B — Expandir stack Python + Mongo de uma IHM legada | Reaproveita esboços existentes | Diverge da súmula; Mongo não é o BD prometido academicamente |
| C — Spring Boot + PostgreSQL | Boas convenções de Clean Architecture | Fora do escopo declarado do TCC; curva e stack diferentes |

---

## Decisão

Adotar **Opção A**: Node.js + Express + MySQL como stack oficial do backend do TCC.

- Stacks FastAPI/Mongo de IHMs externas **não** são o alvo deste repositório; servem no máximo como contraste de gap.
- O novo código viverá neste repositório (`apps/backend` em fase futura).
- Convenções de camadas seguirão Clean Architecture / ports & adapters adaptadas ao ecossistema Node — ver `conventions/backend-architecture-and-code.md`.

---

## Consequências

**Positivas**

- Rastreabilidade direta com documentos acadêmicos
- Modelo relacional natural para RBAC, profiles e auditoria
- Separação clara entre runtime `noblenara` (estudo de caso) e o backend de produto deste TCC

**Negativas / trade-offs**

- Retrabalho em relação ao backend Python atual
- Equipe precisa implementar auth/migrations do zero neste repo
- Integração Node ↔ ROS será via contratos HTTP + client rosbridge no front (ADR-002), não via rclnodejs obrigatório no MVP
