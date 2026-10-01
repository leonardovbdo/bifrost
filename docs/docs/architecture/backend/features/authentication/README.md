# Feature: Autenticação e RBAC

**Status:** `PLANEJAMENTO`  
**Owner:** Backend TCC  
**Criado em:** 2026-09-29  

---

## Visão Geral

Substitui autenticação frágil (hardcoded / só no client) por JWT com papéis em PostgreSQL (`admin`, `operator`, `viewer`) e emissão de session-config.

## Objetivos

1. Login seguro com senha hasheada
2. Claims de papel no JWT
3. Endpoint `/me/session-config` para hidratar o client
4. Impedir elevação de privilégio apenas no browser

## Documentos

| Documento | Propósito |
|-----------|-----------|
| [`spec.md`](spec.md) | Especificação |
| [`tasks.md`](tasks.md) | Tasks de implementação |
| [`adr.md`](adr.md) | Ponte para ADR-004 |

## Dependências

- ADR-006 (stack Java/Spring)
- ADR-004 (auth/RBAC)
- ADR-005 (sem biometria no MVP)
