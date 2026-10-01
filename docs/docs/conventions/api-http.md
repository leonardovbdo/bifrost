# Convenções — API HTTP

**Projeto:** Bifrost  
**Status:** Vigente (MVP)

---

## Prefixo

- API versionada: `/api/v1/...`
- Health fora do prefixo de negócio se desejado: `/health` (ou `/actuator/health` quando Actuator entrar)

## Autenticação

- Caminho canônico do protótipo: cookies httpOnly (ADR-008) com `credentials: include`
- Rotas públicas mínimas: login, refresh (conforme desenho), health

## Paginação

- Query: `page` (0-based), `size`
- Default: `page=0`, `size=20`
- Máximo: `size=100` (acima → 400)
- Resposta de lista sugerida:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

## Erros

Formato canônico:

```json
{
  "error": {
    "code": "PROFILE_FORBIDDEN",
    "message": "Human-readable message",
    "details": [
      { "field": "slug", "message": "must not be blank" }
    ]
  }
}
```

- `code` + `message` **obrigatórios**
- `details` **opcional** (validação Bean Validation, conflitos de campo)
- Não vazar stack traces nem SQL ao client

## Datas e tempo

- Sempre **ISO-8601 em UTC** (ex.: `2026-09-30T23:00:00Z`)
- Timestamps no JSON como string instant; evitar epoch sem documentação explícita

## Métodos e status (guia rápido)

| Situação | Status |
|----------|--------|
| Criado | 201 |
| OK leitura/update | 200 |
| Sem body (logout ok) | 204 quando fizer sentido |
| Validação | 400 |
| Não autenticado | 401 |
| Sem permissão / ACL profile | 403 (ADR-009) |
| Não encontrado | 404 |
| Conflito (slug único) | 409 |

## Content-Type

- Request/response JSON: `application/json`
- Charset UTF-8
