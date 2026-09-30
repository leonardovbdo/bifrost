# Bifrost — nome e metáfora do projeto

**Status:** Nome de trabalho do produto/backend deste TCC  
**Grafia no repositório:** `bifrost` (ASCII); opcionalmente **Bifröst** em textos narrativos

---

## Relação da Bifrost com o projeto

Na mitologia nórdica, a **Bifröst** é a ponte que liga mundos distintos. Ela não substitui o que existe em cada margem: é o **vão atravessável** entre reinos, o caminho pelo qual a passagem se torna possível — e, por isso, um lugar onde a travessia precisa de **ordem, reconhecimento e cuidado**.

Essa imagem descreve o papel do backend proposto neste trabalho. De um lado está a **interface com pessoas** — operadores, administradores e visualizadores, por meio de um protótipo de cliente do próprio TCC ou de outro front compatível. Do outro, o **runtime robótico** (no estudo de caso, o ecossistema NARA em ROS), com teleoperação, sensores, câmeras e navegação. Entre esses mundos não basta um fio solto de mensagens: é preciso uma **ponte governada** que autentique usuários, aplique níveis de acesso, selecione o perfil do robô autorizado, parametrize limites e registre eventos relevantes.

No modelo **híbrido** adotado, o fluxo de baixa latência (por exemplo, comandos de movimento e streams de visão) continua na via técnica já consolidada com o robô — rosbridge e serviços de vídeo. A **Bifrost**, aqui, é a camada que **estrutura e supervisiona** a travessia de produto: quem entra, com qual papel, para qual robô ou tecnologia, sob quais parâmetros, e com qual rastro de auditoria — inclusive alinhamento a boas práticas de proteção de dados.

O nome também antecipa a ambição de **não se limitar à cadeira NARA**. A Bifröst não existe para um único viajante; ela conecta **múltiplas travessias**. Da mesma forma, o backend organiza **perfis de sistemas autônomos** distintos, de modo que novas tecnologias possam ser integradas sem abandonar o núcleo de identidade, política e monitoramento.

Em síntese: **Bifrost** é a metáfora da *ponte governada* (o *control plane*) do projeto — entre pessoas e sistemas autônomos — enquanto o ROS e o cliente web permanecem, respectivamente, o mundo operacional e a face de interação.

### Tagline

> A ponte governada entre pessoas e sistemas autônomos.

---

## O que Bifrost **não** é

- Não é o `rosbridge` em si (ferramenta ROS de transporte)
- Não é o repositório `noblenara` (runtime / simulação)
- Não é uma IHM completa de terceiros

Bifrost é o **backend de governo** documentado e a implementar neste repositório TCC.
