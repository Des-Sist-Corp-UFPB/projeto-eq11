package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.dto.CorretorResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface CorretorAiService {

    @SystemMessage("Você é um professor avaliador rigoroso e especialista na banca {{banca}}. Sua missão é corrigir a redação do aluno sobre o tema fornecido e retornar uma avaliação detalhada em formato JSON.")
    @UserMessage("""
            Corrija a redação abaixo simulando rigorosamente os critérios da banca {{banca}}.
            
            Tema: {{tema}}
            
            Redação do aluno:
            {{texto}}
            
            Regras rigorosas para a correção, DE ACORDO COM A BANCA ESCOLHIDA ({{banca}}):
            
            SE A BANCA FOR "ENEM":
            Avalie usando EXATAMENTE estas 5 competências (máx 200 pontos cada, Total: 1000):
            1. Domínio da norma culta (0-200): Gramática, ortografia e vocabulário.
            2. Compreensão do tema e tipologia (0-200): Adequação ao tema e texto dissertativo-argumentativo com repertório.
            3. Organização de argumentos (0-200): Coerência e projeto de texto lógico.
            4. Coesão (0-200): Uso adequado de conectivos entre e dentro dos parágrafos.
            5. Proposta de intervenção (0-200): Ação, agente, modo, efeito e detalhamento.
            
            SE A BANCA FOR "VUNESP":
            Não exija proposta de intervenção. Avalie nestes 3 critérios (Total: 100):
            1. Tema (0-33): Avalia se o texto aborda o tema de forma integral, sem fugir da proposta.
            2. Estrutura (0-33): Avalia o gênero dissertativo-argumentativo e a progressão coerente das ideias.
            3. Expressão (0-34): Domínio da norma-padrão da língua portuguesa e coesão textual.
            
            SE A BANCA FOR "CEBRASPE" (ou "CESPE"):
            Avalie a objetividade e resposta direta aos tópicos. Avalie nestes 2 critérios (Total: 100):
            1. Apresentação e Estrutura (0-5): Legibilidade, respeito às margens e paragrafação.
            2. Desenvolvimento do Conteúdo e Gramática (0-95): Domínio do tema, objetividade nos argumentos e desconto rigoroso de erros gramaticais (ortografia, morfossintaxe).
            
            SE FOR OUTRA BANCA (Genérica):
            Avalie nestes 3 critérios (Total: 100):
            1. Adequação ao Tema (0-30).
            2. Estrutura e Argumentação (0-40).
            3. Gramática e Coesão (0-30).
            
            Formato Exigido de Saída:
            - notaTotal: Soma de todos os critérios aplicados (Double).
            - comentarioGeral: Feedback resumido focando nas exigências da banca selecionada.
            - criterios: Uma lista contendo os critérios da respectiva banca avaliada. Cada critério deve ter o 'nome', a 'nota' (dentro do limite) e o 'comentario' justificando.
            
            Seu retorno deve ser ESTRITAMENTE o JSON correspondente ao DTO CorretorResponse. Não inclua Markdown, blocos de código (```json) ou textos fora do JSON.
            """)
    CorretorResponse avaliarRedacao(
            @V("banca") String banca,
            @V("tema") String tema,
            @V("texto") String texto);
}
