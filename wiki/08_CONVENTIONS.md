# 08. Convenções do Projeto

Respeitar esses padrões é a garantia de um código-fonte sem atritos de merge (conflitos de versionamento) e agradável de ser auditado.

## Estrutura de Migrations Flyway

O nome dos arquivos `.sql` que gerenciam a evolução da estrutura do banco são restritos:
```
V{número}__{descrição_com_underscores}.sql
Exemplo: V1__criar_tabela_produto.sql
```

- **Nunca edite** uma migration já commitada ou em produção, o Flyway vai travar a aplicação de subir se detectar o checksum do script diferente. Crie sempre o próximo sequencial.
- Descrição preferencialmente em português e minúsculo (snake_case).

## Conventional Commits

Todas as mensagens de commits no nosso Git usam os padrões de Conventional Commits para ajudar os geradores de changelog e as auditorias.

```
feat: adicionar módulo de correção de redações
fix: corrigir cálculo de estatísticas da tela inicial
docs: atualizar documentação e criar a Wiki
refactor: simplificar métodos e extrair para o Service
test: cobrir o CorretorController com MockMvc
chore: atualizar versão de biblioteca no pom.xml
```

## Nomenclatura Java

| Elemento | Convenção | Exemplo |
|---|---|---|
| Package | lowercase | `br.ufpb.dsc.studyai.service` |
| Classe/Interface | PascalCase | `FlashcardService` / `DeckRepository` |
| Método | camelCase | `avaliarRedacao(...)` |
| Constante (final) | UPPER_SNAKE | `MAX_FLASHCARDS_PER_DECK` |
| Instância/Var | camelCase | `novaRedacao` |

## Padrão de Nomenclatura nos Fragmentos HTMX
Para Views complexas que usam fragmentos ou tabelas recarregadas sem refresh:
- `layout.html` — Base para carregar em outras views (`th:replace="~{layout :: layout(~{::main})}"`).
- Componentes e fragmentos ficam preferencialmente em pastas `/fragments/`.
- NUNCA usamos React ou Vue neste projeto. Pela simplicidade e poder computacional provido do Java no Backend.

## Validação Global

- DTOs (Data Transfer Objects) devem obrigatoriamente usar Bean Validation do Java EE (`@NotBlank`, `@Size`, `@Min`, etc).
- As assinaturas de Controllers (`@PostMapping`) devem marcar os beans/payloads com a anotação de validação (`@Valid` ou `@ModelAttribute`).
- DTOs devem ser implementados no formato de `record` para garantir a Imutabilidade desde a raiz e simplificar o *boilerplate*.
