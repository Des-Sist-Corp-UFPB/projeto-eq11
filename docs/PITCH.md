# StudyAI BR — Pitch (Equipe eq11 · DSC/UFPB)

> **Plataforma de estudos com IA para Concursos e ENEM.**
> Transforma qualquer texto em material de memorização e correção — com a IA rodando
> de forma segura no backend, dentro de um sistema corporativo de verdade.

---

## 1. O problema
Quem estuda para concurso e ENEM se afoga em PDFs, leis e resumos, mas perde tempo
transformando isso em **estudo ativo** (flashcards, correção de redação, previsão de temas).
As ferramentas que existem ou são genéricas, ou expõem a chave de IA no navegador
(inseguro e caro), ou não guardam o histórico do aluno.

## 2. A solução
O **StudyAI** concentra três módulos de IA voltados ao contexto brasileiro:

| Módulo | O que faz | Status |
|--------|-----------|--------|
| **FlashIA** | Gera flashcards (pergunta/resposta) a partir de um texto | ✅ **Funcionando** |
| **CorretorIA** | Corrige redação ENEM (5 competências), discursiva e peça OAB | 🔜 Roadmap |
| **PrevêTema** | Projeta os temas mais prováveis da próxima prova | 🔜 Roadmap |

A chamada de IA **nunca** sai do navegador: o servidor é um proxy seguro que guarda a
chave em variável de ambiente e persiste os resultados (decks, cartões, histórico).

## 3. O que já dá para demonstrar hoje (FlashIA, ponta a ponta)
1. Login seguro (Spring Security) → **admin / admin123**.
2. Cola um resumo/lei → escolhe banca, disciplina e quantidade → **"Gerar Flashcards com IA"**.
3. Os cartões aparecem **sem recarregar a página** (HTMX), com **flip** e **carrossel**.
4. O deck é **salvo no banco** e as estatísticas da home refletem o uso real.

> **Modo demo embutido:** o FlashIA funciona **sem internet e sem chave de API**
> (`studyai.ia.modo=demo`), então a demonstração nunca depende de rede no dia do pitch.
> Com uma chave configurada, o mesmo fluxo chama o provedor real (Anthropic ou Gemini).

## 4. Por que é um "sistema corporativo" (e não só uma telinha)
- **Arquitetura em camadas:** `controller → service → repository → domain`, DTOs como `records`.
- **IA isolada atrás de uma interface** (`IAService`): troca de provedor sem mexer na regra de negócio.
- **Persistência versionada:** PostgreSQL + **Flyway** (cada mudança de schema é uma migration).
- **Segurança real:** Spring Security, CSRF ativo (inclusive nos POSTs HTMX), nenhum segredo no código.
- **Parsing robusto da IA:** remove cercas markdown, valida com Jackson, trata erro com mensagem amigável.

## 5. Stack
**Java 21 · Spring Boot 3.4.5 · Thymeleaf + HTMX · PostgreSQL 16 · Flyway ·
Spring Security 6 · Docker · GitHub Actions (CI/CD + SAST).**
Identidade visual própria (tema escuro, fontes Syne + DM Sans).

## 6. Arquitetura em uma figura
```
Navegador (HTMX)  ──POST /flashcards/gerar──►  FlashcardController
                                                    │
                                            FlashcardService  ──►  IAService
                                                    │              (demo | Anthropic | Gemini)
                                       Deck + Flashcard (JPA)
                                                    │
                                              PostgreSQL (Flyway)
```

## 7. Engenharia / DevOps
- **Containerização única:** um `docker-compose.yml` sobe banco + aplicação.
  App exposta em **`http://127.0.0.1:8111`** (porta padronizada da equipe).
- **CI/CD:** push na `main` → build da imagem → publicação no GHCR → deploy no servidor.
- **Segurança no pipeline:** SpotBugs + FindSecBugs, OWASP Dependency-Check, Semgrep e Trivy.

## 8. Como rodar / demonstrar
```bash
# Sobe tudo (banco + aplicação) na porta da equipe
docker compose up -d --build
# Acesse:
http://127.0.0.1:8111      # login: admin / admin123
```
Alternativa para desenvolvimento local: `docker compose up -d postgres` + `mvn spring-boot:run`.

## 9. Roadmap
- **Agora:** FlashIA completo (gerar, salvar, revisar) em modo demo e real.
- **Próximo:** CorretorIA (3 modos) e PrevêTema, reaproveitando o mesmo `IAService`.
- **Depois:** autenticação por banco (cadastro de alunos), planos GRATUITO/PRO com limites,
  histórico paginado e pagamento (webhook).

## 10. Mensagem-chave
> Já temos um **módulo de IA funcionando ponta a ponta** dentro de uma base
> **corporativa, segura e pronta para deploy** — e um caminho claro para os próximos
> dois módulos sem reescrever nada.
