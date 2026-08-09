# 10. Módulo RoadmapIA

O **RoadmapIA** é o terceiro módulo principal do sistema. Ele recebe as respostas de um
questionário de perfil e devolve um **plano de estudos para o ENEM** que cobre exatamente
o intervalo escolhido pelo aluno — do primeiro dia de estudo até a data da prova —
organizado em blocos semanais com tarefas diárias.

> Este módulo substituiu o antigo **PrevêTema**, que nunca saiu do planejamento.

## O Fluxo de Negócio

1. **Questionário de perfil**: o aluno responde 6 perguntas na aba RoadmapIA (período,
   horas por dia, curso pretendido, áreas de dificuldade, experiência anterior com o ENEM
   e observações livres). Os dados chegam ao Spring via `RoadmapRequest`.
2. **Validação do período**: o `RoadmapService` converte as datas, exige que a final seja
   posterior à inicial e recusa períodos acima de **26 semanas** (~6 meses).
3. **Cálculo do calendário**: o serviço divide o intervalo em semanas **em Java**, não na IA
   (ver a seção abaixo). A última semana pode ser mais curta que 7 dias.
4. **Comunicação com a IA (LangChain4j)**: a interface `@AiService` `RoadmapAiService` recebe
   o perfil e o número de semanas e devolve um `RoadmapResponse` já mapeado para objetos Java.
5. **Validação da resposta**: cada tarefa é conferida antes de virar entidade — data dentro
   da semana, duração dentro do tempo disponível, assunto não vazio.
6. **Persistência**: `Roadmap` → `SemanaEstudo` → `TarefaEstudo`, gravados em cascata pelo
   Hibernate (migração `V10__roadmap.sql`).
7. **Renderização via HTMX**: o controller devolve `fragments/roadmap-result :: roadmap` e o
   HTMX injeta o cronograma em `#roadmap-resultado`. As semanas viram um acordeão — a
   primeira já vem aberta, e `toggleSemana()` (em `studyai.js`) abre e fecha as demais.

## Por que o calendário é calculado no servidor

Modelos de linguagem erram aritmética de datas com frequência: pulam semanas, repetem
números ou colocam tarefas fora do período pedido. Se o plano dependesse da IA para isso,
o aluno receberia um cronograma que não cobre a data da prova.

A divisão do intervalo em semanas é feita em Java e a IA fica responsável apenas pelo
**conteúdo pedagógico** (foco da semana e tarefas). O `RoadmapService` percorre o
**calendário real**, não a resposta da IA — assim, mesmo que o modelo devolva menos
semanas do que o pedido, o plano continua cobrindo o intervalo inteiro:

| Problema na resposta da IA | O que o serviço faz |
|---|---|
| Semana ausente | Preenche com o plano padrão (teoria, exercícios, redação, revisão) e loga um `WARN` |
| Data fora da semana ou mal formatada | Reposiciona a tarefa pela ordem em que veio |
| Duração absurda (ex.: 6000 min) | Limita ao total de horas disponíveis por dia |
| Duração ínfima (ex.: 2 min) | Eleva ao piso de 15 minutos |
| Assunto vazio | Descarta a tarefa |

Esse comportamento está coberto por testes em `RoadmapServiceTest`.

## O limite de 26 semanas

Um plano detalhado de mais de meio ano estoura o limite de resposta do modelo (vira
conteúdo repetido) e a chamada passa do `studyai.ia.timeout-segundos`. Por isso o serviço
recusa períodos maiores, com mensagem explicando o motivo. A constante fica em
`RoadmapService.MAX_SEMANAS`.

O detalhamento também se adapta ao tamanho do plano: até 8 semanas, a IA recebe permissão
para até 6 tarefas por semana; acima disso, 4.

## Modo Demo

Como nos demais módulos, o RoadmapIA funciona **sem chave de API e sem internet**. Em modo
demo, o serviço monta um plano determinístico em Java, alternando as áreas do ENEM ao longo
das semanas — cobrindo o período real escolhido pelo aluno. O mesmo gerador serve de rede de
segurança quando a IA omite uma semana, o que evita duplicar código entre demo e produção.

## Estrutura de Domínio e Banco de Dados

| Entidade | Tabela | Papel |
|---|---|---|
| `Roadmap` | `roadmap` | Plano completo: período, perfil respondido e estratégia escrita pela IA |
| `SemanaEstudo` | `roadmap_semana` | Bloco semanal com datas (calculadas no servidor) e foco |
| `TarefaEstudo` | `roadmap_tarefa` | Tarefa de um dia: assunto, descrição e duração |

Como nos outros módulos, as consultas sempre filtram pelo dono
(`findByIdAndUsuarioUsername`): trocar o id na URL não dá acesso ao plano de outro usuário.

## Auditoria

A geração de um plano registra a ação `GERAR_ROADMAP` na tabela `audit_log`, com a
entidade `roadmap`, o id gerado e um resumo (`"4 semanas; 24 tarefas"`).
