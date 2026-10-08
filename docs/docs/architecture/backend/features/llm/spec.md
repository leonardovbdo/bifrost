# Spec — LLM Proxy

## Escopo

- `POST /api/v1/llm/ask`
- Integração Gemini via port `LlmClient`
- Audit `llm_ask`
- Config: API key, modelo, timeout, max prompt length
- Stub local quando `BIFROST_LLM_API_KEY` estiver vazia e `stub-when-missing-key=true`

## Fora de escopo (MVP)

- Histórico de conversas no Postgres
- Streaming SSE/WebSocket da resposta
- Viewer com acesso ao LLM
- Fine-tuning / embeddings store

## Regras

1. Só `admin` e `operator`
2. Sem API key no response ou logs
3. Prompt/resposta não persistidos como histórico; audit só metadados
4. `context` opcional é string/objeto enviado ao provedor na mesma request (efêmero)
5. Rate limit simples recomendado (config) para abuso em demos

## Contrato

```json
// request
{ "prompt": "…", "context": { "profileSlug": "nara-sim-alfa" } }

// response
{ "reply": "…", "model": "…" }
```

## Critérios de aceite

- [x] Operator recebe reply com key só no server
- [x] Viewer recebe 403
- [x] Falha Gemini não vaza key
- [x] Evento `llm_ask` aparece na auditoria admin
- [x] Sem key: stub responde (dev/CI) ou 503 se stub desligado
