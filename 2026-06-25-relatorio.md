# Relatório de Avaliação — EQ11 (DSC)

| | |
|---|---|
| **Data** | 2026-06-25 |
| **Repositório** | https://github.com/des-sist-corp-ufpb/projeto-eq11 |
| **Aplicação** | https://eq11.dsc.rodrigor.com |
| **Período de atividade** | 2026-06-10 → 2026-06-25 |
| **Total de commits** (sem merges, branch main) | 2 |
| **Integrantes** | Clodomir Alves De Oliveira Junior (@clodomiralves), Jean Matheus Nunes De Lima Barros (@jeanmatheusnunes) |

---

## 1. Tecnologias

- Spring Boot 3.4.5
- Thymeleaf
- Flyway (3 migrations)
- Spring Security

---

## 2. Análise Funcional

### Endpoints REST (6 mapeados)

| Método | Path | Arquivo |
|--------|------|---------|
| `GET` | `/login` | `AuthController.java` |
| `GET` | `/flashcards` | `FlashcardController.java` |
| `GET` | `/flashcards/{id}` | `FlashcardController.java` |
| `POST` | `/flashcards/gerar` | `FlashcardController.java` |
| `GET` | `/` | `HomeController.java` |
| `GET` | `/ping` | `PingController.java` |

### Entidades / Tabelas (5 encontradas)

- `flashcard`
- `deck`
- `produto (via V1__criar_tabela_produto.sql)`
- `deck (via V2__deck_flashcard.sql)`
- `flashcard (via V2__deck_flashcard.sql)`

### Migrations (3 arquivos)

- `V1__criar_tabela_produto.sql`
- `V2__deck_flashcard.sql`
- `V3__drop_produto.sql`

---

## 3. Análise Arquitetural

| Aspecto | Status | Observação |
|---------|--------|-----------|
| Arquitetura em camadas | ✅ | controller=✅  service=✅  repository=✅ |
| Testes automatizados | ✅ | 2 arquivo(s) de teste |
| Migrations versionadas | ✅ | 3 migration(s) |
| Logging | ✅ | @Slf4j / LoggerFactory / logging.getLogger detectado |
| Autenticação / Segurança | ✅ | Spring Security / JWT / decorator detectado |
| DTOs / Separação de dados | ✅ | classes *DTO / *Request / *Response detectadas |
| Tratamento global de exceções | ✅ | @ControllerAdvice / @ExceptionHandler detectado |
| Documentação de API (OpenAPI) | ❌ | não detectado |
| Variáveis de ambiente | ❌ | não detectado |
| Dockerfile / docker-compose | ✅ | presente |

---

## 4. Contribuição por Usuário

### Resumo

| Usuário | Commits (main) | Commits (GitHub API) | Linhas adicionadas | Linhas no código atual | % código atual |
|---------|---------------|---------------------|-------------------|----------------------|----------------|
| Clodomir Alves De Oliveira Junior (@clodomiralves) | 1 | **10** ⚠️ | 5.412 | 3.561 | 100% |
| Jean Matheus Nunes De Lima Barros (@jeanmatheusnunes) | 0 | **0** | 0 | 0 | 0% |
| *(sem login GitHub)* | 1 | 50% | — | — | — |

> **⚠️ Divergência entre commits locais e GitHub API:**
> - **@clodomiralves**: 1 commit(s) na branch `main` vs **10** registrados na API GitHub (commits em branches não mergeadas ou absorvidos via squash-merge sem preservação de autoria).
>

### Contribuição por Camada

| Camada | Total linhas | Clodomir Alves De Oliveira Junior (@clodomiralves) | Jean Matheus Nunes De Lima Barros (@jeanmatheusnunes) |
|--------|-------------|---------|---------|
| Controller | 1.137 | 100% | 0% |
| Frontend | 1.336 | 100% | 0% |
| Repository | 40 | 100% | 0% |
| Service | 456 | 100% | 0% |

---

## 5. Contribuição por Funcionalidade

Baseado em `git blame` nos arquivos de controller e service.

| Arquivo | Total linhas | Clodomir Alves De Oliveira Junior (@clodomiralves) | Jean Matheus Nunes De Lima Barros (@jeanmatheusnunes) |
|---------|-------------|---------|---------|
| `studyai.css` | 215 | 100% | 0% |
| `FlashcardService.java` | 187 | 100% | 0% |
| `IAServiceImpl.java` | 154 | 100% | 0% |
| `FlashcardController.java` | 122 | 100% | 0% |
| `flashcards.html` | 120 | 100% | 0% |
| `login.html` | 98 | 100% | 0% |
| `home.html` | 93 | 100% | 0% |
| `layout.html` | 90 | 100% | 0% |
| `studyai.js` | 82 | 100% | 0% |
| `flashcard-result.html` | 70 | 100% | 0% |
| `AuthController.java` | 48 | 100% | 0% |
| `PingController.java` | 47 | 100% | 0% |
| `HomeController.java` | 47 | 100% | 0% |
| `StudyAiApplication.java` | 44 | 100% | 0% |
| `StudyAiApplicationTests.java` | 43 | 100% | 0% |
| `PingControllerTest.java` | 37 | 100% | 0% |
| `V2__deck_flashcard.sql` | 33 | 100% | 0% |
| `IAService.java` | 28 | 100% | 0% |
| `V1__criar_tabela_produto.sql` | 25 | 100% | 0% |
| `V3__drop_produto.sql` | 10 | 100% | 0% |

---

*Relatório gerado automaticamente em 2026-06-25.*
*Os dados de contribuição são baseados em `git log --numstat` (linhas adicionadas) e `git blame` (linhas no código atual), excluindo commits de merge.*