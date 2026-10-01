# ADR-011 — Proxy LLM no backend

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  
**Relacionadas:** ADR-005 (LGPD / chave no servidor), ADR-006 (Spring)

---

## Contexto

O produto prevê assistência via LLM sem expor API keys no browser. É preciso decidir se entra no MVP, qual provedor, quem pode chamar e se há histórico persistido.

---

## Opções consideradas

| Tema | Opções | Escolha |
|------|--------|---------|
| Escopo MVP | fora vs proxy magro | **Proxy magro no MVP** |
| Provedor | só Gemini vs pluggable | **Gemini default + interface pluggable** |
| ACL | admin / operator+ / todos auth | **admin + operator** |
| Histórico | Postgres vs efêmero + audit | **Efêmero + audit `llm_ask`** |

---

## Decisão

1. **MVP:** `POST /api/v1/llm/ask` com body `{ prompt, context? }` → `{ reply }`.
2. Chave de API **somente** no servidor (env); nunca no frontend.
3. Abstração `LlmClient` (port); implementação inicial **Gemini**; outros provedores sem mudar o contrato HTTP.
4. Autorização: `ROLE_ADMIN` e `ROLE_OPERATOR`. `ROLE_VIEWER` → 403.
5. **Não** persistir conversas/prompts completos no Postgres no MVP.
6. Registrar audit `llm_ask` com metadados (user id, modelo, tamanhos) — **evitar** gravar prompt/resposta completos se houver risco de PII; preferir hashes/tamanhos.
7. Timeouts e limite de tamanho de prompt configuráveis; falhas do provedor → erro de API padronizado sem vazar detalhes internos da key.

---

## Consequências

**Positivas**

- Demo de assistente alinhada à súmula
- LGPD mais simples sem histórico de chat
- Troca de vendor possível via nova impl do port

**Negativas**

- Dependência de rede/provedor externo nas demos
- Sem “continuidade de conversa” no MVP (cada ask é independente, salvo o client reenviar contexto)
