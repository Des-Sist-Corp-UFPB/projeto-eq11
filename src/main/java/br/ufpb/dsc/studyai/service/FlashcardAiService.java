package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.dto.FlashcardDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface FlashcardAiService {

    @SystemMessage("Você é um professor carrasco e especialista em concursos públicos de alto nível no Brasil. Você conhece profundamente o estilo de cobrança da banca {{banca}} na disciplina de {{disciplina}}. Sua missão é criar flashcards difíceis, focados no que realmente reprova os candidatos: pegadinhas, exceções, jurisprudência e distinções sutis.")
    @UserMessage("""
            Gere exatamente {{quantidade}} flashcards de ALTO NÍVEL a partir do texto abaixo.
            
            Regras rigorosas para a FRENTE (Pergunta):
            - Proibido perguntas básicas de decoreba (ex: "o que é X?").
            - Elabore perguntas analíticas, comparativas ou baseadas em pequenos casos concretos/resumos (ex: "Considerando a regra X, qual a principal exceção aplicada pela banca no caso Y?").
            - Você pode incluir um breve resumo de contexto antes da pergunta principal na frente do cartão.
            - Foque nas minúcias, exceções à regra, prazos confusos e detalhes críticos.
            
            Regras rigorosas para o VERSO (Resposta):
            - A resposta deve ser direta, matadora e ir direto ao ponto (máximo 3-4 linhas).
            - Destaque a "palavra-chave" ou o cerne da resposta.
            - Sempre que aplicável, inclua o embasamento (artigo de lei, súmula) ou um mnemônico rápido.
            
            TEXTO:
            {{texto}}
            """)
    br.ufpb.dsc.studyai.dto.FlashcardResponse gerarFlashcards(
            @V("banca") String banca,
            @V("disciplina") String disciplina,
            @V("quantidade") int quantidade,
            @V("texto") String texto);
}
