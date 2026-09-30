# Feature: Robot Profiles

**Status:** `PLANEJAMENTO`  
**Owner:** Backend TCC  
**Criado em:** 2026-09-29  

---

## Visão Geral

Backendiza a seleção de robô/tecnologia e o mapa de tópicos que costumam ficar hardcoded no client, permitindo NARA e outros sistemas autônomos.

## Objetivos

1. CRUD de profiles (admin)
2. Bindings de tópicos/capabilities/ambiente
3. Controle de quais users acessam quais profiles
4. Seed `nara-sim-alfa`

## Documentos

| Documento | Propósito |
|-----------|-----------|
| [`spec.md`](spec.md) | Especificação |
| [`tasks.md`](tasks.md) | Tasks |
| [`adr.md`](adr.md) | Ponte ADR-003 |

## Dependências

- ADR-003
- Feature authentication (session-config consome profiles)
