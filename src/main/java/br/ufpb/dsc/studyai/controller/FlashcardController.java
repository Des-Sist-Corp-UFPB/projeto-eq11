package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogService;
import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.dto.FlashcardDTO;
import br.ufpb.dsc.studyai.dto.FlashcardRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.service.FlashcardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
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
    private final AuditLogService auditLogService;

    public FlashcardController(FlashcardService flashcardService,
                              ObjectMapper objectMapper,
                              AuditLogService auditLogService) {
        this.flashcardService = flashcardService;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
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
     * Reabre um deck salvo: renderiza a página do FlashIA com o carrossel já
     * preenchido com os cartões daquele deck (reaproveita o fragmento de resultado).
     *
     * <p>Acionado ao clicar num deck na home ({@code /flashcards/{id}}).
     *
     * @param id    identificador do deck
     * @param model modelo Thymeleaf
     * @return página do FlashIA com o deck carregado (ou o placeholder, se não existir)
     */
    @GetMapping("/{id}")
    public String abrirDeck(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute("titulo", "FlashIA");
        var deckOpt = flashcardService.buscarDeck(id, principal.getName());
        if (deckOpt.isPresent()) {
            Deck deck = deckOpt.get();
            List<FlashcardDTO> cartoes = deck.getFlashcards().stream()
                    .map(f -> new FlashcardDTO(f.getFrente(), f.getVerso()))
                    .toList();
            model.addAttribute("deck", deck);
            model.addAttribute("cartoes", cartoes);
            try {
                model.addAttribute("cartoesJson", objectMapper.writeValueAsString(cartoes));
            } catch (JsonProcessingException e) {
                // Sem JSON dos cartões, a página cai no placeholder em vez de quebrar
                model.addAttribute("deck", null);
            }
        }
        // Se o deck não existir, "deck" fica ausente e a página exibe o placeholder padrão
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
    public String gerar(@ModelAttribute FlashcardRequest request, Model model, Principal principal) {
        try {
            Deck deck = flashcardService.gerar(request, principal.getName());
            List<FlashcardDTO> cartoes = deck.getFlashcards().stream()
                    .map(f -> new FlashcardDTO(f.getFrente(), f.getVerso()))
                    .toList();

            // Auditoria: registra a geração de flashcards (usuário e IP resolvidos no service)
            auditLogService.registrar(
                    "GERAR_FLASHCARD", "deck", deck.getId(),
                    cartoes.size() + " cartões; banca=" + deck.getBanca());

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
