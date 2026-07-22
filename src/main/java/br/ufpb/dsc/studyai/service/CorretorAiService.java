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
            Corrija a redação abaixo de acordo com os critérios da banca {{banca}}.
            
            Tema: {{tema}}
            
            Redação do aluno:
            {{texto}}
            
            Regras para a correção:
            1. Atribua uma nota final (notaTotal) entre 0 e 1000 (ou o padrão da banca adaptado para a escala 0 a 10). Considere o padrão do ENEM se a banca for ENEM.
            2. Forneça um comentário geral sobre a redação.
            3. Avalie a redação em critérios específicos da banca (ex: Competência 1 a 5 para ENEM, ou Gramática/Coesão/Tema para outras bancas).
            4. Para cada critério, forneça o nome do critério, a nota (nota) e um breve comentário justificando a nota.
            
            Seu retorno deve ser estritamente no formato JSON correspondente ao DTO CorretorResponse.
            """)
    CorretorResponse avaliarRedacao(
            @V("banca") String banca,
            @V("tema") String tema,
            @V("texto") String texto);
}
