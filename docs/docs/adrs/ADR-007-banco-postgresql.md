# ADR-007 — Banco de dados PostgreSQL

**Status:** ACEITA  
**Data:** 2026-09-30  
**Decisores:** Leonardo Vilasboas de Oliveira  
**Relacionada a:** ADR-006 (stack Java + Spring Boot); amenda a escolha de SGBD

---

## Contexto

A ADR-006 aceitou **Java + Spring Boot + MySQL**, mantendo MySQL para reduzir o desvio em relação à súmula (que citava MySQL).

Na prática, o time prefere **PostgreSQL**, alinhado ao ecossistema Spring comum e suficiente para o modelo relacional do Bifrost (usuários, RBAC, robot profiles, parâmetros, auditoria).

A troca do SGBD **não** altera a arquitetura híbrida ROS (ADR-002) nem o restante da stack Java/Spring.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| A — Manter MySQL | Mais próximo do texto original da súmula | Menos alinhado à preferência atual do projeto |
| **B — PostgreSQL** | Maduro com Spring/JPA/Flyway; JSONB útil para `topics`/`capabilities`; padrão em muitos backends SI | Desvia do MySQL citado na súmula (atualizar narrativa acadêmica) |

---

## Decisão

Adotar **PostgreSQL** como banco oficial do Bifrost.

- Stack vigente: **Java + Spring Boot + PostgreSQL**
- Migrations: Flyway
- Driver: JDBC PostgreSQL via Spring Data JPA

A ADR-006 permanece válida quanto a **Java + Spring Boot**; apenas o SGBD passa a ser o desta ADR.

---

## Consequências

**Positivas**

- Ferramentas e exemplos Spring costumam assumir Postgres
- JSONB pode simplificar campos flexíveis de Robot Profile

**Negativas**

- Ajustar docs e, quando couber, súmula/slides (MySQL → PostgreSQL)
- Ambiente local/ops passa a subir Postgres em vez de MySQL
