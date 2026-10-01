# Feature: LLM Proxy

**Status:** `PLANEJAMENTO`  
**Owner:** Backend TCC  
**Criado em:** 2026-09-30  

---

## Visão Geral

Proxy HTTP para assistente (Gemini no MVP), chave no servidor, sem histórico no Postgres. Ver ADR-011.

## Objetivos

1. Endpoint `/llm/ask` autenticado
2. ACL admin + operator
3. Client pluggable (port + Gemini)
4. Audit `llm_ask` com metadados mínimos

## Documentos

| Documento | Propósito |
|-----------|-----------|
| [`spec.md`](spec.md) | Especificação |
| [`tasks.md`](tasks.md) | Tasks |
| [`adr.md`](adr.md) | Ponte ADR-011 |

## Dependências

- ADR-011, ADR-008 (auth), feature telemetria (audit)
