package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.dto.FlashcardDTO;
import br.ufpb.dsc.studyai.dto.FlashcardRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardAiService flashcardAiService;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private IAProperties props;

    private FlashcardService service;

    @BeforeEach
    void setUp() {
        service = new FlashcardService(flashcardAiService, deckRepository, props);
        lenient().when(deckRepository.save(any(Deck.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void gerar_happyPath_criaDeckComCartoesNaOrdem() {
        when(props.isDemo()).thenReturn(false);
        when(flashcardAiService.gerarFlashcards(any(), any(), anyInt(), any()))
                .thenReturn(new br.ufpb.dsc.studyai.dto.FlashcardResponse(List.of(
                        new FlashcardDTO("Pergunta 1", "Resposta 1"),
                        new FlashcardDTO("Pergunta 2", "Resposta 2")
                )));

        Deck deck = service.gerar(new FlashcardRequest("Cebraspe", "Direito", 2, "texto base"));

        assertThat(deck.getBanca()).isEqualTo("Cebraspe");
        assertThat(deck.getDisciplina()).isEqualTo("Direito");
        assertThat(deck.getFlashcards()).hasSize(2);
        assertThat(deck.getFlashcards().get(0).getFrente()).isEqualTo("Pergunta 1");
        assertThat(deck.getFlashcards().get(0).getOrdem()).isZero();
        assertThat(deck.getFlashcards().get(1).getOrdem()).isEqualTo(1);
    }

    @Test
    void gerar_modoDemo_retornaCartoesMock() {
        when(props.isDemo()).thenReturn(true);

        Deck deck = service.gerar(new FlashcardRequest("FGV", "Português", 5, "conteúdo"));

        assertThat(deck.getFlashcards()).hasSize(8); // O demo mock tem 8 itens hardcoded
    }

    @Test
    void gerar_listaVaziaLancaExcecao() {
        when(props.isDemo()).thenReturn(false);
        when(flashcardAiService.gerarFlashcards(anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(new br.ufpb.dsc.studyai.dto.FlashcardResponse(List.of()));

        assertThatThrownBy(() -> service.gerar(new FlashcardRequest("Geral", "", 2, "x")))
                .isInstanceOf(IAIndisponivelException.class);
    }

    @Test
    void gerar_erroNoAiServiceLancaExcecao() {
        when(props.isDemo()).thenReturn(false);
        when(flashcardAiService.gerarFlashcards(anyString(), anyString(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("Erro genérico da IA"));

        assertThatThrownBy(() -> service.gerar(new FlashcardRequest("Geral", "", 2, "x")))
                .isInstanceOf(IAIndisponivelException.class);
    }

    @Test
    void buscarDeck_presente_inicializaCartoes() {
        Deck deck = new Deck("T", "Geral", "");
        deck.adicionarFlashcard(new br.ufpb.dsc.studyai.domain.Flashcard("F", "V", 0));
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        Optional<Deck> resultado = service.buscarDeck(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFlashcards()).hasSize(1);
    }

    @Test
    void buscarDeck_ausente_devolveVazio() {
        when(deckRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.buscarDeck(99L)).isEmpty();
    }
}
