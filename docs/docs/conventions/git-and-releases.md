# Convenções — Git e releases

**Projeto:** Bifrost  
**Status:** Vigente

---

## Commits — Conventional Commits

Formato: `tipo(escopo): descrição`

### Tipos comuns

`feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `style`

### Escopos padrão (MVP)

| Escopo | Uso |
|--------|-----|
| `adr` | Architecture Decision Records |
| `docs` | Documentação geral |
| `auth` | Autenticação / JWT / RBAC |
| `profiles` | Robot profiles |
| `params` | Parameters |
| `audit` | Auditoria / telemetria |
| `llm` | Proxy LLM |
| `ops` | Operação, Docker, runbooks |
| `conv` | Convenções |
| `backend` | Código `apps/backend` (quando existir) |

Exemplos:

```text
docs(adr): accept ADR-011 LLM proxy
feat(auth): add refresh cookie rotation
chore(ops): add postgres compose service
```

## Versionamento SemVer

Enquanto o repositório for **docs-first** (pouco ou nenhum código de produto):

| Mudança | Bump |
|---------|------|
| Nova ADR aceita / feature pack documentado de peso | **MINOR** (`0.X.0`) |
| Correção de doc, typo, clareza | **PATCH** (`0.X.Y`) |
| Quebra deliberada de contrato documentado | **MAJOR** (evitar antes de `1.0.0`) |

Quando `apps/backend` existir: mesma SemVer; `feat` de API → MINOR; breaking de contrato HTTP → MAJOR.

Tags: `vMAJOR.MINOR.PATCH` (ex.: `v0.4.0`).

## Changelog

- Manter [`CHANGELOG.md`](../../../CHANGELOG.md) na raiz do repositório
- Atualizar **a cada tag** de release
- Formato Keep a Changelog (Added / Changed / Fixed)

## Branches (sugestão)

- `main` — estável documentada
- `feat/...`, `docs/...` — trabalho curto → PR quando CI existir
