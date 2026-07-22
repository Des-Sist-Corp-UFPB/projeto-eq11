# 03. Módulo FlashIA

O **FlashIA** é o módulo principal do StudyAI para gerar cartões de memorização (flashcards) ativos através da leitura e sintetização de textos (resumos, leis, anotações).

## O Fluxo de Negócio

1. **Recepção de Dados**: O usuário preenche o formulário na aba Flashcards. Os dados (banca, disciplina, texto, quantidade) chegam ao Spring via `FlashcardRequest`.
2. **Avaliação de Modo**: O `FlashcardService` verifica se a variável de ambiente `STUDYAI_AI_MODO` está como `real` ou `demo`.
3. **Comunicação com a IA (LangChain4j)**: 
    - O sistema passa os parâmetros para a interface declarativa `@AiService` chamada `FlashcardAiService`.
    - Os metadados de `@SystemMessage` e `@UserMessage` guiam a inteligência artificial para que ela retorne uma lista de flashcards usando o DTO `FlashcardResponse`.
    - A integração abstrai completamente a formatação JSON complexa. O LangChain4j garante que a saída seja um objeto Java mapeado (`FlashcardResponse`).
4. **Persistência (Entity)**:
    - O serviço cria uma entidade abstrata do tipo `Deck` associada a esse bloco de texto.
    - Em seguida, itera pelos cartões da IA e os adiciona como filhos da entidade `Deck` usando os objetos da classe `Flashcard`. O Hibernate/JPA grava isso em cascata no Postgres (`redacao_criterio`).
5. **Renderização via HTMX**:
    - O controller devolve o fragmento de Thymeleaf (`fragments/flashcard-result :: cards`) injetando a entidade Deck recém persistida.
    - O HTMX injeta o HTML no navegador do usuário (no elemento `#flash-resultado`), e a aba "flip" nativa em CSS cuida da interação de virar a carta.

## O problema do Type Erasure resolvido
A arquitetura anterior usava uma `List<FlashcardDTO>`. Isso quebrava por conta da limitação do Java chamada *Type Erasure*, onde o compilador perdia a tipagem Genérica da lista em tempo de execução, confundindo a IA na hora de mapear o JSON de volta para Java.

A correção imposta para o módulo (e adotada como padrão em todos os demais) é ter uma **Classe/Record Response "Wrapper"**, como `FlashcardResponse`, que engloba a lista interna (`List<FlashcardDTO>`). Assim, o parser Jackson e o LangChain4j leem o tipo de maneira robusta.
