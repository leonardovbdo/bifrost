# ADR-002 — Integração ROS híbrida

**Status:** ACEITA  
**Data:** 2026-09  
**Decisores:** Leonardo Vilasboas de Oliveira  

---

## Contexto

A súmula prevê integração da API com o ROS via Rosbridge / Roslibjs para leitura de sensores e envio de comandos. Em stacks web robóticas é comum o **browser conectar diretamente** ao `rosbridge_websocket` e ao `web_video_server`, o que já foi validado no estudo de caso NARA.

Há tensão entre:

1. Colocar **todo** o tráfego ROS atrás do backend Spring (controle acadêmico “puro”)
2. Preservar **baixa latência** no teleop e nos streams de vídeo (requisito de segurança/usabilidade)

O TCC precisa de um backend que **governe** o sistema sem se tornar gargalo de cada `geometry_msgs/Twist`.

---

## Opções consideradas

| Opção | Prós | Contras |
|-------|------|---------|
| **A — Híbrida** | Backend governa auth/perfis/ACL/auditoria; client mantém rosbridge para tempo real; alinhada à validação com NARA | Controle ROS não é 100% mediado pelo Spring; exige contrato claro de session-config |
| B — Proxy total no backend | Todo publish/subscribe passa pelo servidor; auditoria fina possível | Maior latência; complexidade (WS duplex, fan-out, backpressure); risco ao teleop |
| C — Só frontend (status quo) | Simples | Não cumpre o objetivo do TCC de backendizar governo e segurança |

---

## Decisão

Adotar **Opção A — integração híbrida**:

1. **Backend (Spring Boot)**  
   - Autenticação/RBAC  
   - Robot Profiles e bindings de tópicos  
   - Parâmetros e limites  
   - Emissão de `session-config`  
   - Auditoria de eventos de alto nível  
   - Proxy LLM  

2. **Client UI (protótipo TCC ou compatível)**  
   - Abre WebSocket no rosbridge autorizado  
   - Publica teleop / goals conforme ACL  
   - Consome câmeras via web_video_server  

3. **Runtime ROS** (`noblenara` / simulação) permanece a fonte dos tópicos.

O backend **não** encaminha cada mensagem de velocidade no MVP.

---

## Consequências

**Positivas**

- Atende RNF de latência de controle
- Permite clientes leves sem reescrever o grafo ROS
- Backend continua central para segurança e multi-robô

**Negativas / mitigações**

- Client malicioso poderia tentar publicar em tópicos não autorizados no rosbridge  
  - Mitigação MVP: rede local confiável + ACL na UI + auditoria  
  - Mitigação futura: autenticação/rosbridge security, VPN, ou bridge filtrante
- Documentação acadêmica deve explicar explicitamente o híbrido (não omitir o hop direto ao rosbridge)
