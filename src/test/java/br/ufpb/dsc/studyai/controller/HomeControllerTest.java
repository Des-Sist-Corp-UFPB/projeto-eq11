package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import br.ufpb.dsc.studyai.repository.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.security.Principal;
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
    void home_exibeEstatisticasReais_eLimitaDecksRecentesA5() {
        DeckRepository deckRepository = mock(DeckRepository.class);
        FlashcardRepository flashcardRepository = mock(FlashcardRepository.class);
        br.ufpb.dsc.studyai.repository.RedacaoRepository redacaoRepository = mock(br.ufpb.dsc.studyai.repository.RedacaoRepository.class);

        List<Deck> seteDecks = IntStream.range(0, 7)
                .mapToObj(i -> {
                    Deck d = new Deck("Deck " + i, "Geral", "");
                    d.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 0));
                    d.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 1));
                    d.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 2));
                    d.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 3));
                    d.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 4));
                    d.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 5));
                    return d;
                })
                .toList();
        when(deckRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc("user")).thenReturn(seteDecks);
        when(flashcardRepository.count()).thenReturn(42L);

        List<br.ufpb.dsc.studyai.domain.Redacao> oitoRedacoes = IntStream.range(0, 8)
                .mapToObj(i -> new br.ufpb.dsc.studyai.domain.Redacao())
                .toList();
        when(redacaoRepository.findAllByUsuarioUsernameOrderByCriadoEmDesc("user")).thenReturn(oitoRedacoes);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user");

        HomeController controller = new HomeController(deckRepository, flashcardRepository, redacaoRepository);
        Model model = new ConcurrentModel();

        String view = controller.home(model, principal);

        assertThat(view).isEqualTo("studyai/home");
        assertThat(model.getAttribute("totalDecks")).isEqualTo(7);
        assertThat(model.getAttribute("totalFlashcards")).isEqualTo(42L);
        assertThat((List<?>) model.getAttribute("decksRecentes")).hasSize(5);
        assertThat(model.getAttribute("totalRedacoes")).isEqualTo(8);
        assertThat((List<?>) model.getAttribute("redacoesRecentes")).hasSize(5);
        assertThat(model.getAttribute("titulo")).isEqualTo("Início");
    }
}
