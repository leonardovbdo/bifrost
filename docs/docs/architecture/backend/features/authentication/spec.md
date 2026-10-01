# Spec — Autenticação e RBAC

## Escopo

- Cadastro inicial via seed (admin local por env)
- Login username/password
- JWT access + refresh (cookies httpOnly) — ADR-008
- RBAC em rotas (`ROLE_ADMIN` | `ROLE_OPERATOR` | `ROLE_VIEWER`)
- `/me` e `/me/session-config` — ADR-009
- Troca de profile ativo persistida no backend

## Fora de escopo (MVP)

- OAuth social
- Face login
- MFA

## Regras

1. Senha com bcrypt (custo configurável)
2. Access JWT com `sub`, `username`, `role`, `exp`, `iat`; TTLs parametrizáveis
3. Refresh token no MVP; preferir rotação a cada refresh
4. Tokens canônicos em **cookie httpOnly** (protótipo com `credentials: include`)
5. Role `viewer` → `canTeleop` / `canSendGoal` / `canManageProfiles` = false
6. Falha de login: mensagem genérica (não revelar se o usuário existe)
7. Usuário `active=false` não autentica
8. Admin inicial: username/senha **somente** via env no primeiro boot
9. Profile sem ACL → **403** (ADR-009)

## Contratos

- Detalhe JWT: [`../../../../adrs/ADR-008-jwt-spring-security.md`](../../../../adrs/ADR-008-jwt-spring-security.md)
- Session-config: [`../../../../adrs/ADR-009-session-config.md`](../../../../adrs/ADR-009-session-config.md)
- Visão geral: [`../../../backend-functional-spec.md`](../../../backend-functional-spec.md)

## Critérios de aceite

- [ ] Login seta cookies de access/refresh e devolve metadados do usuário
- [ ] Refresh renova sessão sem novo password
- [ ] Rota admin rejeita `operator`/`viewer`
- [ ] session-config reflete role, ativo salvo, limits e permissions
- [ ] Troca de ativo respeita ACL e gera auditoria `profile_switch`
- [ ] Nenhuma senha em texto claro no banco ou logs
