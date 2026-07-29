# StudyAI BR 🎓🤖

**Plataforma de estudos com IA para Concursos Públicos e ENEM.**
Transforma textos, resumos e leis em material de estudo ativo — com a Inteligência
Artificial rodando de forma **segura no backend**, dentro de um sistema corporativo completo.

> **Disciplina:** Desenvolvimento de Sistemas Corporativos (DSC)
> **Professor:** Rodrigo Rebouças · **Instituição:** UFPB — Campus IV · **Equipe:** eq11

---

## 📑 Sumário
- [Visão geral](#-visão-geral)
- [Módulos do produto](#-módulos-do-produto)
- [Gestão de fases (onde estamos)](#-gestão-de-fases-onde-estamos)
- [Tecnologias](#-tecnologias)
- [Padrão arquitetural](#-padrão-arquitetural)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Como executar](#-como-executar)
- [Acesso e portas](#-acesso-e-portas)
- [Banco de dados e migrations](#-banco-de-dados-e-migrations)
- [Testes](#-testes)
- [Segurança (SAST)](#-segurança-sast)
- [CI/CD e Deploy](#-cicd-e-deploy)
- [Convenções de código](#-convenções-de-código)
- [Solução de problemas](#-solução-de-problemas)

---

## 🔭 Visão geral

O StudyAI resolve um problema concreto de quem estuda para concursos e ENEM: **converter
conteúdo bruto em estudo ativo** (flashcards, correção de redação, previsão de temas), sem
expor a chave de IA no navegador e guardando o histórico do aluno.

A regra de ouro do projeto: **a chamada de IA é sempre feita no servidor** (`IAService`),
com a chave em variável de ambiente. O navegador nunca toca em segredos.

---

## 🧩 Módulos do produto

| Módulo | O que faz | Status |
|--------|-----------|:------:|
| **FlashIA** | Gera flashcards (pergunta/resposta) a partir de um texto, salva em decks e exibe com flip + carrossel | ✅ **Funcionando** |
| **CorretorIA** | Corrige redação ENEM (5 competências), discursiva Cebraspe e peça OAB | 🔜 Planejado |
| **PrevêTema** | Projeta os temas mais prováveis da próxima prova a partir do histórico | 🔜 Planejado |

> 💡 **Modo demo:** o FlashIA funciona **sem internet e sem chave de API**
> (`studyai.ia.modo=demo`, padrão). Com uma chave configurada, o mesmo fluxo chama o
> provedor real (Anthropic ou Gemini).

---

## 🚦 Gestão de fases (onde estamos)

O desenvolvimento é incremental. Cada fase entrega algo demonstrável.

| Fase | Entrega | Status |
|:----:|---------|:------:|
| **0** | **Fundação** — scaffold, identidade visual (tema escuro, Syne + DM Sans), layout Thymeleaf, login (Spring Security), Docker e CI/CD configurados | ✅ Concluída |
| **1** | **FlashIA ponta a ponta** — domínio `Deck`/`Flashcard`, migration Flyway, `IAService` (interface) com modo demo + provedores reais, persistência, fragmentos HTMX, home com estatísticas reais | ✅ Concluída |
| **1.5** | **Auditoria + cobertura de testes** — módulo de log de auditoria (`audit_log`), integração de IA documentada e cobertura ≥ 85% | ✅ Concluída |
| **2** | **CorretorIA** — correção ENEM/Discursiva/OAB; entidade `Correcao` com `payload` JSONB; 3 endpoints + fragmentos de resultado | ⬜ Planejada (roadmap) |
| **3** | **PrevêTema + Histórico** — entidade `Previsao`, seed do histórico ENEM, página de histórico paginada | ⬜ Planejada (roadmap) |
| **4** | **Autenticação Avançada + OAuth2** — entidade `Usuario` (`UserDetailsService`), login por E-mail ou Username e integração social com **Google OAuth2** | ✅ Concluída |
| **4.5** | **Planos e limites** — planos GRATUITO/PRO e limites de uso impostos no servidor | ⬜ Planejada (roadmap) |
| **5** | **Hardening & Pagamentos** — webhook de pagamento, ajustes finais de SAST e deploy | ⬜ Planejada (roadmap) |

> ✅ **O que está implementado hoje:** o módulo **FlashIA** (geração de flashcards por IA)
> ponta a ponta, **Log de auditoria** e **Autenticação Completa** (banco local e Google OAuth2),
> com cobertura de testes ≥ 85%. As Fases 2–3 e planos (**CorretorIA**, **PrevêTema** e
> pagamentos) são **roadmap** — ainda não existem no código.

---

## 🛠 Tecnologias

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 3.4.5 |
| Web / MVC | Spring Web (Tomcat embutido) | 6.x |
| Templates | Thymeleaf + **HTMX** | 3.x + 2.0.4 |
| Interatividade | HTMX (Ajax sem SPA) + JS client-side mínimo | — |
| Persistência | Spring Data JPA / Hibernate | 6.6 |
| Banco de dados | PostgreSQL | 16 |
| Migrations | Flyway | 11.x |
| Segurança | Spring Security | 6.x |
| Cliente HTTP (IA) | Spring `RestClient` | 6.x |
| JSON | Jackson | 2.x |
| Build | Maven | 3.9+ |
| Testes | JUnit 5 + Testcontainers | — |
| Container | Docker + Docker Compose | — |
| CI/CD | GitHub Actions + GHCR | — |
| SAST | SpotBugs + FindSecBugs, OWASP Dependency-Check, Semgrep, Trivy | — |

---

## 🏛 Padrão arquitetural

### Arquitetura em camadas (Layered / N-tier) + MVC server-side
A interface é renderizada no servidor (Thymeleaf) e a interatividade vem do **HTMX**
(troca de fragmentos de HTML via Ajax, sem precisar de um framework SPA). As
responsabilidades são separadas em camadas bem definidas:

```
┌─────────────────────────────────────────────────────────────┐
│  Navegador  (Thymeleaf + HTMX + JS mínimo: flip, carrossel)  │
└───────────────┬─────────────────────────────────────────────┘
                │  HTTP / Ajax (hx-post, hx-target)
┌───────────────▼─────────── controller/ ─────────────────────┐
│  HomeController · FlashcardController · AuthController        │  ← entrada HTTP
└───────────────┬─────────────────────────────────────────────┘
┌───────────────▼─────────── service/ ────────────────────────┐
│  FlashcardService  ──►  IAService (interface)                 │  ← regra de negócio
│                          ├─ modo DEMO (offline)               │     (@Transactional)
│                          └─ provedor real (Anthropic|Gemini)  │
└───────────────┬─────────────────────────────────────────────┘
┌───────────────▼─────────── repository/ ─────────────────────┐
│  DeckRepository · FlashcardRepository (Spring Data JPA)       │  ← acesso a dados
└───────────────┬─────────────────────────────────────────────┘
┌───────────────▼─────────── domain/ ─────────────────────────┐
│  Deck (1 ──► N) Flashcard   →   PostgreSQL (schema via Flyway)│  ← entidades JPA
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de uma requisição (FlashIA)
1. O formulário envia `hx-post="/flashcards/gerar"` (HTMX).
2. `FlashcardController` recebe um `FlashcardRequest` (record).
3. `FlashcardService` monta os prompts e chama `IAService.completar(system, user)`.
4. O `IAService` retorna cartões de **exemplo** (modo demo) **ou** chama o provedor real.
5. O serviço faz **parse robusto do JSON** (remove cercas markdown, valida com Jackson).
6. Persiste `Deck` + `Flashcard` e devolve um **fragmento Thymeleaf** com os cartões.
7. O HTMX troca apenas o `#flash-resultado` — sem recarregar a página.

### Padrões de projeto aplicados
- **Injeção de Dependência** (construtor) em todos os componentes Spring.
- **DTO** com `records` Java imutáveis (`FlashcardRequest`, `FlashcardDTO`).
- **Repository** (Spring Data JPA) — interfaces sem implementação manual.
- **Strategy** — `IAService` abstrai o provedor (demo / Anthropic / Gemini) sem
  acoplar a regra de negócio a um fornecedor específico.
- **Service Layer transacional** — `@Transactional(readOnly = true)` por padrão,
  escrita explícita onde há persistência.
- **Configuration Properties** — `IAProperties` (`@ConfigurationProperties("studyai.ia")`).
- **Migrations versionadas** — Flyway (nunca editar uma migration já aplicada).

### Pacotes (`br.ufpb.dsc.studyai`)
| Pacote | Responsabilidade |
|--------|------------------|
| `config/` | `SecurityConfig`, `IAProperties`, `GlobalModelAttributes` |
| `controller/` | Recebem requisições HTTP/HTMX e devolvem views/fragmentos |
| `domain/` | Entidades JPA (`Deck`, `Flashcard`) |
| `dto/` | Records de entrada/saída |
| `exception/` | Exceções de domínio (`IAIndisponivelException`) |
| `repository/` | Interfaces Spring Data JPA |
| `service/` | Lógica de negócio + integração com IA |

---

## 📂 Estrutura do projeto

```
projeto-eq11/
├── docker-compose.yml              # Orquestração ÚNICA (banco + app + perfis dev/scan)
├── Dockerfile                      # (em docker/) build multi-stage de produção
├── .env / .env.example             # Variáveis (DB_*, APP_IMAGE) — .env é gitignored
├── pom.xml
├── .github/workflows/deploy.yml    # Pipeline CI/CD (build → push GHCR → deploy)
├── docker/
│   └── Dockerfile
├── src/main/java/br/ufpb/dsc/studyai/
│   ├── StudyAiApplication.java     # Classe principal (@SpringBootApplication)
│   ├── config/                     # SecurityConfig, IAProperties, GlobalModelAttributes
│   ├── controller/                 # Home, Flashcard, Auth
│   ├── domain/                     # Deck, Flashcard
│   ├── dto/                        # FlashcardRequest, FlashcardDTO (records)
│   ├── exception/                  # IAIndisponivelException
│   ├── repository/                 # DeckRepository, FlashcardRepository
│   └── service/                    # IAService, IAServiceImpl, FlashcardService
├── src/main/resources/
│   ├── application.yml             # Config base + studyai.ia.*
│   ├── application-dev.yml         # Perfil local (mvn spring-boot:run)
│   ├── application-prod.yml        # Perfil container (lê DB_*)
│   ├── db/migration/               # Scripts SQL versionados (inclui V9__add_email_e_oauth_to_usuario)
│   ├── static/                     # css/studyai.css, js/studyai.js
│   └── templates/                  # auth/login, auth/cadastro + studyai/{layout,home,flashcards}
└── docs/                           # PITCH, SECURITY, CONVENTIONS, arquitetura
```

---

## ▶ Como executar

### Pré-requisitos
- **Java 21** (Eclipse Temurin recomendado) · **Maven 3.9+** · **Docker Desktop** (em execução).

### Opção A — Tudo em containers (recomendado para demonstração)
Um único comando sobe o **banco + aplicação**:
```bash
docker compose up -d --build
```
A aplicação fica disponível em **http://127.0.0.1:8111** (porta padronizada da equipe).

### Opção B — Banco no Docker + app local (recomendado para desenvolvimento)
```bash
# Sobe só o banco
docker compose up -d postgres
# Roda a aplicação localmente (perfil dev, hot-reload)
mvn spring-boot:run
```
Nesse modo a app responde em **http://localhost:8080**.

### Perfis opcionais do Compose
```bash
# Adminer (interface web do banco) em http://127.0.0.1:8112
docker compose --profile dev up -d adminer

# Trivy (scan de vulnerabilidades no filesystem)
docker compose --profile scan up trivy
```

### Parar
```bash
docker compose down          # para (mantém os dados)
docker compose down -v       # para e APAGA os dados do banco
```

---

## 🔑 Acesso e portas

| Recurso | Em container (Compose) | Local (`mvn spring-boot:run`) |
|---------|------------------------|-------------------------------|
| Aplicação | http://127.0.0.1:8111 | http://localhost:8080 |
| Login | Cadastro manual (Username/E-mail) ou **Google OAuth2** | Cadastro manual ou **Google OAuth2** |
| Adminer (perfil `dev`) | http://127.0.0.1:8112 | — |
| Health check | `…:8111/actuator/health` | `…:8080/actuator/health` |

---

## 🗄 Banco de dados e migrations

- **PostgreSQL 16**, schema 100% controlado por **Flyway** (`ddl-auto: validate` — o
  Hibernate **nunca** altera o banco).
- Credenciais via variáveis `DB_*` (arquivo `.env`), com defaults `eq11` para rodar sem `.env`.

| Migration | Descrição |
|-----------|-----------|
| `V1__criar_tabela_produto.sql` | Tabela de exemplo do boilerplate (legado) |
| `V2__deck_flashcard.sql` | Tabelas `deck` e `flashcard` (módulo FlashIA) |
| `V3__drop_produto.sql` | Remove a tabela `produto` (não usada pelo StudyAI) |
| `V4__audit_log.sql` | Tabela para logs de auditoria |
| `V9__add_email_e_oauth_to_usuario.sql`| Adiciona colunas `email` e `provider` para integração OAuth2 e login estendido |

> ⚠️ **Nunca edite uma migration já aplicada** — o Flyway valida o checksum. Para
> mudar o schema, crie uma nova (`V4__...sql`).

---

## ✅ Testes
```bash
mvn test                  # testes + relatório JaCoCo (target/site/jacoco/index.html)
mvn clean test jacoco:report   # idem, partindo do zero
mvn verify                # testes + cobertura na fase verify
```

A suíte tem **63 testes** e roda **com ou sem Docker**: os testes unitários e de fatia
(Mockito, `@WebMvcTest`, `MockRestServiceServer`) não precisam de banco; o teste de
integração `StudyAiApplicationTests` sobe um PostgreSQL real via **Testcontainers** e é
**automaticamente pulado** quando o Docker não está disponível
(`@Testcontainers(disabledWithoutDocker = true)`).

### Cobertura de testes

**Cobertura de testes total: 91%** (instruções 91%, ramos 67%) — bem acima do mínimo de 85% exigido.
Relatório versionado em [`cobertura/jacoco/index.html`](cobertura/jacoco/index.html)
(gerado com JaCoCo; a classe de bootstrap `StudyAiApplication` é excluída da medição).

---

## 📋 Log de Auditoria

Registra as ações de usuário relevantes do sistema, para rastreabilidade.

- **O que é auditado:** as ações reais existentes hoje — **login** bem-sucedido,
  **falha de login**, **logout** e **geração de flashcard** (`POST /flashcards/gerar`).
- **Onde fica armazenado:** tabela **`audit_log`** (PostgreSQL, criada pela migração
  Flyway `V4__audit_log.sql`). Campos principais: `usuario`, `acao`
  (`LOGIN` / `LOGIN_FALHA` / `LOGOUT` / `GERAR_FLASHCARD`), `entidade`, `entidade_id`,
  `detalhes`, `ip` e `data_hora`. É um registro *append-only* (apenas inserções).
- **Como foi implementado:** abordagem **híbrida**, escolhida pelo escopo enxuto do
  sistema (poucas ações de usuário):
  - um **service dedicado** (`AuditLogService`) chamado explicitamente no ponto de
    negócio — a geração de flashcard, no `FlashcardController` — onde a chamada é
    direta e rastreável;
  - **listeners de evento do Spring Security** para login/logout, porque o
    `POST /login` é tratado internamente pelo framework (não há método de controller
    onde inserir a chamada). Isso evita acoplar a configuração de segurança. AOP/
    interceptor seria desnecessário para um único endpoint de negócio.

  O `AuditLogService` resolve automaticamente o **usuário** (do `SecurityContextHolder`,
  com *fallback* `anonimo`) e o **IP** (da requisição atual, respeitando
  `X-Forwarded-For`). Falhas ao gravar o log nunca derrubam a operação principal.
- **Classes participantes:**
  - `src/main/java/br/ufpb/dsc/studyai/audit/AuditLog.java` (entidade)
  - `src/main/java/br/ufpb/dsc/studyai/audit/AuditLogRepository.java`
  - `src/main/java/br/ufpb/dsc/studyai/audit/AuditLogService.java`
  - `src/main/java/br/ufpb/dsc/studyai/audit/AuthAuditListener.java` (login / falha de login)
  - `src/main/java/br/ufpb/dsc/studyai/audit/AuditLogoutHandler.java` (logout)
  - `src/main/resources/db/migration/V4__audit_log.sql` (schema)
  - ponto de captura de negócio: `src/main/java/br/ufpb/dsc/studyai/controller/FlashcardController.java`

---

## 🔌 Integração com Serviço Externo

> O **PostgreSQL** fornecido pela disciplina é infraestrutura básica e **não** conta como
> integração externa. As integrações implementadas são de **IA** e **Autenticação (OAuth2)**.

### 1. IA (Geração de Flashcards)
- **Qual serviço:** geração de flashcards por IA (Anthropic Claude, Google Gemini e modo demo).
  via padrão **Strategy** — **Anthropic Claude** e **Google Gemini** — além de um **modo
  demo** embutido (flashcards de exemplo) usado como *fallback*.
- **Para que é usado:** transformar um texto em flashcards. O `FlashcardService` monta os
  prompts (system + user) e delega ao `IAService`, que chama o provedor configurado e
  devolve o JSON; o serviço faz o parse robusto e persiste o deck.
- **Classes participantes:**
  - `src/main/java/br/ufpb/dsc/studyai/service/IAService.java` (interface — contrato do provedor)
  - `src/main/java/br/ufpb/dsc/studyai/service/IAServiceImpl.java` (demo / Anthropic / Gemini)
  - `src/main/java/br/ufpb/dsc/studyai/config/IAProperties.java` (configuração e modo)
  - `src/main/java/br/ufpb/dsc/studyai/config/IAClientConfig.java` (bean `RestClient` com timeout)
  - `src/main/java/br/ufpb/dsc/studyai/service/FlashcardService.java` (orquestra a chamada e o parse)
- **Como é configurado** (variáveis de ambiente — **sem valores reais no repositório**):

  | Variável | Função |
  |----------|--------|
  | `STUDYAI_AI_MODO` | `demo` (padrão) ou `real` |
  | `STUDYAI_AI_PROVEDOR` | `anthropic` ou `gemini` |
  | `STUDYAI_AI_API_KEY` | chave do provedor (somente em ambiente, nunca versionada) |
  | `STUDYAI_AI_MODELO` | modelo a usar no provedor |

  **Importante para avaliação:** `IAProperties.isDemo()` retorna `true` por padrão quando
  **não há chave configurada** (ou `modo=demo`). Ou seja, **a integração existe e está
  pronta**, mas roda em **modo demo por padrão** — garantindo que a aplicação funcione sem
  internet nem chave. Para exercitar a chamada real, defina `STUDYAI_AI_MODO=real` e as
  demais variáveis acima.
- **Nota de segurança:** a chave do Gemini é enviada no cabeçalho `x-goog-api-key`. O Anthropic já usa o cabeçalho `x-api-key`.

### 2. Autenticação (Google OAuth2)
- **Qual serviço:** Google Identity Services (OAuth2).
- **Para que é usado:** Permitir que os usuários criem conta e façam login na plataforma utilizando sua conta Google existente de forma rápida e segura (Single Sign-On).
- **Como é configurado:** via variáveis de ambiente no `.env`:
  ```env
  GOOGLE_CLIENT_ID=seu_client_id_aqui
  GOOGLE_CLIENT_SECRET=seu_client_secret_aqui
  ```
- **Fluxo:** O Spring Security gerencia a comunicação com o Google automaticamente. O provedor recupera o E-mail e o Nome do usuário e passa para o nosso `CustomOAuth2UserService` que registra ou autentica o usuário no banco local.


## 🔒 Segurança (SAST)
```bash
mvn verify -Psecurity                       # SpotBugs + FindSecBugs + OWASP Dependency-Check
docker compose --profile scan up trivy      # Trivy (filesystem)
mvn versions:display-dependency-updates -Pversions   # dependências desatualizadas
```
Boas práticas já aplicadas: CSRF ativo (inclusive nos POSTs HTMX, via header + meta tag),
nenhum segredo versionado, senhas com BCrypt, chave de IA somente em variável de ambiente.
Detalhes em [`docs/SECURITY.md`](docs/SECURITY.md).

---

## 🚀 CI/CD e Deploy

O pipeline ([`.github/workflows/deploy.yml`](.github/workflows/deploy.yml)) roda a cada
`push` na `main`:

1. **Build & push** — constrói a imagem com [`docker/Dockerfile`](docker/Dockerfile) e
   publica em `ghcr.io/des-sist-corp-ufpb/projeto-eq11:latest`.
2. **Deploy** — via SSH no servidor da disciplina (`dsc.rodrigor.com`), que puxa a
   imagem e sobe o `docker-compose.yml`.

### Configuração necessária
- **Secrets no GitHub:** `SSH_DEPLOY_KEY`, `SSH_USERNAME` (o `GITHUB_TOKEN` é automático).
- **`.env` no servidor** (gitignored, contém segredos):
  ```env
  APP_IMAGE=ghcr.io/des-sist-corp-ufpb/projeto-eq11:latest
  DB_HOST=postgres
  DB_PORT=5432
  DB_NAME=eq11
  DB_USER=eq11
  DB_PASSWORD=<senha-forte>
  GOOGLE_CLIENT_ID=<id-gerado-no-cloud>
  GOOGLE_CLIENT_SECRET=<secret-gerado-no-cloud>
  ```
  > Regra de formatação: **sem espaços** ao redor do `=`.

A aplicação publicada responde em `127.0.0.1:8111` no servidor (atrás do proxy reverso da disciplina).

---

## 📐 Convenções de código
- Domínio, endpoints e comentários em **português**.
- **DTOs como `records`** (imutáveis).
- `@Transactional(readOnly = true)` em métodos de consulta.
- **Conventional Commits**: `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`.
- Migrations Flyway versionadas — nunca editar as já aplicadas.

Detalhes em [`docs/CONVENTIONS.md`](docs/CONVENTIONS.md).

---

## 🩺 Solução de problemas

| Sintoma | Causa / Solução |
|---------|-----------------|
| `Port 8111 already in use` | Outra instância rodando. Pare com `docker compose down` ou libere a porta. |
| `Cannot connect to the Docker daemon` | Docker Desktop não está em execução — abra e aguarde inicializar. |
| `Connection refused` ao banco | Postgres ainda subindo. Verifique `docker compose ps` (container `eq11-postgres` deve estar `healthy`). |
| Erro de compilação Java | Confirme `mvn -version` mostrando **Java 21**; ajuste `JAVA_HOME` se necessário. |
| Flyway: *non-empty schema with no history* | Banco criado sem as migrations. Recrie: `docker compose down -v && docker compose up -d`. |
| App não conecta após trocar o `.env` | Recrie o volume do banco: `docker compose down -v && docker compose up -d`. |

---

> **StudyAI BR** — Equipe eq11 · Desenvolvimento de Sistemas Corporativos · UFPB Campus IV
