package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.domain.Flashcard;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.domain.Roadmap;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import br.ufpb.dsc.studyai.repository.RedacaoRepository;
import br.ufpb.dsc.studyai.repository.RoadmapRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários de {@link HomeController} com repositórios mockados.
 */
class HomeControllerTest {

    @Test
    void home_exibeEstatisticasReais_eLimitaRecentesA5() {
        DeckRepository deckRepository = mock(DeckRepository.class);
        RedacaoRepository redacaoRepository = mock(RedacaoRepository.class);
        RoadmapRepository roadmapRepository = mock(RoadmapRepository.class);

        List<Deck> seteDecks = IntStream.range(0, 7)
                .mapToObj(i -> {
                    Deck d = new Deck("Deck " + i, "Geral", "");
                    IntStream.range(0, 6).forEach(j -> d.adicionarFlashcard(new Flashcard("F", "V", j)));
                    return d;
                })
                .toList();
        when(deckRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc("user")).thenReturn(seteDecks);

        List<Redacao> oitoRedacoes = IntStream.range(0, 8)
                .mapToObj(i -> new Redacao())
                .toList();
        when(redacaoRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc("user")).thenReturn(oitoRedacoes);

        List<Roadmap> tresRoadmaps = IntStream.range(0, 3)
                .mapToObj(i -> new Roadmap("Plano " + i, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 11, 5), 3))
                .toList();
        when(roadmapRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc("user")).thenReturn(tresRoadmaps);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user");

        HomeController controller = new HomeController(deckRepository, redacaoRepository, roadmapRepository);
        Model model = new ConcurrentModel();

        String view = controller.home(model, principal);

        assertThat(view).isEqualTo("studyai/home");
        assertThat(model.getAttribute("totalDecks")).isEqualTo(7);
        // 7 decks x 6 cartões — somados a partir dos decks do próprio usuário
        assertThat(model.getAttribute("totalFlashcards")).isEqualTo(42L);
        assertThat((List<?>) model.getAttribute("decksRecentes")).hasSize(5);
        assertThat(model.getAttribute("totalRedacoes")).isEqualTo(8);
        assertThat((List<?>) model.getAttribute("redacoesRecentes")).hasSize(5);
        assertThat(model.getAttribute("totalRoadmaps")).isEqualTo(3);
        assertThat((List<?>) model.getAttribute("roadmapsRecentes")).hasSize(3);
        assertThat(model.getAttribute("titulo")).isEqualTo("Início");
    }
}
