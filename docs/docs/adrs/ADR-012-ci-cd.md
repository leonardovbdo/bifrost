# ADR-012 — CI/CD (GitHub Actions)

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  

---

## Contexto

O repositório é docs-first e caminha para `apps/backend` Java. É preciso CI útil agora, sem fingir que já existe Maven, e uma política de contribuição para `main`.

---

## Opções consideradas

| Tema | Opções | Escolha |
|------|--------|---------|
| CI imediato | nada vs lint docs vs só Maven futuro | **Lint de markdown agora** + job Maven **quando** houver wrapper |
| Proteção `main` | livre vs PR recomendado vs block rígido dia 1 | **PR recomendado**; enforcement no GitHub quando fizer sentido |

---

## Decisão

1. Workflow GitHub Actions na pasta `.github/workflows/`:
   - **docs:** lint Markdown em PRs e pushes para `main`
   - **backend:** `./mvnw verify` (Testcontainers; ITs com `disabledWithoutDocker`) — `.github/workflows/backend.yml`
   - **web:** `npm ci` + lint + build do protótipo — `.github/workflows/web.yml`
2. Branch `main`: preferir mudanças via **pull request**; proteção formal (required checks) ativar quando o remote e o fluxo solo/permitirem sem atrito indevido
3. Sem deploy automático no MVP (sem ambiente cloud obrigatório)

---

## Consequências

**Positivas**

- Feedback cedo em docs quebradas
- Caminho claro para CI Java sem bloquear docs-first

**Negativas**

- Lint de MD pode exigir ignores pontuais (ex.: mermaid, acadêmico)
- Proteção rígida demais cedo atrasa commits solo — por isso é recomendação, não bloqueio absoluto no dia 1
