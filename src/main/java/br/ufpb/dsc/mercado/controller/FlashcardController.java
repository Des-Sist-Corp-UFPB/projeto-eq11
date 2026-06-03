package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Deck;
import br.ufpb.dsc.mercado.dto.FlashcardDTO;
import br.ufpb.dsc.mercado.dto.FlashcardRequest;
import br.ufpb.dsc.mercado.exception.IAIndisponivelException;
import br.ufpb.dsc.mercado.service.FlashcardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller do módulo <strong>FlashIA</strong>.
 *
 * <p>Rotas:
 * <ul>
 *   <li>{@code GET /flashcards} — página do módulo (formulário + área de resultado).</li>
 *   <li>{@code POST /flashcards/gerar} — gera os cartões e devolve um <em>fragmento</em>
 *       Thymeleaf, trocado na página via HTMX (sem recarregar).</li>
 * </ul>
 *
 * <p>O flip do cartão e a navegação do carrossel são feitos em JS client-side
 * (ver {@code /js/studyai.js}); o servidor só devolve o HTML dos cartões.
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
@RequestMapping("/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final ObjectMapper objectMapper;

    public FlashcardController(FlashcardService flashcardService, ObjectMapper objectMapper) {
        this.flashcardService = flashcardService;
        this.objectMapper = objectMapper;
    }

    /**
     * Exibe a página do FlashIA.
     *
     * @param model modelo Thymeleaf
     * @return template da página
     */
    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("titulo", "FlashIA");
        return "studyai/flashcards";
    }

    /**
     * Gera os flashcards a partir do formulário e devolve o fragmento com os cartões.
     *
     * <p>Acionado pelo HTMX: {@code hx-post="/flashcards/gerar"},
     * {@code hx-target="#flash-resultado"}, {@code hx-indicator="#flash-loading"}.
     *
     * @param request dados do formulário (mapeados pelo nome dos campos)
     * @param model   modelo Thymeleaf
     * @return fragmento com os cartões, ou fragmento de erro amigável
     */
    @PostMapping("/gerar")
    public String gerar(@ModelAttribute FlashcardRequest request, Model model) {
        try {
            Deck deck = flashcardService.gerar(request);
            List<FlashcardDTO> cartoes = deck.getFlashcards().stream()
                    .map(f -> new FlashcardDTO(f.getFrente(), f.getVerso()))
                    .toList();

            model.addAttribute("deck", deck);
            model.addAttribute("cartoes", cartoes);
            // JSON serializado no servidor para o JS client-side do carrossel consumir com segurança
            model.addAttribute("cartoesJson", objectMapper.writeValueAsString(cartoes));
            return "studyai/fragments/flashcard-result :: cards";
        } catch (IAIndisponivelException e) {
            model.addAttribute("mensagem", e.getMessage());
            return "studyai/fragments/flashcard-result :: erro";
        } catch (JsonProcessingException e) {
            model.addAttribute("mensagem", "Erro ao preparar os cartões para exibição.");
            return "studyai/fragments/flashcard-result :: erro";
        }
    }
}
