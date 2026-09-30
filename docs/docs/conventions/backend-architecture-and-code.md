# Convenções de Arquitetura e Código do Backend

**Projeto:** TCC Backend NARA  
**Status:** Rascunho inicial (docs-first; código ainda não existe)  

---

## 1. Objetivo

Padronizar a futura implementação em `apps/backend` como monólito modular Express com separação de domínio, aplicação e infraestrutura.

---

## 2. Estrutura sugerida

```text
apps/backend/
  src/
    domain/           # entidades, VOs, erros de domínio, ports
    application/      # use cases, DTOs de aplicação
    infrastructure/   # Express controllers, MySQL, JWT, LLM client
    main.ts           # bootstrap
  migrations/
  tests/
  package.json
  .env.example
```

### 2.1 `domain`

- Sem dependência de Express/MySQL drivers
- Entidades: User, RobotProfile, AuditEvent, Parameter
- Ports: `UserRepository`, `RobotProfileRepository`, `AuditPublisher`, etc.

### 2.2 `application`

- Casos de uso: `LoginUser`, `GetSessionConfig`, `UpsertRobotProfile`, `RecordAuditEvent`, `AskLlm`
- Orquestra ports; define transações

### 2.3 `infrastructure`

- Controllers finos (HTTP ↔ use case)
- Repositórios MySQL
- Middleware auth
- Config via env

---

## 3. Regras práticas

1. Controllers não acessam SQL diretamente
2. Use cases não conhecem `req`/`res`
3. Validação de entrada na borda (HTTP) + invariantes no domínio
4. Nomes de arquivos/casos de uso explícitos
5. Sem segredos commitados (`.env` no `.gitignore`)
6. Logs estruturados JSON quando possível
7. Testes de integração para auth e session-config primeiro

---

## 4. HTTP

- Prefixo `/api/v1`
- Bearer JWT
- Erros: `{ "error": { "code": "AUTH_INVALID", "message": "…" } }`
- Códigos HTTP semânticos (401/403/404/409/422/500)

---

## 5. Banco

- Migrations obrigatórias para qualquer mudança de schema
- Seeds só para desenvolvimento / bootstrap documentado
- Evitar lógica de negócio em stored procedures no MVP

---

## 6. Relação com ROS

Conforme ADR-002: código Node **não** precisa publicar Twist no caminho crítico. Integrações ROS server-side, se existirem no futuro, ficam isoladas em adapters — não no domínio puro.
