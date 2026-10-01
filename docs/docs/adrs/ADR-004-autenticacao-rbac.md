# ADR-004 — Autenticação e RBAC

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  

---

## Contexto

Em muitas IHMs robóticas a autenticação é frágil (credenciais no client ou backend mínimo) e o “papel” pode ser alterado só na UI, o que não é segurança real. Fluxos biométricos (ex.: webcam + store NoSQL) também aparecem e não estão alinhados ao PostgreSQL do TCC.

É necessário um modelo de identidade adequado à súmula (usuários, permissões, LGPD) e a papéis de produto (admin / operator / viewer).

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — JWT + RBAC em PostgreSQL** | Padrão para APIs; stateless no client; papéis claros | Precisa refresh/expiração e seed inicial |
| B — Sessão server-side (cookie) apenas | Simples em same-site | Pior para clientes múltiplos; menos alinhado a API REST pura |
| C — Manter hardcoded | Zero esforço | Inaceitável para o TCC |

Papéis considerados:

| Papel | Escopo |
|-------|--------|
| `admin` | Gestão de usuários/profiles, rosapi, auditoria |
| `operator` | Teleop + goals + monitoramento |
| `viewer` | Somente monitoramento |

---

## Decisão

Adotar **Opção A**:

- Tabela `users` (id, email/username, password_hash, role, active, timestamps)
- Login `POST /auth/login` → access JWT (claims: `sub`, `role`, opcional `allowed_profile_ids`)
- `GET /me` e `GET /me/session-config` para hidratar qualquer client da API
- Middleware Spring Security + checagem de papel em rotas administrativas
- O client **não** pode elevar privilégios localmente; preferências de UI não alteram a role do token

Reconhecimento facial: **fora do MVP** (ver ADR-005).

---

## Consequências

**Positivas**

- Substitui trust-the-UI
- Alinha admin/operator/viewer ao produto
- Base para auditoria por `user_id`

**Negativas**

- Clients precisam implementar o fluxo JWT + session-config (protótipo TCC ou outro)
- Rosbridge em si não valida JWT no MVP (limitação aceita na ADR-002)
