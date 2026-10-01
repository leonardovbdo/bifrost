# ADR-008 — JWT e Spring Security (detalhe)

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  
**Relacionadas:** ADR-004 (conceito RBAC), ADR-006 (Spring)

---

## Contexto

A ADR-004 definiu JWT + RBAC em PostgreSQL, mas deixou em aberto TTL, refresh, claims, armazenamento no client e bootstrap do admin. Sem isso o protótipo e a feature `authentication` inventam detalhes na implementação.

---

## Opções consideradas

| Tema | Opções | Escolha |
|------|--------|---------|
| TTL access | fixo vs parametrizável | Parametrizável (env/config) |
| Refresh | só access vs access + refresh | Access + refresh no MVP |
| Claims | só `sub`+role vs extras | Extras (`username`, roles de admin) |
| Client storage | memory / web storage / cookie | Cookie httpOnly |
| Role Spring | claim solta vs `ROLE_*` | `ROLE_*` |
| Admin seed | SQL manual vs env no primeiro boot | Env no primeiro boot |

---

## Decisão

### Tokens

- **Access token (JWT)** e **refresh token** no MVP.
- TTLs **parametrizáveis** via configuração (ex.: `BIFROST_JWT_ACCESS_TTL`, `BIFROST_JWT_REFRESH_TTL`).
- **Defaults sugeridos** (overrideável): access `15m`, refresh `7d`.
- Refresh: endpoint dedicado (ex.: `POST /api/v1/auth/refresh`); rotação recomendada na implementação (novo refresh a cada uso).

### Entrega ao client

- Tokens em **cookies httpOnly** (+ `Secure` fora de localhost; `SameSite` adequado ao protótipo).
- O body do login pode devolver metadados do usuário (`id`, `username`, `role`), **sem** expor o JWT em JSON/localStorage.
- CORS + credenciais (`credentials: include`) no protótipo front.

### Claims / autoridades

- Claims mínimas do access JWT: `sub` (user id), `username`, `role` (ou `roles`), `exp`, `iat`.
- No Spring Security, mapear para autoridades `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_VIEWER`.
- Papéis de produto continuam `admin` | `operator` | `viewer` no banco; o prefixo `ROLE_` é só o contrato Spring.

### Seed do admin

- No **primeiro boot** (ou migration/seed condicional): criar admin se não existir, com username/senha **somente** via variáveis de ambiente (nunca commitadas).
- Sem seed de admin com senha default no repositório.

### Alinhamento com ADR-004

- Substitui a leitura “Bearer só no JSON” como caminho preferencial do MVP do protótipo; APIs de teste podem ainda aceitar `Authorization: Bearer` se documentado, mas o caminho canônico do TCC é cookie.

---

## Consequências

**Positivas**

- TTL ajustável sem redeploy de lógica
- Refresh evita relogin constante em demos
- Cookie httpOnly reduz XSS roubando token em JS
- `ROLE_*` encaixa em anotações/`hasRole` do Spring

**Negativas**

- Cookies exigem cuidado com CSRF/SameSite/CORS
- Refresh implica persistir ou versionar tokens de refresh (tabela ou store)
- Clients não-browser precisam de fluxo explícito (Bearer opcional)
