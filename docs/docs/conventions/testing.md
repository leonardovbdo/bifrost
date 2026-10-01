# Convenções — Testes

**Projeto:** Bifrost  
**Status:** Vigente (MVP backend)

---

## Pirâmide

1. **Unitários** — domínio, merge de parameters, mapeamento de roles  
2. **Integração** — API + PostgreSQL real via **Testcontainers**  
3. **Manuais / demo** — NARA sim + protótipo UI (fora do CI obrigatório no início)

## Integração com Postgres

- MVP: testes de integração usam **Testcontainers** com PostgreSQL (não H2 como fonte da verdade)
- Flyway aplicado no container de teste
- Preferir `@SpringBootTest` + mock de ports externos (LLM) quando necessário

## O que cobrir no MVP (casos críticos)

Sem meta percentual rígida. Obrigatório ter testes (unit ou integração) para:

| Área | Casos |
|------|--------|
| Auth | login ok/fail; viewer sem rota admin; refresh |
| Session-config | limits após merge params; permissions por role |
| Profiles | ACL 403; validação teleop sem cmd_vel; seed slug |
| Parameters | operator não escreve global; user preset reflete limits |
| Audit | evento de login; purge respeita retenção (pode ser unit do serviço) |
| LLM | viewer 403; client mockado retorna reply |

## Nomenclatura

- `*Test` unitário
- `*IT` ou `*IntegrationTest` com Testcontainers

## O que não exigir no MVP

- E2E browser completo no CI
- Carga/performance
- Contrato Pact (opcional depois)
