# StudyAI BR — Briefing para o Pitch (entrega 1)

> Objetivo de amanhã: app Spring Boot rodando, seguindo a arquitetura do boilerplate,
> com a cara do StudyAI e o módulo **FlashIA** funcionando de ponta a ponta.
> **Fora de escopo hoje:** gateway de pagamento e refatoração de autenticação.

---

## 0. Antes de tudo (2 minutos)

1. Abra o repositório base do professor no VS Code (é o seu *workspace*).
2. Copie para dentro do repo, numa pasta `docs/`, os dois arquivos de referência:
   - `studyai_br_mvp.html`
   - `studyai_arquitetura_dsc.md`
3. **Faça um commit limpo agora** (`git add -A && git commit -m "chore: baseline antes do studyai"`).
   Isso te dá um ponto de retorno se algo der errado à noite.
4. Confirme que o boilerplate sobe: `docker compose -f docker/docker-compose.dev.yml up postgres` + `mvn spring-boot:run`. Se já sobe, ótimo — não mexa no que funciona.

---

## 1. Prompt para colar no Claude Code

> Cole o texto abaixo no Claude Code (VS Code), com o repositório base aberto.

```
Contexto: este repositório é o boilerplate "Sistema Mercado" da disciplina DSC/UFPB
(Spring Boot 3.4.5, Java 21, Thymeleaf + HTMX, PostgreSQL, Flyway, Spring Security).
Em docs/ há dois arquivos de referência: studyai_br_mvp.html (o MVP visual do produto)
e studyai_arquitetura_dsc.md (a arquitetura alvo completa).

Vou apresentar um PITCH amanhã. NÃO implemente a arquitetura inteira do .md — implemente
APENAS a fatia apresentável descrita abaixo. Trabalhe de forma incremental e, ao final,
me diga exatamente como rodar.

ESCOPO (só isto):
1. Layout Thymeleaf portando a identidade visual do studyai_br_mvp.html: sidebar escura,
   fontes Syne + DM Sans, paleta e estilo do MVP. Crie um fragmento layout.html reutilizável.
2. Página inicial (home) com os 3 cards de módulo: FlashIA (ativo), CorretorIA e PrevêTema
   marcados como "Em breve". Use as stats reais de flashcards/decks quando existirem.
3. Domínio: Deck (id, titulo, banca, disciplina, criadoEm) com relação 1->N para
   Flashcard (id, frente, verso, ordem). NÃO crie entidade de usuário agora; os decks
   ficam sem dono nesta entrega.
4. UMA migration Flyway nova (ex.: V2__deck_flashcard.sql) criando essas tabelas.
   Não edite migrations já aplicadas.
5. Repositórios Spring Data: DeckRepository, FlashcardRepository.
6. IAService como INTERFACE (completar(String system, String user)) + uma implementação
   com MODO DEMO obrigatório:
   - Se a propriedade studyai.ia.modo=demo OU a chave studyai.ia.api-key estiver vazia,
     retorne flashcards de EXEMPLO imediatamente (sem chamada externa), para a demo
     funcionar sem internet/chave.
   - Caso contrário, faça a chamada real ao provedor configurado via RestClient,
     com timeout de 45s. Provedor, chave e modelo vêm de variáveis de ambiente.
   - Faça parse de JSON robusto (remova cercas ```json, valide com Jackson, trate erro
     com mensagem amigável).
7. FlashcardService.gerar(request): chama o IAService, faz parse, persiste Deck+Flashcard,
   devolve os cartões. (Sem checagem de limite de plano nesta entrega.)
8. FlashcardController: GET /flashcards (página) e POST /flashcards/gerar que devolve um
   FRAGMENTO Thymeleaf com os cartões, trocado via HTMX (hx-post + hx-target + hx-indicator).
   O flip do cartão e a navegação do carrossel ficam em JS client-side (sem servidor).
9. application.yml: studyai.ia.modo=demo por padrão; provedor/chave/modelo lidos de env
   (STUDYAI_AI_PROVEDOR, STUDYAI_AI_API_KEY, STUDYAI_AI_MODELO).
10. MANTENHA o Spring Security existente do boilerplate (login admin/admin123). Apenas
    libere os recursos estáticos. NÃO refatore autenticação. Garanta que o CSRF do HTMX
    funcione (token via header ou meta tag).

RESTRIÇÕES:
- Respeite o pacote base e as convenções do boilerplate (domínio em português, DTOs como
  records, @Transactional(readOnly=true) em consultas, Conventional Commits).
- O app DEVE compilar e subir com `mvn spring-boot:run` + Postgres do docker-compose,
  e a demo do FlashIA DEVE funcionar com modo demo (sem nenhuma chave de API).
- Você pode manter o exemplo Produto existente, desde que não impeça o boot.

Comece criando a migration e as entidades, depois service/controller, depois os templates.
Pare e me mostre como rodar e testar o FlashIA ao final.
```

---

## 2. Higiene durante a noite (com o Claude Code)

- Deixe o Claude Code trabalhar em **pedaços** (entidades → migration → service → controller → templates) e **rode após cada pedaço**. É mais fácil achar o que quebrou.
- Se um arquivo específico der erro de compilação (o `IAService` é o candidato mais provável), copie o erro e o arquivo — dá para corrigir aquele arquivo isolado rapidamente.
- Commits pequenos: `feat: dominio deck/flashcard`, `feat: IAService com modo demo`, `feat: FlashIA via HTMX`.

---

## 3. Modo demo — sua rede de segurança no palco

A apresentação acontece com `studyai.ia.modo=demo` (padrão). Nesse modo o FlashIA devolve
cartões de exemplo na hora, sem internet. Vantagens:

- A demo **nunca trava** por causa de rede/chave/crédito.
- É **instantânea** (sem os 5–30s da IA real).

Se quiser provar que é IA de verdade durante o ensaio, suba uma vez com a chave real:
```
export STUDYAI_AI_MODO=real
export STUDYAI_AI_PROVEDOR=gemini        # ou anthropic
export STUDYAI_AI_API_KEY=...sua_chave...
export STUDYAI_AI_MODELO=...modelo_atual...
mvn spring-boot:run
```
Mas **apresente no modo demo** (ou deixe pronto para alternar). Nunca dependa de uma
chamada externa ao vivo num pitch.

---

## 4. Checklist pré-pitch (faça hoje à noite, não amanhã de manhã)

- [ ] `git status` limpo; commit de baseline feito antes das mudanças.
- [ ] `docker compose ... up postgres` sobe e fica "healthy".
- [ ] `mvn spring-boot:run` sobe sem erro.
- [ ] Login `admin/admin123` funciona.
- [ ] Home mostra os 3 cards (FlashIA ativo; outros "Em breve").
- [ ] FlashIA: colar um texto → aparecem os cartões → flip funciona → navegação funciona.
- [ ] Tudo isso com modo demo (sem chave).
- [ ] **Ensaio cronometrado** do roteiro de fala (abaixo) — 1 vez inteiro.
- [ ] Plano B testado (seção 6).

---

## 5. Roteiro de fala sugerido (2–4 min)

1. **Problema** (20s): jovens de cidades menores estudando para concursos/ENEM sem mentoria — material existe, mas falta direcionamento e feedback.
2. **Solução** (30s): StudyAI — 3 módulos de IA: FlashIA (memorização ativa), CorretorIA (redação ENEM/discursiva/OAB) e PrevêTema (previsão de temas).
3. **Demo ao vivo** (60–90s): mostre o FlashIA gerando cartões e o flip. É o "uau".
4. **Arquitetura** (30s): mostre a estrutura em camadas no VS Code (domain/service/controller), a migration Flyway, o IAService no backend — reforça que é "sistema corporativo", não um script. Mencione que a chave de IA fica no servidor (segurança).
5. **Roadmap** (20s): aponte para o `.md` — CorretorIA e PrevêTema completos, autenticação em banco, planos/limites e CI/CD com deploy automático. Mostra visão sem prometer o que não está pronto.

---

## 6. Plano B (se algo não subir na hora)

- Tenha um **vídeo de tela de 60s** gravado hoje à noite com o FlashIA funcionando (modo demo). Se a máquina falhar no palco, você passa o vídeo e segue a fala.
- Tenha o **`studyai_arquitetura_dsc.md` aberto** numa aba: mesmo sem demo, dá para apresentar a arquitetura e a visão com solidez.
```
