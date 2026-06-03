package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Deck;
import br.ufpb.dsc.mercado.domain.Flashcard;
import br.ufpb.dsc.mercado.dto.FlashcardDTO;
import br.ufpb.dsc.mercado.dto.FlashcardRequest;
import br.ufpb.dsc.mercado.exception.IAIndisponivelException;
import br.ufpb.dsc.mercado.repository.DeckRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço de negócio do módulo <strong>FlashIA</strong>.
 *
 * <p>Orquestra a geração de flashcards: monta os prompts, chama o {@link IAService},
 * faz o parse robusto do JSON, persiste o {@link Deck} com seus {@link Flashcard}
 * e devolve os cartões.
 *
 * <p>Nesta entrega não há checagem de limite de plano (isso virá com o módulo de
 * usuários/planos).
 *
 * @author DSC - UFPB Campus IV
 */
@Service
@Transactional(readOnly = true)
public class FlashcardService {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final IAService iaService;
    private final DeckRepository deckRepository;
    private final ObjectMapper objectMapper;

    public FlashcardService(IAService iaService, DeckRepository deckRepository, ObjectMapper objectMapper) {
        this.iaService = iaService;
        this.deckRepository = deckRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Gera flashcards a partir de um texto, persiste o deck e devolve a entidade salva.
     *
     * @param request dados do formulário (banca, disciplina, quantidade, texto)
     * @return deck persistido, já com os flashcards na ordem correta
     * @throws IAIndisponivelException se a IA falhar ou devolver um formato inválido
     */
    @Transactional
    public Deck gerar(FlashcardRequest request) {
        String banca = StringUtils.hasText(request.banca()) ? request.banca().trim() : "Geral";
        String disciplina = request.disciplina() == null ? "" : request.disciplina().trim();
        int quantidade = request.quantidade() <= 0 ? 15 : Math.min(request.quantidade(), 30);

        String system = montarSystem(banca, disciplina);
        String user = montarUser(quantidade, request.texto());

        String bruto = iaService.completar(system, user);
        List<FlashcardDTO> cartoes = parsearFlashcards(bruto);

        Deck deck = new Deck(montarTitulo(banca, disciplina), banca, disciplina);
        int ordem = 0;
        for (FlashcardDTO dto : cartoes) {
            deck.adicionarFlashcard(new Flashcard(dto.frente(), dto.verso(), ordem++));
        }
        return deckRepository.save(deck);
    }

    // =========================================================================
    // PROMPTS (migrados do MVP em JS)
    // =========================================================================

    private String montarSystem(String banca, String disciplina) {
        StringBuilder sb = new StringBuilder("Você é um professor especialista em concursos públicos brasileiros");
        if (!"Geral".equalsIgnoreCase(banca)) {
            sb.append(" e especialmente na banca ").append(banca);
        }
        if (StringUtils.hasText(disciplina)) {
            sb.append(", com foco em ").append(disciplina);
        }
        sb.append(". Você cria flashcards objetivos e eficientes para memorização ativa.")
          .append(" Retorne APENAS JSON válido, sem markdown nem explicações.");
        return sb.toString();
    }

    private String montarUser(int quantidade, String texto) {
        String conteudo = texto == null ? "" : texto;
        if (conteudo.length() > 8000) {
            conteudo = conteudo.substring(0, 8000);
        }
        return """
                Gere exatamente %d flashcards a partir do texto abaixo.
                Regras:
                - A "frente" deve ser uma pergunta direta e objetiva
                - O "verso" deve conter a resposta completa mas concisa (max 2-3 linhas)
                - Priorize conceitos cobrados em provas: definições, prazos, princípios, artigos de lei, competências
                - Se houver dispositivo legal, inclua o número no verso
                - Linguagem simples e direta, sem jargão desnecessário
                - Varie os tipos de pergunta (o que é, qual, quem, quando, como)

                Formato JSON:
                [{"frente":"pergunta aqui","verso":"resposta completa aqui"}]

                TEXTO:
                %s""".formatted(quantidade, conteudo);
    }

    private String montarTitulo(String banca, String disciplina) {
        String base = StringUtils.hasText(disciplina) ? disciplina
                : (!"Geral".equalsIgnoreCase(banca) ? banca : "Deck");
        return base + " — " + DATA_BR.format(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // =========================================================================
    // PARSE ROBUSTO DO JSON
    // =========================================================================

    /**
     * Faz o parse do texto da IA em uma lista de flashcards, de forma tolerante:
     * remove cercas markdown (```json ... ```) e valida com Jackson.
     *
     * @param bruto texto retornado pela IA
     * @return lista de flashcards
     * @throws IAIndisponivelException se o conteúdo não for um JSON de flashcards válido
     */
    private List<FlashcardDTO> parsearFlashcards(String bruto) {
        if (!StringUtils.hasText(bruto)) {
            throw new IAIndisponivelException("A IA não retornou nenhum conteúdo. Tente novamente.");
        }
        String limpo = limparCercas(bruto);
        try {
            List<FlashcardDTO> cartoes = objectMapper.readValue(limpo, new TypeReference<>() {});
            if (cartoes == null || cartoes.isEmpty()) {
                throw new IAIndisponivelException("A IA não gerou nenhum flashcard. Tente um texto mais detalhado.");
            }
            // Descarta cartões malformados (sem frente ou verso)
            List<FlashcardDTO> validos = cartoes.stream()
                    .filter(c -> StringUtils.hasText(c.frente()) && StringUtils.hasText(c.verso()))
                    .toList();
            if (validos.isEmpty()) {
                throw new IAIndisponivelException("Os flashcards retornados estavam incompletos. Tente novamente.");
            }
            return validos;
        } catch (IAIndisponivelException e) {
            throw e;
        } catch (Exception e) {
            throw new IAIndisponivelException(
                    "Não foi possível interpretar a resposta da IA. Tente novamente.", e);
        }
    }

    /**
     * Remove cercas de código markdown (```json, ```) que alguns modelos adicionam.
     */
    private String limparCercas(String texto) {
        String limpo = texto.trim();
        // Remove uma cerca de abertura opcional com linguagem (```json, ```JSON, ```)
        limpo = limpo.replaceFirst("(?s)^```[a-zA-Z]*\\s*", "");
        // Remove uma cerca de fechamento no final
        limpo = limpo.replaceFirst("(?s)\\s*```\\s*$", "");
        return limpo.trim();
    }
}
