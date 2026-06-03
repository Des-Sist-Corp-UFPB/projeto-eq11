# StudyAI BR — Plano de Arquitetura e Desenvolvimento
### Disciplina: Desenvolvimento de Sistemas Corporativos (DSC) — UFPB / Prof. Rodrigo Rebouças
### Base: boilerplate "Sistema Mercado" · Produto-alvo: StudyAI BR (Concursos & ENEM)

---

## 1. Decisão de tecnologia (resumo executivo)

**Manter a stack do professor:** Java 21 + Spring Boot 3.4.5 + Thymeleaf + HTMX + Bootstrap 5.3 + PostgreSQL 16 + Flyway + Spring Security 6.

**Por quê:** o StudyAI, para virar um *sistema corporativo* de verdade, precisa de autenticação, persistência (decks, correções, histórico), medição de uso por plano e um proxy de IA no servidor — exatamente os pontos fortes do Spring. O MVP atual é só a camada de view; tudo que falta cai nas camadas que o boilerplate já oferece. Some-se o alvo de deploy pronto (`dsc.rodrigor.com`) e o pipeline CI/CD + SAST já montados.

**Alternativa honesta:** Next.js 15 + Supabase + Gemini seria mais rápido para um lançamento comercial (feel de SPA, streaming mais simples). Fica como caminho de produção, **não** para a entrega da disciplina.

**Mudança inegociável:** a chamada de IA sai do navegador e vai para um serviço no backend. A chave de API nunca pode estar no front (falha de segurança + CORS + reprovaria no SAST).

---

## 2. O contrato do projeto (requisitos extraídos do README/CLAUDE do professor)

| Exigência | O que significa para o StudyAI |
|-----------|-------------------------------|
| Arquitetura em camadas (`config/controller/domain/dto/repository/service`) | Toda lógica de IA e CRUD respeita essa separação |
| JPA + PostgreSQL + **Flyway** | Cada mudança de schema é uma migration nova (`V2__`, `V3__`...). Nunca editar migration aplicada |
| **Spring Security** (trocar `InMemoryUserDetailsManager` por banco) | Implementar `UserDetailsService` com a entidade `Usuario` |
| DTOs como `records` Java | `FlashcardRequest`, `CorrecaoEnemResponse`, etc. |
| Testes JUnit 5 + Testcontainers | Testes de repositório/serviço sobem Postgres em container |
| CI/CD GitHub Actions + deploy automático | `push` na `main` → testes → SAST → build imagem → deploy |
| SAST (SpotBugs, FindSecBugs, Semgrep, Trivy, OWASP DC) | Código limpo, sem segredo hardcoded, dependências sem CVE |
| Convenções em português + Conventional Commits | Domínio em PT; commits `feat:`, `fix:`, `docs:`... |

> Segredo extra no pipeline: além de `SSH_DEPLOY_KEY` e `NVD_API_KEY`, você vai precisar de um secret para a **chave da API de IA** (ex.: `STUDYAI_AI_API_KEY`).

---

## 3. O que falta no MVP para virar "sistema corporativo"

| Módulo do MVP | Existe hoje | Falta (o trabalho real) |
|---------------|-------------|-------------------------|
| Sidebar/navegação | SPA em JS | Vira layout Thymeleaf (fragmento) + páginas server-side |
| FlashIA | Gera cartões via API no front | Mover IA p/ backend; salvar `Deck`+`Flashcard` |
| CorretorIA (ENEM/Discursiva/OAB) | 3 prompts no front | Backend; salvar `Correcao` (feedback em `jsonb`) |
| PrevêTema | Histórico embarcado no JS | Backend; salvar `Previsao`; histórico vira seed/constante no service |
| Histórico | Só `state` em memória | Consulta paginada por usuário |
| Plano Gratuito / Upgrade | Constantes fictícias no front | `Plano` no `Usuario` + limites impostos no servidor |
| Login | Não existe | Auth em banco (Spring Security) |
| Pagamento (Kiwify) | Botão com `alert()` | Webhook no backend (fase avançada/opcional) |

---

## 4. Arquitetura alvo

### 4.1 Pacotes
```
br.ufpb.dsc.studyai
├── config/        # SecurityConfig, RestClientConfig, propriedades da IA
├── controller/    # Home, Flashcard, Correcao, Previsao, Historico, Auth, (Webhook)
├── domain/        # Usuario, Deck, Flashcard, Correcao, Previsao (+ enums Plano, TipoCorrecao)
├── dto/           # records de request/response
├── exception/     # LimiteExcedidoException, IAIndisponivelException, etc.
├── repository/    # interfaces Spring Data JPA
└── service/       # IAService, FlashcardService, CorrecaoService, PrevisaoService,
                   # LimiteService, UsuarioService
```

### 4.2 Entidades de domínio
- **Usuario** — `id, nome, email (único), senhaHash, plano (GRATUITO|PRO), criadoEm`. Base da autenticação.
- **Deck** — `id, usuario, titulo, banca, disciplina, criadoEm`. 1→N com Flashcard.
- **Flashcard** — `id, deck, frente, verso, ordem`.
- **Correcao** — `id, usuario, tipo (ENEM|DISCURSIVA|OAB), tema, notaTotal, notaMaxima, payload (jsonb), criadoEm`. O feedback completo vai em `jsonb` porque cada tipo tem formato diferente; colunas denormalizadas (`tipo`, `notaTotal`...) servem para consulta/histórico.
- **Previsao** — `id, usuario, prova, ano, foco, payload (jsonb), criadoEm`.

> JSON no Postgres: coluna `jsonb` na migration; no Hibernate 6 (Boot 3.4) mapeie com `@JdbcTypeCode(SqlTypes.JSON)` no campo `String payload` (ou um tipo próprio).

### 4.3 DTOs (records)
- `FlashcardRequest(String banca, String disciplina, int quantidade, String texto)`
- `FlashcardDTO(String frente, String verso)`
- `CorrecaoEnemRequest(String tema, String texto)`
- `CorrecaoDiscursivaRequest(String banca, String area, String enunciado, String texto, int pontos)`
- `CorrecaoOabRequest(String area, String tipo, String enunciado, String texto)`
- `PrevisaoRequest(String prova, int ano, String foco)`
- Responses espelham o JSON dos prompts do MVP (ex.: `competencias.c1.nota`, `nota_total`, etc.).

### 4.4 Repositórios
`UsuarioRepository`, `DeckRepository`, `FlashcardRepository`, `CorrecaoRepository`, `PrevisaoRepository` — todos `extends JpaRepository`. Métodos como `findByUsuarioOrderByCriadoEmDesc(...)`, `countByUsuarioAndTipo(...)` para limites e histórico.

### 4.5 Serviços (o coração está no IAService)
- **IAService** (interface) — `String completar(String system, String user)`. Implementação (`AnthropicIAService` *ou* `GeminiIAService`) escolhida por propriedade. Guarda a chave via env var, monta a requisição com `RestClient`, devolve o texto. **É aqui que a chave fica segura e a lógica do `callClaude` do MVP é migrada.**
- **PromptFactory** (opcional) — concentra os prompts (hoje espalhados no JS). Bom para testes.
- **FlashcardService** — checa limite → chama IAService → faz parse robusto do JSON → persiste `Deck`+`Flashcard` → devolve DTOs.
- **CorrecaoService** — idem para os 3 tipos; salva `Correcao` com `payload` jsonb.
- **PrevisaoService** — idem; histórico ENEM como constante/seed.
- **LimiteService** — dado o `Usuario`, conta registros do ciclo e diz se pode gerar (lança `LimiteExcedidoException` se estourar). Fonte única de verdade no servidor.
- **UsuarioService** — implementa `UserDetailsService`; cadastro com BCrypt.

### 4.6 Controllers + rotas HTMX
| Rota | Método | Retorna |
|------|--------|---------|
| `/` | GET | `home.html` (dashboard com stats reais) |
| `/flashcards` | GET | `flashcards.html` |
| `/flashcards/gerar` | POST | fragmento `fragments/flashcard-result :: cards` |
| `/corrigir` | GET | `corrigir.html` |
| `/corrigir/enem` | POST | fragmento resultado ENEM |
| `/corrigir/discursiva` | POST | fragmento resultado discursiva |
| `/corrigir/oab` | POST | fragmento resultado OAB |
| `/previsao` | GET | `previsao.html` |
| `/previsao/gerar` | POST | fragmento resultado previsão |
| `/historico` | GET | `historico.html` |
| `/login`, `/cadastro` | GET/POST | Spring Security + controller de cadastro |
| `/webhooks/kiwify` | POST | (fase avançada) confirma pagamento → vira PRO |

### 4.7 Templates Thymeleaf
- `layout.html` — fragmento com sidebar + shell `main` (porta o CSS do MVP).
- Páginas: `home`, `flashcards`, `corrigir`, `previsao`, `historico`, `login`, `cadastro`.
- Fragmentos (devolvidos pelos POSTs HTMX): `fragments/flashcard-result`, `fragments/correcao-enem`, `fragments/correcao-discursiva`, `fragments/correcao-oab`, `fragments/previsao-result`.
- JS client-side (estático): flip do cartão, navegação do carrossel, troca de abas, contador de palavras. **Não** precisa de servidor.

### 4.8 Migrations Flyway
```
V1__init.sql              (já existe — pode adaptar/remover Produto)
V2__usuario.sql
V3__deck_flashcard.sql
V4__correcao.sql
V5__previsao.sql
V6__seed_admin.sql        (usuário inicial p/ testes)
```

### 4.9 Segurança
- `SecurityFilterChain`: liberar `/login`, `/cadastro`, estáticos e `/webhooks/**` (com validação de assinatura); proteger o resto.
- `UsuarioService implements UserDetailsService`; senha com `BCryptPasswordEncoder`.
- CSRF: HTMX precisa enviar o token (configurar `hx-headers` ou meta tag + JS).
- Nenhum segredo no código: chave de IA, credenciais de banco — tudo via env/secret.

### 4.10 Configuração da IA
```yaml
studyai:
  ia:
    provedor: ${STUDYAI_AI_PROVEDOR:anthropic}   # anthropic | gemini
    api-key: ${STUDYAI_AI_API_KEY}               # NUNCA versionar
    modelo: ${STUDYAI_AI_MODELO:...}             # use um modelo atual do provedor
    timeout-segundos: 45
```
- `RestClient` com timeout generoso (chamadas de LLM levam 5–30s).
- Parse de JSON robusto: remover cercas ```` ```json ````, validar com Jackson, tratar falha com mensagem amigável.

---

## 5. Mapeamento MVP → Spring (módulo a módulo)

**FlashIA.** O `gerarFlashcards()` do JS vira: form com `hx-post="/flashcards/gerar"` → `FlashcardController` → `LimiteService.verificar()` → `IAService.completar(system,user)` (prompt migrado) → parse → salva `Deck`+`Flashcard` → devolve fragmento com os cartões. O flip e o carrossel continuam em JS no fragmento.

**CorretorIA.** As 3 funções (`corrigirENEM`, `corrigirDiscursiva`, `corrigirOAB`) viram 3 endpoints. Os prompts e os formatos JSON de resposta (ex.: 5 competências 0–200, total 1000) são preservados; o resultado estruturado é salvo em `Correcao.payload` (jsonb) e renderizado num fragmento.

**PrevêTema.** O histórico ENEM 2010–2025 que está no JS vira constante no `PrevisaoService` (ou tabela seed). O prompt é o mesmo; resultado salvo em `Previsao`.

**Histórico.** Substitui o `state.historico`/`state.decks` por consultas paginadas (`findByUsuario...`).

**Home.** As stats fictícias viram contagens reais via `LimiteService`/repositórios.

**Planos/limites.** `Plano` (GRATUITO|PRO) no `Usuario`. Limites do MVP (30 flashcards, 2 correções, 1 previsão) impostos no `LimiteService` contando registros. Estourou → fragmento de "faça upgrade".

---

## 6. Padrão de interação HTMX (exemplo concreto — FlashIA)

**Template (form):**
```html
<form hx-post="/flashcards/gerar"
      hx-target="#flash-resultado"
      hx-swap="innerHTML"
      hx-indicator="#flash-loading">
  <!-- banca, disciplina, quantidade, textarea -->
  <button type="submit" class="btn btn-primary">Gerar Flashcards com IA</button>
</form>
<div id="flash-loading" class="htmx-indicator"><!-- spinner --></div>
<div id="flash-resultado"><!-- fragmento entra aqui --></div>
```

**Controller:**
```java
@PostMapping("/flashcards/gerar")
public String gerar(@AuthenticationPrincipal UserDetails user,
                    FlashcardRequest req, Model model) {
    Deck deck = flashcardService.gerar(user, req);   // limite + IA + persistência
    model.addAttribute("deck", deck);
    return "fragments/flashcard-result :: cards";
}
```

Resultado: experiência fluida, sem recarregar a página, 100% dentro do paradigma server-side que a disciplina valoriza.

---

## 7. Roadmap por fases

**Fase 0 — Scaffolding (1 sessão).** Clonar o boilerplate, renomear pacote para `studyai`, subir banco + app via Docker, confirmar login admin. Commit `chore: scaffold studyai`.

**Fase 1 — Domínio + Auth em banco.** Criar `Usuario` + migrations; trocar `InMemoryUserDetailsManager` por `UsuarioService`; telas de login/cadastro. *Entrega:* dá para registrar e logar.

**Fase 2 — IAService + FlashIA ponta a ponta.** `IAService` com chave em env; `Deck`/`Flashcard`; gerar e salvar; fragmento HTMX com flip. *Entrega:* primeiro módulo de IA funcionando seguro.

**Fase 3 — CorretorIA (3 modos).** `Correcao` com `jsonb`; 3 endpoints; renderização dos resultados. *Entrega:* correção ENEM/Discursiva/OAB.

**Fase 4 — PrevêTema + Histórico.** `Previsao` + seed do histórico; página de histórico paginada. *Entrega:* app funcionalmente completo.

**Fase 5 — Planos e limites server-side.** `LimiteService`; bloqueio + tela de upgrade; stats reais na home.

**Fase 6 — Pagamentos (opcional/avançado).** Webhook Kiwify que promove o usuário a PRO. *Pode ficar como stub se o prazo apertar.*

**Fase 7 — Testes, SAST e deploy.** Testes de serviço/repositório (Testcontainers); rodar `mvn verify -Psecurity`; configurar secrets no GitHub (incl. chave de IA); deploy na `main`.

---

## 8. Decisões em aberto (você define)

1. **Provedor de IA.** Para um projeto de faculdade, **Gemini Flash** tem free tier generoso (custo ~zero); Anthropic também funciona bem. O `IAService` é agnóstico — dá para começar com um e trocar depois.
2. **Modelo de limites.** Contar registros persistidos (recomendado, fonte única) vs. tabela de contador dedicada.
3. **Pagamento entra ou vira stub?** Se a nota não exige cobrança real, deixe Kiwify como fase opcional e foque em auth + limites (que já demonstram "sistema corporativo").

---

## 9. CLAUDE.md adaptado (rascunho para colar no seu repositório)

```markdown
# Memória do Projeto — StudyAI BR (DSC/UFPB)

## Identidade
- Nome: StudyAI BR — plataforma de estudos para Concursos & ENEM
- Disciplina: Desenvolvimento de Sistemas Corporativos / Prof. Rodrigo Rebouças
- Base: boilerplate "Sistema Mercado"

## Stack
Java 21 · Spring Boot 3.4.5 · Thymeleaf + HTMX · Bootstrap 5.3 ·
PostgreSQL 16 · Flyway · Spring Security 6 · Maven · JUnit 5 + Testcontainers

## Pacote raiz
br.ufpb.dsc.studyai (config, controller, domain, dto, exception, repository, service)

## Módulos de negócio
- FlashIA: gera flashcards de um texto (Deck + Flashcard)
- CorretorIA: corrige redação ENEM (5 competências/1000), discursiva e peça OAB (Correcao, payload jsonb)
- PrevêTema: projeta temas prováveis a partir de histórico (Previsao)
- Histórico e Planos (GRATUITO/PRO) com limites impostos no servidor

## Regra de ouro
A chamada de IA é SEMPRE no backend (IAService). A chave fica em variável de
ambiente (STUDYAI_AI_API_KEY). Nunca expor segredo no front nem versionar chave.

## Convenções
Domínio em português · DTOs como records · @Transactional(readOnly=true) em consultas ·
Migrations Flyway versionadas (nunca editar aplicadas) · Conventional Commits

## IA
- Provedor configurável (anthropic|gemini) via propriedade studyai.ia.provedor
- Parse de JSON robusto (remover cercas markdown, validar com Jackson)
- Timeout generoso (chamadas de LLM levam 5–30s)
```
