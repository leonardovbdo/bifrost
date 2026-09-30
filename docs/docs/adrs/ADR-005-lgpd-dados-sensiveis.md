# ADR-005 — LGPD e dados sensíveis

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  

---

## Contexto

A fundamentação teórica e a súmula exigem alinhamento à **LGPD**. Captura automática de webcam / login facial em clients web é um padrão sensível observado em algumas IHMs e não será o caminho do MVP deste TCC (MySQL + auth clássica).

É preciso delimitar quais dados pessoais entram no MVP e como tratar biometria.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — MVP sem biometria; face na fase 2** | Reduz risco LGPD; acelera auth clássica; evita Mongo | Perde feature de face no curto prazo |
| B — Portar face/biometria para MySQL no MVP | Mantém feature vista em algumas IHMs | Dado biométrico sensível; complexidade alta cedo |
| C — Face só no device sem servidor | Privacidade | Não atende login centralizado do backend |

---

## Decisão

Adotar **Opção A** no MVP do TCC:

1. **Dados no MVP:** identificadores de conta, papel, preferências não sensíveis, logs de auditoria operacional, robot profiles.
2. **Biometria / frames de webcam:** não persistidos pelo backend TCC no MVP; feature facial permanece fase 2 com ADR específica de retenção, consentimento e base legal.
3. **LLM:** prompts podem conter contexto operacional; evitar enviar dados pessoais desnecessários; chave no servidor.
4. **Princípios:** minimização, propósito determinado, acesso por RBAC, retenção limitada de logs, segredos fora do git.

Quando face for retomada: avaliar templates/vetores com consentimento explícito, criptografia em repouso e política de exclusão — sem amarrar o TCC a um store NoSQL de IHM externa.

---

## Consequências

**Positivas**

- MVP mais seguro e defendável academicamente
- Foco no core (API + MySQL + governo ROS híbrido)

**Negativas**

- Face login fica fora do MVP (auth por senha/JWT); clients de teste não precisam de webcam
- Precisa comunicar na documentação acadêmica a decisão de faseamento
