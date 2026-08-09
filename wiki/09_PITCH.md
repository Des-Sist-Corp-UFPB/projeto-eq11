# 09. Pitch do Produto (StudyAI BR)

Esse documento contém os "Bullet points" para ser utilizado nas apresentações em sala de aula (Pitch) da disciplina de DSC.

## 1. O Problema
Quem estuda para concurso e ENEM se afoga em PDFs, leis e resumos longos, e perde muito tempo valioso tentando transformar isso em **estudo ativo** (flashcards, testes, redações e previsões). As ferramentas atuais no mercado, quando usam IA, ou entregam resultados muito genéricos (por usarem *prompts* fracos), ou expõem a chave da IA de forma não-segura pelo navegador do cliente, ou cobram fortunas de assinatura.

## 2. A Solução
O **StudyAI** centraliza a dor dos estudantes em módulos construídos com inteligência artificial aplicada ao contexto brasileiro:
- **FlashIA**: Lê resumos/leis e vomita material pronto para retenção.
- **CorretorIA**: Uma banca virtual e rigorosa (ENEM, OAB, Cebraspe) disposta a corrigir e avaliar qualquer dissertação a qualquer momento do dia, apontando furos e elogiando gramática.

**E o grande trunfo:** O nosso Frontend HTMX e as chamadas de IA estão separadas. A chave do LLM não fica na mão do aluno (navegador). Fica no nosso Backend blindado pelo Spring Security e pelas variáveis restritas.

## 3. Escalabilidade Corporativa (Não é só uma tela bonitinha)
- Não somos um simples protótipo ou MVP "hackeado".
- O Backend está dividido através do consagrado padrão Layered Architecture + MVC (`Controller -> Service -> Repository -> Domain`).
- O Modelo da IA é plugável. Usamos interfaces e adaptadores (Padrão Strategy). Se amanhã o Claude da Anthropic for derrubado ou o Gemini do Google ficar caro, a troca é uma única linha de código.
- As mudanças nas tabelas do PostgreSQL são controladas cirurgicamente por migrations (`Flyway`).
- Possuímos pipeline completo contínuo (Deploy e Integração baseadas no GitHub Actions) blindados por SAST, Semgrep, Trivy para prevenir brechas e um Coverage robusto testado automatizadamente (Testcontainers & JaCoCo).

## 4. Como Vender / Demonstrar
No dia da apresentação:
1. Abra o serviço, já conte que a senha administrativa é blindada com BCrypt, mas digite `admin` e entre.
2. Mostre o **FlashIA**: Cole o Artigo 5º da Constituição Federal no textarea, selecione 15 flashcards e dê o gerar. Mostre que não ocorreu Load na Página inteira, agradeça à maravilha técnica chamada HTMX.
3. Mostre o **CorretorIA**: Cole um texto falho proposital. Peça a banca ENEM e gere a correção. Prove que a IA dividiu perfeitamente o Payload JSONB nas 5 competências do MEC.
