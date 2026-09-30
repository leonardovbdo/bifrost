# Convenções de Documentação

**Projeto:** TCC Backend NARA  

---

## 1. Onde vive cada coisa

| Tipo | Pasta |
|------|-------|
| Decisões | `docs/docs/adrs/` |
| Specs de sistema | `docs/docs/architecture/` |
| Features | `docs/docs/architecture/backend/features/<nome>/` |
| Requisitos | `docs/docs/requirements/` |
| Ops | `docs/docs/operations/` |
| Convenções | `docs/docs/conventions/` |
| Acadêmico | `docs/academic/` |

---

## 2. ADR

Arquivo: `ADR-XXX-titulo-curto.md`

Obrigatório:

- Status (`PROPOSTA` \| `ACEITA` \| `SUBSTITUÍDA` \| `REJEITADA`)
- Data
- Contexto
- Opções consideradas (tabela)
- Decisão
- Consequências

Numeração sequencial. Não reutilizar número.

---

## 3. Feature pack

Cada feature contém:

| Arquivo | Conteúdo |
|---------|----------|
| `README.md` | Visão, objetivos, links |
| `spec.md` | Comportamento e critérios de aceite |
| `adr.md` | Decisões locais ou ponte para ADR global |
| `tasks.md` | Checklist de implementação com status |

Status da feature no README: `PLANEJAMENTO` \| `EM_PROGRESSO` \| `CONCLUIDA`.

---

## 4. Estilo de escrita

- Português (Brasil)
- Frases diretas; evitar marketing
- Preferir tabelas para comparações
- Diagramas Mermaid nos markdowns de arquitetura
- Links relativos entre documentos

---

## 5. Mudança de decisão

Se uma ADR `ACEITA` for revertida:

1. Criar nova ADR explicando a mudança
2. Marcar a antiga como `SUBSTITUÍDA` com link para a nova
3. Atualizar specs/features afetadas
