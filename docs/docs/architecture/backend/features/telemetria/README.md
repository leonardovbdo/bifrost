# Feature: Telemetria e Auditoria

**Status:** `PLANEJAMENTO`  
**Owner:** Backend TCC  
**Criado em:** 2026-09-29  

---

## Visão Geral

Persistir eventos de alto nível (auditoria) e preparar evolução para telemetria amostrada. Não registrar cada mensagem de teleop no MVP.

## Objetivos

1. API para registrar eventos autenticados
2. Consulta admin com filtros
3. Tipos MVP: login_success/failure, logout, profile_switch, goal_pose, parameter_change, llm_ask
4. Retenção 90 dias + purge automático
5. Desenhar tabela futura de samples de telemetria (sem ingestão contínua no MVP)

## Documentos

| Documento | Propósito |
|-----------|-----------|
| [`spec.md`](spec.md) | Especificação |
| [`tasks.md`](tasks.md) | Tasks |
| [`adr.md`](adr.md) | Decisões locais + ponte híbrida |

## Dependências

- Authentication
- Robot Profiles (opcional no evento)
- ADR-002 (não auditar cada Twist)
