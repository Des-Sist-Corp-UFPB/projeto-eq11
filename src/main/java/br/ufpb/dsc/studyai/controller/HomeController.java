package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import br.ufpb.dsc.studyai.repository.FlashcardRepository;
import br.ufpb.dsc.studyai.repository.RedacaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller da página inicial (dashboard) do StudyAI.
 *
 * <p>Exibe as estatísticas reais (decks e flashcards já gerados) e os cards dos
 * três módulos: FlashIA (ativo), CorretorIA (ativo) e PrevêTema (em breve).
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
public class HomeController {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final RedacaoRepository redacaoRepository;

    public HomeController(DeckRepository deckRepository, FlashcardRepository flashcardRepository, RedacaoRepository redacaoRepository) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.redacaoRepository = redacaoRepository;
    }

    /**
     * Renderiza a home com estatísticas reais e os decks/redações mais recentes.
     *
     * @param model modelo Thymeleaf
     * @return template da home
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Deck> decks = deckRepository.findAllByOrderByCriadoEmDesc();
        List<Redacao> redacoes = redacaoRepository.findAllByOrderByCriadoEmDesc();
        
        model.addAttribute("totalDecks", decks.size());
        model.addAttribute("totalFlashcards", flashcardRepository.count());
        model.addAttribute("totalRedacoes", redacoes.size());
        
        // Mostra apenas os 5 recentes na home
        model.addAttribute("decksRecentes", decks.stream().limit(5).toList());
        model.addAttribute("redacoesRecentes", redacoes.stream().limit(5).toList());
        
        model.addAttribute("titulo", "Início");
        return "studyai/home";
    }
}
