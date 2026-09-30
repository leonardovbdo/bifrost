# Bifrost (TCC) — Backend para Controle e Monitoramento de Sistemas Autônomos

Repositório do Trabalho de Conclusão de Curso (Bacharelado em Sistemas de Informação — IFBA Campus Vitória da Conquista).

**Produto (nome de trabalho):** **Bifrost** — *a ponte governada entre pessoas e sistemas autônomos.*  
**Discente:** Leonardo Vilasboas de Oliveira  
**Orientador:** Prof. Crescencio Rodrigues Lima Neto  
**Estudo de caso (runtime):** projeto NARA / repositório `noblenara` (cadeira de rodas autônoma em ROS)

Metáfora e posicionamento do nome: [`docs/docs/architecture/bifrost-nome-e-metafora.md`](docs/docs/architecture/bifrost-nome-e-metafora.md).

## Propósito

Desenvolver e documentar **Bifrost**, uma arquitetura de **backend** (Node.js + Express + MySQL) que **governa** identidade, perfis de robô, parametrização, níveis de acesso e telemetria para sistemas autônomos — com o NARA como caso de uso, sem se limitar a uma única tecnologia ou a um frontend de terceiros.

A comunicação de **baixa latência** com o ROS (teleop, sensores, câmeras) permanece no modelo **híbrido**: o cliente (protótipo de UI do TCC ou outro front) fala com rosbridge / web_video_server; o backend autoriza, configura e audita.

Este repositório começa **docs-first**. A implementação em `apps/backend` (e, se necessário, um **protótipo leve de frontend** só para testes da API) virá em etapas posteriores.

## Stack declarada

| Camada | Tecnologia |
|--------|------------|
| Backend | Node.js + Express |
| Banco | MySQL |
| Integração ROS | Rosbridge (cliente no browser; governo no backend) |
| Cliente de testes | Protótipo de UI do próprio TCC (futuro; opcional) |
| Runtime robótico | ROS 2 Jazzy (`noblenara` / `nara-sim`) |

## Documentação

Toda a documentação arquitetural vive em [`docs/`](docs/README.md), organizada em `adrs/`, `architecture/`, `conventions/`, `requirements/` e `operations/`.

Documentos acadêmicos de origem (súmula, slides, fundamentação): índice em [`docs/academic/`](docs/academic/README.md) — PDFs ficam fora do git (`~/Desktop/teste da silva/`).

## Repositórios relacionados

| Repositório | Papel |
|-------------|--------|
| `noblenara` | Runtime ROS / simulação — estudo de caso deste TCC |
| *(opcional)* outros fronts | Qualquer cliente HTTP/WS que respeite o contrato da API; **não** há dependência de um IHM externo específico |

## Status

Fase atual: **planejamento e especificação** (sem código de produção do backend ainda).
