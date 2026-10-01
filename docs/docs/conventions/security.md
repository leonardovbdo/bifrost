# Convenções — Segurança

**Projeto:** Bifrost  
**Status:** Vigente (MVP)

**Relacionadas:** ADR-005 (LGPD), ADR-008 (JWT/cookies), ADR-011 (LLM keys)

---

## Segredos

- MVP: **somente variáveis de ambiente** (e `.env` local **fora do git**)
- Fornecer `.env.example` só com chaves vazias/placeholders
- Docker secrets / vault: **fase seguinte** (ops)
- Nunca commitar: JWT secret, senha admin seed, API key Gemini, senhas DB

## Logging

**Nunca logar:**

- password / password_hash em claro
- JWT ou refresh token completos
- API keys / Authorization headers completos
- Prompt LLM completo se houver risco de PII (preferir tamanho/hash)

Erros de provedor externo: mensagem genérica ao client; detalhe interno só em log sanitizado.

## Transporte

- **localhost:** HTTP aceitável no desenvolvimento
- **Fora de localhost** (LAN/demo/produção): **HTTPS obrigatório**
- Cookies: `HttpOnly`; `Secure` quando HTTPS; `SameSite` compatível com o protótipo

## AuthZ

- RBAC no servidor (`ROLE_*`); UI não é fonte de verdade
- Profile sem ACL → **403** (ADR-009)
- Inputs validados (Bean Validation)

## Dependências e headers

- Atualizar dependências com CVE conhecidos antes de demos públicas
- Não expor Actuator sensível sem auth quando for ligado
- CORS explícito para origem do protótipo (sem `*` com credentials)

## Checklist rápido de PR

- [ ] Sem segredo novo no git
- [ ] Erros sem stack ao client
- [ ] Rotas admin protegidas
- [ ] Audit sem PII desnecessária
