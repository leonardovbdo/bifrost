# Spec — Autenticação e RBAC

## Escopo

- Cadastro inicial via seed (admin local)
- Login username/password
- JWT access token
- RBAC em rotas
- `/me` e `/me/session-config`

## Fora de escopo (MVP)

- OAuth social
- Face login
- Refresh token rotation completa (pode ser fase seguinte)
- MFA

## Regras

1. Senha com bcrypt (custo configurável)
2. JWT com `sub`, `role`, `exp`; opcional `allowed_profile_ids`
3. Role `viewer` não recebe `canTeleop` / `canSendGoal`
4. Falha de login não revela se o usuário existe (mensagem genérica)
5. Usuário `active=false` não autentica

## Contratos

Ver seção Auth / Session-config em [`../../../backend-functional-spec.md`](../../../backend-functional-spec.md).

## Critérios de aceite

- [ ] Login retorna JWT válido
- [ ] Rota admin rejeita `operator`/`viewer`
- [ ] session-config reflete role e profiles permitidos
- [ ] Nenhuma senha em texto claro no banco ou logs
