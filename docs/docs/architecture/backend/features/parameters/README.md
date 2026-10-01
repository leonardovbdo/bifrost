# Feature: Parameters

**Status:** `PLANEJAMENTO`  
**Owner:** Backend TCC  
**Criado em:** 2026-09-30  

---

## Visão Geral

Parâmetros de produto e preferências no PostgreSQL, com escopos `global` e `user` no MVP. Alimentam `limits.teleop` na `session-config` (ADR-009). Enforçamento server-side de Twist fica fora do MVP (modelo híbrido ADR-002).

## Objetivos

1. Defaults globais de velocidade (safety / normal / fast)
2. Preferência por usuário (qual preset ativo)
3. Exposição consistente via session-config
4. Admin gerencia `global`; usuário autenticado gerencia próprio escopo `user`

## Documentos

| Documento | Propósito |
|-----------|-----------|
| [`spec.md`](spec.md) | Especificação |
| [`tasks.md`](tasks.md) | Tasks de implementação |
| [`adr.md`](adr.md) | Decisões desta feature |

## Dependências

- ADR-007 (PostgreSQL)
- ADR-009 (session-config / limits)
- ADR-008 (auth / roles)
