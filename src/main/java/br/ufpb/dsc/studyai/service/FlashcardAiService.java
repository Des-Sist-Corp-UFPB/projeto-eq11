package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.dto.FlashcardDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface FlashcardAiService {

    @SystemMessage("Você é um professor especialista em concursos públicos brasileiros e especialmente na banca {{banca}}, com foco em {{disciplina}}. Você cria flashcards objetivos e eficientes para memorização ativa.")
    @UserMessage("""
            Gere exatamente {{quantidade}} flashcards a partir do texto abaixo.
            Regras:
            - A "frente" deve ser uma pergunta direta e objetiva
            - O "verso" deve conter a resposta completa mas concisa (max 2-3 linhas)
            - Priorize conceitos cobrados em provas: definições, prazos, princípios, artigos de lei, competências
            - Se houver dispositivo legal, inclua o número no verso
            - Linguagem simples e direta, sem jargão desnecessário
            - Varie os tipos de pergunta (o que é, qual, quem, quando, como)

            TEXTO:
            {{texto}}
            """)
    List<FlashcardDTO> gerarFlashcards(
            @V("banca") String banca,
            @V("disciplina") String disciplina,
            @V("quantidade") int quantidade,
            @V("texto") String texto);
}
