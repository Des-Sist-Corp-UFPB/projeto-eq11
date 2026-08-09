package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.domain.Roadmap;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import br.ufpb.dsc.studyai.repository.RedacaoRepository;
import br.ufpb.dsc.studyai.repository.RoadmapRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * Controller da página inicial (dashboard) do StudyAI.
 *
 * <p>Exibe as estatísticas reais (decks, flashcards, redações e planos de estudo já
 * gerados) e os cards dos três módulos: FlashIA, CorretorIA e RoadmapIA.
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
public class HomeController {

    private final DeckRepository deckRepository;
    private final RedacaoRepository redacaoRepository;
    private final RoadmapRepository roadmapRepository;

    public HomeController(DeckRepository deckRepository,
                          RedacaoRepository redacaoRepository,
                          RoadmapRepository roadmapRepository) {
        this.deckRepository = deckRepository;
        this.redacaoRepository = redacaoRepository;
        this.roadmapRepository = roadmapRepository;
    }

    /**
     * Renderiza a home com estatísticas reais e os itens mais recentes de cada módulo.
     *
     * @param model modelo Thymeleaf
     * @return template da home
     */
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        String username = principal.getName();
        List<Deck> decks = deckRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc(username);
        List<Redacao> redacoes = redacaoRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc(username);
        List<Roadmap> roadmaps = roadmapRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc(username);

        model.addAttribute("totalDecks", decks.size());

        // Soma os cartões dos decks do próprio usuário (contar a tabela inteira mostraria
        // os flashcards de todo mundo).
        long totalFlashcards = decks.stream().mapToLong(d -> d.getFlashcards().size()).sum();
        model.addAttribute("totalFlashcards", totalFlashcards);
        model.addAttribute("totalRedacoes", redacoes.size());
        model.addAttribute("totalRoadmaps", roadmaps.size());

        // Mostra apenas os 5 recentes na home
        model.addAttribute("decksRecentes", decks.stream().limit(5).toList());
        model.addAttribute("redacoesRecentes", redacoes.stream().limit(5).toList());
        model.addAttribute("roadmapsRecentes", roadmaps.stream().limit(5).toList());

        model.addAttribute("titulo", "Início");
        return "studyai/home";
    }
}
