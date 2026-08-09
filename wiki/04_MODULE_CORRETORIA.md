# 04. Módulo CorretorIA

O **CorretorIA** é o segundo módulo principal do sistema. Sua função é receber uma redação ou prova discursiva de um aluno e, utilizando LLMs (Modelos de Linguagem), corrigir e detalhar notas de acordo com os critérios de uma banca específica (Ex: ENEM, Cebraspe).

## Estrutura de Domínio e Banco de Dados

O banco de dados foi expandido com a tabela `redacao` e a tabela `redacao_criterio` (migração `V6__redacao.sql` do Flyway).
- `Redacao`: Armazena a banca, o tema da redação, o texto cru original enviado pelo usuário, a `notaTotal` alcançada e o `comentarioGeral` (string longa).
- `Criterio`: Entidade filha. Representa cada uma das competências avaliadas. (No ENEM, as competências de 1 a 5). Possui o `nome` do critério, a `nota` específica dele e um `comentario` justificando-a.

## Comunicação Robusta com a Inteligência Artificial

A interface `@AiService` usada neste módulo chama-se `CorretorAiService`. 

### Prompts Declarativos (Annotations)
O Spring injeta os dados (banca, tema, texto) dentro da anotação `@UserMessage` usando *placeholders* `{{nome_da_variavel}}`, o que garante segurança contra Injections, pois a injeção é tratada pela engine do LangChain4j.

O `@SystemMessage` garante que o modelo assuma uma *persona* de avaliador extremamente rigoroso.

### O DTO Wrapper (CorretorResponse)
Assim como ocorreu no módulo `FlashIA`, utilizamos um DTO envelopado chamado `CorretorResponse` (que implementa `double notaTotal`, `String comentarioGeral` e `List<CriterioAvaliacao> criterios`).

Ao retornar o tipo estrito da interface `CorretorAiService`, a ferramenta de Structured Output extrai a árvore JSON perfeitamente para essa hierarquia de objetos Java. Nenhuma manipulação manual de String ou Regex é requerida no `CorretorService`.

## Exibição via HTMX
1. Ao colar a redação na aba "Corretor", o *form* realiza um Post e esconde o botão, mostrando o ícone de carregamento ("spinner").
2. O servidor envia o payload pra IA.
3. Ao retornar, o Thymeleaf converte os dados do objeto `Redacao` persistido para HTML visual usando a biblioteca de componentes (classes do CSS).
4. O fragmento inteiro de sucesso ou de erro injeta-se no layout limpidamente.
