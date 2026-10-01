# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-09-30  
**Estado:** Etapa A **completa** na branch `feat/etapa-a-auth-session` (PR #1). Próximo: **Etapa B** (parameters + audit).

Use este arquivo para retomar o TCC sem reabrir o histórico de chat.

---

## Onde estamos

| Camada | Situação |
|-------|----------|
| ADRs / specs / convenções | Prontos |
| Auth + session-config | **DONE** |
| Robot profiles CRUD + ACL list | **DONE** |
| Parameters / audit / LLM | TODO |
| Branch | `feat/etapa-a-auth-session` — https://github.com/leonardovbdo/bifrost/pull/1 |

---

## Como retomar

```bash
git checkout feat/etapa-a-auth-session
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET=change-me-bifrost-dev-secret-at-least-32-chars
# opcional: operator seed
export BIFROST_OPERATOR_SEED_ENABLED=true
export BIFROST_OPERATOR_USERNAME=operator BIFROST_OPERATOR_PASSWORD=change-me
cd apps/backend && ./mvnw spring-boot:run
```

Endpoints-chave: `/api/v1/auth/login`, `/api/v1/me/session-config`, `/api/v1/robot-profiles`.

---

## Próxima ação imediata

**Etapa B** — parameters (global/user + limits na session) e auditoria (90d + purge).

---

## Checklist

- [ ] `./mvnw test` com Docker  
- [ ] Merge PR #1 quando revisado  
- [ ] Sem segredos no git  
