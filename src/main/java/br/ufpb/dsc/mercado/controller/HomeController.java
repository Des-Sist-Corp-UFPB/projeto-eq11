package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.Deck;
import br.ufpb.dsc.mercado.repository.DeckRepository;
import br.ufpb.dsc.mercado.repository.FlashcardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller da página inicial (dashboard) do StudyAI.
 *
 * <p>Exibe as estatísticas reais (decks e flashcards já gerados) e os cards dos
 * três módulos: FlashIA (ativo), CorretorIA e PrevêTema (em breve).
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
public class HomeController {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;

    public HomeController(DeckRepository deckRepository, FlashcardRepository flashcardRepository) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
    }

    /**
     * Renderiza a home com estatísticas reais e os decks mais recentes.
     *
     * @param model modelo Thymeleaf
     * @return template da home
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Deck> decks = deckRepository.findAllByOrderByCriadoEmDesc();
        model.addAttribute("totalDecks", decks.size());
        model.addAttribute("totalFlashcards", flashcardRepository.count());
        // Mostra apenas os 5 decks mais recentes na home
        model.addAttribute("decksRecentes", decks.stream().limit(5).toList());
        model.addAttribute("titulo", "Início");
        return "studyai/home";
    }
}
