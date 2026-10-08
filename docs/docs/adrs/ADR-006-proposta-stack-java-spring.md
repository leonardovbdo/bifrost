# ADR-006 — Stack Java + Spring Boot (+ SGBD inicial MySQL)

**Status:** ACEITA  
**Data de proposta:** 2026-09-29  
**Data de aceite:** 2026-09-30  
**Decisores:** Leonardo Vilasboas de Oliveira (discente); Prof. Crescencio Rodrigues Lima Neto (orientador)  
**Substitui:** [`ADR-001-stack-node-express-mysql.md`](ADR-001-stack-node-express-mysql.md)  
**Relacionada a:** ADR-002 (ROS híbrido); **SGBD emendado por** [`ADR-007-banco-postgresql.md`](ADR-007-banco-postgresql.md)

> Stack de plataforma vigente: **Java + Spring Boot**. Banco vigente: **PostgreSQL** (ADR-007).

---

## Contexto

A ADR-001 havia fixado **Node.js + Express + MySQL** alinhada à súmula inicial. Com a arquitetura de **backend de governo** e integração **híbrida** ao ROS (ADR-002), a API é majoritariamente REST + MySQL + JWT; o client fala com rosbridge. Nesse cenário, **Java + Spring Boot** encaixa melhor nas convenções de Clean Architecture / ports & adapters desejadas.

Em 2026-09-30 o orientador **aprovou** a mudança de stack.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| A — Manter Node.js + Express + MySQL | Fidelidade literal à súmula original | Menos natural para o monólito modular desejado |
| **B — Java + Spring Boot + MySQL** | Security, JPA, Flyway, testes, camadas; MySQL permanece | Atualizar docs/materiais acadêmicos |
| C — Spring Boot + PostgreSQL | Comum no ecossistema Spring | Trocaria o BD da súmula sem necessidade |

---

## Decisão

Adotar **Opção B: Java + Spring Boot** como plataforma do backend **Bifrost** (SGBD inicial MySQL; depois emendado para PostgreSQL na ADR-007).

- Pacote base sugerido: `com.bifrost.backend`
- Persistência: Spring Data JPA + Flyway (**PostgreSQL** vigente — ADR-007)
- Segurança: Spring Security + JWT + RBAC
- Integração ROS: permanece híbrida (ADR-002); sem `rcljava` no MVP
- Convenções de código: [`../conventions/backend-architecture-and-code.md`](../conventions/backend-architecture-and-code.md)

A súmula/slides devem ser alinhados a esta decisão quando houver revisão acadêmica formal.

---

## Consequências

**Positivas**

- Tooling maduro para auth, persistência, migrations e testes
- Docs de arquitetura alinhados a monólito modular Spring

**Negativas / trabalho**

- Propagar a stack em README, specs e conventions
- Skeleton `apps/backend` nasce em Java
- Atualizar narrativa da súmula junto ao orientador quando couber
- SGBD: ver ADR-007 (PostgreSQL)

---

## Checklist pós-decisão

- [x] Registrar data e parecer do orientador nesta ADR  
- [x] Atualizar status → `ACEITA`  
- [x] Marcar ADR-001 como `SUBSTITUÍDA`  
- [x] Propagar stack nos docs raiz / architecture / conventions  
