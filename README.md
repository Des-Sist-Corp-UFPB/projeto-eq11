# StudyAI BR 🎓🤖

**Plataforma de estudos com IA para Concursos Públicos e ENEM.**
Transforma textos, resumos e leis em material de estudo ativo — com a Inteligência
Artificial rodando de forma **segura no backend**, dentro de um sistema corporativo completo.

> **Disciplina:** Desenvolvimento de Sistemas Corporativos (DSC)
> **Professor:** Rodrigo Rebouças · **Instituição:** UFPB — Campus IV · **Equipe:** eq11

> 🎥 **Vídeo de apresentação (avaliação):** <https://youtu.be/kNmebS0tUa4>
> Demonstração completa do sistema — todas as telas, integrações, auditoria e observabilidade.

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
- [Observabilidade (Grafana/OpenTelemetry + Umami)](#-observabilidade-grafanaopentelemetry--umami)
- [Segurança (SAST)](#-segurança-sast)
- [CI/CD e Deploy](#-cicd-e-deploy)
- [Convenções de código](#-convenções-de-código)
- [Solução de problemas](#-solução-de-problemas)

---

## 🔭 Visão geral

O StudyAI resolve um problema concreto de quem estuda para concursos e ENEM: **converter
conteúdo bruto em estudo ativo** (flashcards, correção de redação e plano de estudos), sem
expor a chave de IA no navegador e guardando o histórico do aluno.

A regra de ouro do projeto: **a chamada de IA é sempre feita no servidor** (interfaces
`@AiService` do LangChain4j), com a chave em variável de ambiente. O navegador nunca toca
em segredos.

---

## 🧩 Módulos do produto

| Módulo | O que faz | Status |
|--------|-----------|:------:|
| **FlashIA** | Gera flashcards (pergunta/resposta) a partir de um texto, salva em decks e exibe com flip + carrossel | ✅ **Funcionando** |
| **CorretorIA** | Corrige redação por banca (ENEM, Cebraspe, Vunesp e genérica), com nota e comentário por critério | ✅ **Funcionando** |
| **RoadmapIA** | A partir de um questionário de perfil, monta um plano de estudos para o ENEM semana a semana, do primeiro dia até a data da prova | ✅ **Funcionando** |

> 💡 **Modo demo:** os três módulos funcionam **sem internet e sem chave de API**
> (`studyai.ia.modo=demo`, padrão). Com uma chave configurada, o mesmo fluxo chama o
> provedor real (Anthropic, Gemini ou OpenAI).

---

## 🚦 Gestão de fases (onde estamos)

O desenvolvimento é incremental. Cada fase entrega algo demonstrável.

| Fase | Entrega | Status |
|:----:|---------|:------:|
| **0** | **Fundação** — scaffold, identidade visual (tema escuro, Syne + DM Sans), layout Thymeleaf, login (Spring Security), Docker e CI/CD configurados | ✅ Concluída |
| **1** | **FlashIA ponta a ponta** — domínio `Deck`/`Flashcard`, migration Flyway, integração de IA via LangChain4j (`@AiService`) com modo demo + provedores reais, persistência, fragmentos HTMX, home com estatísticas reais | ✅ Concluída |
| **1.5** | **Auditoria + cobertura de testes** — módulo de log de auditoria (`audit_log`), integração de IA documentada e cobertura ≥ 85% | ✅ Concluída |
| **2** | **CorretorIA** — correção por banca; entidades `Redacao`/`Criterio`; endpoints + fragmentos de resultado | ✅ Concluída |
| **3** | **Autenticação Avançada + OAuth2** — entidade `Usuario` (`UserDetailsService`), login por E-mail ou Username e integração social com **Google OAuth2** | ✅ Concluída |
| **4** | **RoadmapIA** — questionário de perfil, entidades `Roadmap`/`SemanaEstudo`/`TarefaEstudo`, cronograma semanal em acordeão | ✅ Concluída |
| **4.5** | **Planos e limites** — planos GRATUITO/PRO e limites de uso impostos no servidor | ⬜ Planejada (roadmap) |
| **5** | **Hardening & Pagamentos** — webhook de pagamento, ajustes finais de SAST e deploy | ⬜ Planejada (roadmap) |

> ✅ **O que está implementado hoje:** os três módulos de IA (**FlashIA**, **CorretorIA** e
> **RoadmapIA**) ponta a ponta, **Log de auditoria** e **Autenticação Completa** (banco local
> e Google OAuth2). A Fase 4.5 (planos e pagamentos) é **roadmap** — ainda não existe no código.

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
| Integração com IA | LangChain4j (`@AiService`) | 0.36.2 |
| JSON | Jackson | 2.x |
| Build | Maven | 3.9+ |
| Testes | JUnit 5 + Testcontainers | — |
| Container | Docker + Docker Compose | — |
| CI/CD | GitHub Actions + GHCR | — |
| SAST | SpotBugs + FindSecBugs, OWASP Dependency-Check, Semgrep, Trivy | — |
| Observabilidade (backend) | OpenTelemetry (agente Java) → Grafana/Loki/Tempo | — |
| Analytics (frontend) | Umami | — |

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
│  Home · Flashcard · Corretor · Roadmap · Auth · Ping          │  ← entrada HTTP
└───────────────┬─────────────────────────────────────────────┘
┌───────────────▼─────────── service/ ────────────────────────┐
│  FlashcardService · CorretorService · RoadmapService          │  ← regra de negócio
│         │                                                     │     (@Transactional)
│         └──► @AiService (LangChain4j — interface declarativa)  │
│                ├─ modo DEMO (offline, sem chave)              │
│                └─ provedor real (Anthropic|Gemini|OpenAI)     │
└───────────────┬─────────────────────────────────────────────┘
┌───────────────▼─────────── repository/ ─────────────────────┐
│  Deck · Redacao · Roadmap · Usuario (Spring Data JPA)         │  ← acesso a dados
└───────────────┬─────────────────────────────────────────────┘
┌───────────────▼─────────── domain/ ─────────────────────────┐
│  Deck→Flashcard · Redacao→Criterio · Roadmap→Semana→Tarefa    │  ← entidades JPA
│                        →   PostgreSQL (schema via Flyway)     │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de uma requisição (FlashIA)
1. O formulário envia `hx-post="/flashcards/gerar"` (HTMX).
2. `FlashcardController` recebe um `FlashcardRequest` (record).
3. `FlashcardService` decide entre o **modo demo** (cartões de exemplo, sem rede) e a IA real.
4. Na IA real, chama `FlashcardAiService.gerarFlashcards(...)` — o LangChain4j envia os prompts
   e devolve um `FlashcardResponse` já mapeado, sem parse manual de JSON.
5. Persiste `Deck` + `Flashcard` e devolve um **fragmento Thymeleaf** com os cartões.
6. O HTMX troca apenas o `#flash-resultado` — sem recarregar a página.

### Fluxo de uma requisição (RoadmapIA)
1. O questionário envia `hx-post="/roadmap/gerar"` (HTMX).
2. `RoadmapController` recebe um `RoadmapRequest` (record).
3. `RoadmapService` valida o período e **calcula as semanas em Java** (a IA não faz conta de data).
4. Chama `RoadmapAiService.gerarRoadmap(...)` e **valida o que voltou** (datas dentro da semana,
   durações dentro do tempo disponível, semanas ausentes preenchidas com o plano padrão).
5. Persiste `Roadmap` → `SemanaEstudo` → `TarefaEstudo` e devolve o fragmento do cronograma.
6. O HTMX troca apenas o `#roadmap-resultado`; as semanas viram um acordeão client-side.

### Padrões de projeto aplicados
- **Injeção de Dependência** (construtor) em todos os componentes Spring.
- **DTO** com `records` Java imutáveis (`FlashcardRequest`, `RoadmapRequest`, ...).
- **Repository** (Spring Data JPA) — interfaces sem implementação manual.
- **Strategy** — `LangChain4jConfig` escolhe o provedor (Anthropic / Gemini / OpenAI / demo)
  sem acoplar a regra de negócio a um fornecedor específico.
- **Service Layer transacional** — `@Transactional(readOnly = true)` por padrão,
  escrita explícita onde há persistência.
- **Configuration Properties** — `IAProperties` (`@ConfigurationProperties("studyai.ia")`).
- **Migrations versionadas** — Flyway (nunca editar uma migration já aplicada).

### Pacotes (`br.ufpb.dsc.studyai`)
| Pacote | Responsabilidade |
|--------|------------------|
| `audit/` | Trilha de auditoria (`AuditLog`, service, listeners) |
| `config/` | `SecurityConfig`, `LangChain4jConfig`, `IAProperties`, `GlobalModelAttributes` |
| `controller/` | Recebem requisições HTTP/HTMX e devolvem views/fragmentos |
| `domain/` | Entidades JPA (`Deck`/`Flashcard`, `Redacao`/`Criterio`, `Roadmap`/`SemanaEstudo`/`TarefaEstudo`, `Usuario`) |
| `dto/` | Records de entrada/saída |
| `exception/` | Exceções de domínio (`IAIndisponivelException`) |
| `repository/` | Interfaces Spring Data JPA |
| `service/` | Lógica de negócio + integração com IA (`@AiService`) |

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
│   ├── audit/                      # AuditLog, AuditLogService, listeners
│   ├── config/                     # SecurityConfig, LangChain4jConfig, IAProperties
│   ├── controller/                 # Home, Flashcard, Corretor, Roadmap, Auth, Ping
│   ├── domain/                     # Deck/Flashcard, Redacao/Criterio, Roadmap/Semana/Tarefa, Usuario
│   ├── dto/                        # Records de entrada e saída de cada módulo
│   ├── exception/                  # IAIndisponivelException
│   ├── repository/                 # Deck, Flashcard, Redacao, Roadmap, Usuario
│   └── service/                    # *Service (negócio) + *AiService (@AiService LangChain4j)
├── src/main/resources/
│   ├── application.yml             # Config base + studyai.ia.*
│   ├── application-dev.yml         # Perfil local (mvn spring-boot:run)
│   ├── application-prod.yml        # Perfil container (lê DB_*)
│   ├── db/migration/               # Scripts SQL versionados (V1 … V10)
│   ├── static/                     # css/studyai.css, js/studyai.js
│   └── templates/                  # auth/{login,cadastro} + studyai/{layout,home,flashcards,corretor,roadmap}
└── wiki/                           # Arquitetura, módulos, testes, CI/CD, segurança, convenções
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
| `V1__init.sql` | Tabelas `deck` e `flashcard` (módulo FlashIA) |
| `V2__audit_log.sql` | Tabela `audit_log` para a trilha de auditoria |
| `V5__usuario.sql` | Tabela `usuario` + usuário `admin` inicial |
| `V6__redacao.sql` | Tabelas `redacao` e `redacao_criterio` (módulo CorretorIA) |
| `V7__fix_redacao_types.sql` | Ajusta as colunas de nota para `DOUBLE PRECISION` |
| `V8__add_usuario_to_deck_and_redacao.sql` | Adiciona `usuario_id` para isolar os dados por usuário |
| `V9__add_email_e_oauth_to_usuario.sql` | Adiciona `email` e `provider` (login OAuth2) e libera `password` nulo |
| `V10__roadmap.sql` | Tabelas `roadmap`, `roadmap_semana` e `roadmap_tarefa` (módulo RoadmapIA) |

> ℹ️ Os números `V3` e `V4` não existem: foram consumidos por migrations descartadas
> durante o desenvolvimento. O Flyway não exige numeração contígua.

> ⚠️ **Nunca edite uma migration já aplicada** — o Flyway valida o checksum. Para
> mudar o schema, crie uma nova (`V11__...sql`).

---

## ✅ Testes
```bash
mvn test                  # testes + relatório JaCoCo (target/site/jacoco/index.html)
mvn clean test jacoco:report   # idem, partindo do zero
mvn verify                # testes + cobertura na fase verify
```

A suíte tem **102 testes** e roda **com ou sem Docker**: os testes unitários e de fatia
(Mockito, `@WebMvcTest`) não precisam de banco; o teste de
integração `StudyAiApplicationTests` sobe um PostgreSQL real via **Testcontainers** e é
**automaticamente pulado** quando o Docker não está disponível
(`@Testcontainers(disabledWithoutDocker = true)`).

### Cobertura de testes

**Cobertura de instruções: 89%** (ramos 68%) — **acima do mínimo de 85% exigido**.
Relatório versionado em [`cobertura/jacoco/index.html`](cobertura/jacoco/index.html)
(gerado com JaCoCo; a classe de bootstrap `StudyAiApplication` é excluída da medição).
A cobertura abrange os três módulos de IA, o domínio (`Deck`, `Redacao`, `Roadmap` e
filhos), a autenticação por banco (`UsuarioService`) e a trilha de auditoria.

---

## 📊 Observabilidade (Grafana/OpenTelemetry + Umami)

Duas camadas distintas: o **OpenTelemetry** instrumenta o **backend** (traces, métricas e
logs do servidor) e o **Umami** mede o **frontend** (visitas, páginas, navegador). Os dois
backends são **centrais da disciplina** — a aplicação só aponta para lá.

### OpenTelemetry → Grafana (traces, métricas, logs)

- **Como funciona:** um **agente Java** é anexado à JVM no [`docker/Dockerfile`](docker/Dockerfile)
  (`-javaagent`). Ele instrumenta **sem código** o Spring MVC (cada requisição vira um *trace*),
  o JDBC (cada query PostgreSQL vira um *span*), métricas da JVM e os logs do Logback (enviados
  ao Loki, já correlacionados por `trace_id`/`span_id`).
- **Spans manuais de negócio:** `FlashcardService.gerar`, `CorretorService.avaliar` e
  `RoadmapService.gerar` são anotados com `@WithSpan`, com atributos como `roadmap.semanas`,
  `flashcard.banca` e `*.modo` (`demo`/`real`).
- **Configuração** (no `.env` de produção — o backend é `otel.dsc.rodrigor.com`):
  ```env
  OTEL_SDK_DISABLED=false                                          # OBRIGATÓRIO para ligar
  OTEL_EXPORTER_OTLP_HEADERS=Authorization=Bearer <TOKEN_DA_TURMA> # token vem do Discord (nunca versionar)
  ```
  > O `OTEL_SERVICE_NAME=dsc-eq11` já é o default no compose — é por ele que a equipe se
  > acha no painel compartilhado. Ligar exige **as duas** variáveis: só o token não basta
  > (fica `OTEL_SDK_DISABLED=true` e nada é exportado).
- **Ver no painel:** <https://otel.dsc.rodrigor.com> → **Explore** → **Tempo**, filtrando
  `Service Name = dsc-eq11` (traces) ou **Loki** com `{service_name="dsc-eq11"}` (logs).

### Umami (analytics de frontend)

- **Como funciona:** um `<script>` no [`layout.html`](src/main/resources/templates/studyai/layout.html)
  (e nas telas de login) envia um *pageview* ao servidor do Umami a cada página aberta.
- **Configuração:** os valores da eq11 já são o default no [`docker-compose.yml`](docker-compose.yml)
  (`UMAMI_SRC` e `UMAMI_WEBSITE_ID`). O `website-id` **não é segredo** (fica visível no HTML),
  por isso pode ir versionado. Localmente (`mvn spring-boot:run`) fica desligado, para o
  tráfego de teste não poluir os números.

---

## 📋 Log de Auditoria

Registra as ações de usuário relevantes do sistema, para rastreabilidade.

- **O que é auditado:** as ações reais existentes hoje — **login** bem-sucedido,
  **falha de login**, **logout**, **geração de flashcard** (`POST /flashcards/gerar`),
  **avaliação de redação** (`POST /corretor/avaliar`) e **geração de plano de estudos**
  (`POST /roadmap/gerar`).
- **Onde fica armazenado:** tabela **`audit_log`** (PostgreSQL, criada pela migração
  Flyway `V2__audit_log.sql`). Campos principais: `usuario`, `acao`
  (`LOGIN` / `LOGIN_FALHA` / `LOGOUT` / `GERAR_FLASHCARD` / `AVALIAR_REDACAO` /
  `GERAR_ROADMAP`), `entidade`, `entidade_id`, `detalhes`, `ip` e `data_hora`.
  É um registro *append-only* (apenas inserções).
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
  - `src/main/resources/db/migration/V2__audit_log.sql` (schema)
  - pontos de captura de negócio: `FlashcardController`, `CorretorController` e
    `RoadmapController` (em `src/main/java/br/ufpb/dsc/studyai/controller/`)

---

## 🔌 Integração com Serviço Externo

> O **PostgreSQL** fornecido pela disciplina é infraestrutura básica e **não** conta como
> integração externa. As integrações implementadas são de **IA**, **Autenticação (OAuth2)**
> e **Observabilidade** (OpenTelemetry/Grafana + Umami — detalhadas na seção
> [Observabilidade](#-observabilidade-grafanaopentelemetry--umami)).

### 1. IA (LangChain4j — os três módulos)
- **Qual serviço:** provedores de LLM acessados pelo **LangChain4j** — **Anthropic Claude**,
  **Google Gemini** ou **OpenAI** — além de um **modo demo** embutido, que produz conteúdo
  de exemplo sem chamada externa.
- **Para que é usado:** gerar flashcards, corrigir redações e montar planos de estudo. Cada
  módulo declara uma interface `@AiService` com os prompts em `@SystemMessage`/`@UserMessage`;
  o LangChain4j entrega a resposta já mapeada para um `record` Java, sem parse manual de JSON.
- **Classes participantes:**
  - `service/FlashcardAiService.java`, `service/CorretorAiService.java`,
    `service/RoadmapAiService.java` (interfaces declarativas `@AiService`)
  - `service/FlashcardService.java`, `service/CorretorService.java`,
    `service/RoadmapService.java` (regra de negócio, modo demo e validação da resposta)
  - `config/LangChain4jConfig.java` (escolhe o provedor e monta o `ChatLanguageModel`)
  - `config/IAProperties.java` (configuração e modo demo/real)
- **Como é configurado** (variáveis de ambiente — **sem valores reais no repositório**):

  | Variável | Função |
  |----------|--------|
  | `STUDYAI_AI_MODO` | `demo` (padrão) ou `real` |
  | `STUDYAI_AI_PROVEDOR` | `openai` (padrão), `anthropic` ou `gemini` |
  | `STUDYAI_AI_API_KEY` | chave do provedor (somente em ambiente, nunca versionada) |
  | `STUDYAI_AI_MODELO` | modelo a usar no provedor (precisa ser do mesmo provedor) |

  > ⚠️ **Cuidado:** a app lê `STUDYAI_AI_API_KEY` (não `OPENAI_API_KEY`). Sem essa
  > variável, `isDemo()` volta ao **modo demo em silêncio**, mesmo com `modo=real`.
  > E o modelo precisa bater com o provedor (`openai` → `gpt-*`, `anthropic` → `claude-*`).

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
Detalhes em [`wiki/07_SECURITY.md`](wiki/07_SECURITY.md).

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
  # IA real (opcional) — sem STUDYAI_AI_API_KEY, a app roda em modo demo
  STUDYAI_AI_MODO=real
  STUDYAI_AI_API_KEY=<chave-do-provedor>
  # Observabilidade (opcional) — liga o OpenTelemetry
  OTEL_SDK_DISABLED=false
  OTEL_EXPORTER_OTLP_HEADERS=Authorization=Bearer <token-da-turma>
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

Detalhes em [`wiki/08_CONVENTIONS.md`](wiki/08_CONVENTIONS.md).

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
