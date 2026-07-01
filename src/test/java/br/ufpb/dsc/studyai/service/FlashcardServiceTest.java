package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.dto.FlashcardRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Testes unitários de {@link FlashcardService}: orquestração da geração, montagem de
 * prompts e o parse robusto de JSON (incluindo os caminhos defensivos).
 *
 * <p>O {@link IAService} é mockado (Mockito), então nenhum teste depende de rede,
 * chave de API ou banco.
 */
@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private IAService iaService;

    @Mock
    private DeckRepository deckRepository;

    @Captor
    private ArgumentCaptor<String> systemCaptor;

    @Captor
    private ArgumentCaptor<String> userCaptor;

    private FlashcardService service;

    private static final String JSON_OK = """
            [
              {"frente":"Pergunta 1","verso":"Resposta 1"},
              {"frente":"Pergunta 2","verso":"Resposta 2"}
            ]
            """;

    @BeforeEach
    void setUp() {
        service = new FlashcardService(iaService, deckRepository, new ObjectMapper());
        // save devolve o próprio deck recebido (simula a persistência).
        // lenient: nem todo teste chega a persistir (casos de erro lançam antes do save).
        lenient().when(deckRepository.save(any(Deck.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void gerar_happyPath_criaDeckComCartoesNaOrdem() {
        when(iaService.completar(anyString(), anyString())).thenReturn(JSON_OK);

        Deck deck = service.gerar(new FlashcardRequest("Cebraspe", "Direito", 2, "texto base"));

        assertThat(deck.getBanca()).isEqualTo("Cebraspe");
        assertThat(deck.getDisciplina()).isEqualTo("Direito");
        assertThat(deck.getFlashcards()).hasSize(2);
        assertThat(deck.getFlashcards().get(0).getFrente()).isEqualTo("Pergunta 1");
        assertThat(deck.getFlashcards().get(0).getOrdem()).isZero();
        assertThat(deck.getFlashcards().get(1).getOrdem()).isEqualTo(1);
    }

    @Test
    void gerar_montaPromptComBancaEDisciplinaEQuantidade() {
        when(iaService.completar(systemCaptor.capture(), userCaptor.capture())).thenReturn(JSON_OK);

        service.gerar(new FlashcardRequest("FGV", "Português", 5, "conteúdo"));

        assertThat(systemCaptor.getValue())
                .contains("banca FGV")
                .contains("Português")
                .contains("APENAS JSON");
        assertThat(userCaptor.getValue())
                .contains("Gere exatamente 5 flashcards")
                .contains("conteúdo");
    }

    @Test
    void gerar_bancaVaziaViraGeral_eNaoCitaBancaNoPrompt() {
        when(iaService.completar(systemCaptor.capture(), anyString())).thenReturn(JSON_OK);

        Deck deck = service.gerar(new FlashcardRequest("  ", null, 0, "x"));

        assertThat(deck.getBanca()).isEqualTo("Geral");
        assertThat(systemCaptor.getValue()).doesNotContain("banca Geral");
    }

    @Test
    void gerar_quantidadeInvalidaUsaPadrao15() {
        when(iaService.completar(anyString(), userCaptor.capture())).thenReturn(JSON_OK);

        service.gerar(new FlashcardRequest("Geral", "", 0, "x"));

        assertThat(userCaptor.getValue()).contains("exatamente 15 flashcards");
    }

    @Test
    void gerar_quantidadeAcimaDoMaximoEhLimitadaA30() {
        when(iaService.completar(anyString(), userCaptor.capture())).thenReturn(JSON_OK);

        service.gerar(new FlashcardRequest("Geral", "", 999, "x"));

        assertThat(userCaptor.getValue()).contains("exatamente 30 flashcards");
    }

    @Test
    void gerar_textoMuitoLongoEhTruncado() {
        String textoEnorme = "a".repeat(9000);
        when(iaService.completar(anyString(), userCaptor.capture())).thenReturn(JSON_OK);

        service.gerar(new FlashcardRequest("Geral", "", 10, textoEnorme));

        // O conteúdo é cortado em 8000 caracteres antes de ir ao prompt:
        // o trecho com 8000 'a' aparece, mas não um com 8001.
        assertThat(userCaptor.getValue())
                .contains("a".repeat(8000))
                .doesNotContain("a".repeat(8001));
    }

    @Test
    void gerar_removeCercasMarkdown() {
        String comCercas = "```json\n" + JSON_OK + "\n```";
        when(iaService.completar(anyString(), anyString())).thenReturn(comCercas);

        Deck deck = service.gerar(new FlashcardRequest("Geral", "", 2, "x"));

        assertThat(deck.getFlashcards()).hasSize(2);
    }

    @Test
    void gerar_descartaCartoesIncompletos() {
        String comInvalidos = """
                [
                  {"frente":"Boa","verso":"Resposta"},
                  {"frente":"","verso":"sem frente"},
                  {"frente":"sem verso","verso":""}
                ]
                """;
        when(iaService.completar(anyString(), anyString())).thenReturn(comInvalidos);

        Deck deck = service.gerar(new FlashcardRequest("Geral", "", 3, "x"));

        assertThat(deck.getFlashcards()).hasSize(1);
        assertThat(deck.getFlashcards().get(0).getFrente()).isEqualTo("Boa");
    }

    @Test
    void gerar_respostaVaziaLancaExcecao() {
        when(iaService.completar(anyString(), anyString())).thenReturn("   ");

        assertThatThrownBy(() -> service.gerar(new FlashcardRequest("Geral", "", 2, "x")))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("não retornou");
    }

    @Test
    void gerar_arrayVazioLancaExcecao() {
        when(iaService.completar(anyString(), anyString())).thenReturn("[]");

        assertThatThrownBy(() -> service.gerar(new FlashcardRequest("Geral", "", 2, "x")))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("nenhum flashcard");
    }

    @Test
    void gerar_todosCartoesIncompletosLancaExcecao() {
        when(iaService.completar(anyString(), anyString()))
                .thenReturn("[{\"frente\":\"\",\"verso\":\"\"}]");

        assertThatThrownBy(() -> service.gerar(new FlashcardRequest("Geral", "", 2, "x")))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("incompletos");
    }

    @Test
    void gerar_jsonInvalidoLancaExcecao() {
        when(iaService.completar(anyString(), anyString())).thenReturn("isto não é json");

        assertThatThrownBy(() -> service.gerar(new FlashcardRequest("Geral", "", 2, "x")))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("interpretar");
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
