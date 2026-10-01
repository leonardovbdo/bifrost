# Próximos passos — retomada do Bifrost

**Atualizado:** 2026-09-30  
**Estado:** Etapa A (auth + session-config + seed NARA) **implementada** na branch `feat/etapa-a-auth-session`. Faltam PROF-03/04 (CRUD/listagem admin) e Etapas B–D.

Use este arquivo para retomar o TCC sem reabrir o histórico de chat.

---

## Onde estamos

| Camada | Situação |
|-------|----------|
| ADRs 001–013 | Aceitas / vigentes (001 histórica Node) |
| Specs + feature packs | Documentados |
| Convenções / ops / diagramas | Prontos |
| Código | Auth JWT+cookies, refresh, `/me`, session-config, seed admin+`nara-sim-alfa`, validação de profile |
| Branch de trabalho | `feat/etapa-a-auth-session` |

---

## Como retomar em 5 minutos

```bash
cd ~/Desktop/TCC
git checkout feat/etapa-a-auth-session
docker compose up -d
export BIFROST_ADMIN_USERNAME=admin BIFROST_ADMIN_PASSWORD=change-me
export BIFROST_JWT_SECRET=change-me-bifrost-dev-secret-at-least-32-chars
cd apps/backend && ./mvnw spring-boot:run
# health: http://localhost:8081/actuator/health
# login:  POST /api/v1/auth/login {"username":"admin","password":"change-me"}
# me:     GET  /api/v1/me/session-config  (cookie)
```

---

## Ordem de implementação (canônica)

### Etapa A — Identidade e sessão

| Item | Status |
|------|--------|
| AUTH-01…09 | **DONE** (IT Testcontainers requer Docker) |
| Seed `nara-sim-alfa` + enums/validação | **DONE** |
| session-config + active-profile | **DONE** |
| PROF-03 CRUD admin / PROF-04 listagem ACL | **TODO** |

### Etapa B — Parameters + auditoria — **TODO**

### Etapa C — LLM + CI backend — **TODO**

### Etapa D — Protótipo UI + demo NARA — **TODO**

---

## Próxima ação imediata

1. Rodar `./mvnw test` com Docker disponível (valida `AuthSessionIntegrationTest`).  
2. Implementar **PROF-03/04** **ou** iniciar **Etapa B** (parameters + audit).

---

## Checklist de higiene

- [ ] `docker compose up -d` e health da API  
- [ ] Tasks `DONE` nos packs  
- [ ] Sem segredos no git  
- [ ] Changelog se for release/tag  

## Contato com o git

Trabalho atual: branch `feat/etapa-a-auth-session` (PR para `main` quando Etapa A estiver aceita).
