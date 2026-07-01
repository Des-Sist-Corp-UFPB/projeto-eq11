# Ideia de Servidor MCP — EQ11

**Domínio:** Geração de material de estudo a partir de textos/leis  
**Data:** 2026-07-01

## O que é

Um **servidor MCP (Model Context Protocol)** expõe as operações do seu sistema como *tools* e *resources* que qualquer assistente de IA (Claude Desktop, Cursor, etc.) pode chamar com segurança. Na prática, é uma camada fina sobre a **API que vocês já têm** — cada tool chama um endpoint/service existente. Assim o projeto deixa de ser só uma tela e passa a ser operável por um agente de IA.

## Servidor proposto: `studyai-mcp`

### Tools sugeridas

- `gerar_flashcards(texto)` — cria flashcards
- `gerar_quiz(texto, n)` — gera questões
- `resumir_lei(referencia)` — resumo estruturado

### Resources (somente leitura)

- biblioteca de materiais gerados como resource

### Exemplos de uso com um LLM

- "Transforme o capítulo 3 desta apostila em 10 flashcards e um quiz."

## Esqueleto para começar (Java / Spring AI)

```java
// pom.xml: org.springframework.ai:spring-ai-starter-mcp-server-webmvc
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class StudyaiTools {

    private final SeuService seuService;   // injete seus services/repositories

    public StudyaiTools(SeuService seuService) { this.seuService = seuService; }

    @Tool(description = "cria flashcards")
    public Object gerar_flashcards(/* params */) {
        return seuService.suaOperacaoExistente();   // reaproveite sua lógica
    }
}
```
> Registre as tools com um `MethodToolCallbackProvider` (bean) apontando para esta classe.

## Boas práticas

- **Segurança:** cada tool que altera dados deve exigir autenticação e registrar no **log de auditoria** (o mesmo do requisito da disciplina).
- **Escopo mínimo:** exponha só o necessário; separe tools de leitura das de escrita.
- **Reaproveite:** as tools devem chamar seus *services*/*controllers* existentes, não reimplementar regra de negócio.

## Referências
- Documentação MCP: https://modelcontextprotocol.io
- SDKs: Python (`mcp`), TypeScript (`@modelcontextprotocol/sdk`), Java (Spring AI MCP Server).

*Sugestão gerada em 2026-07-01 para orientar a integração de LLMs ao projeto.*