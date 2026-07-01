package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogService;
import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.domain.Flashcard;
import br.ufpb.dsc.studyai.dto.FlashcardRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.service.FlashcardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários de {@link FlashcardController} chamando os métodos diretamente
 * (sem MockMvc/Thymeleaf), com {@link FlashcardService} e {@link AuditLogService}
 * mockados. Foca na lógica do controller e na integração com a auditoria.
 */
class FlashcardControllerTest {

    private Deck deckComCartoes() {
        Deck deck = new Deck("Deck Teste", "Cebraspe", "Direito");
        deck.setId(10L);
        deck.adicionarFlashcard(new Flashcard("P1", "R1", 0));
        deck.adicionarFlashcard(new Flashcard("P2", "R2", 1));
        return deck;
    }

    @Test
    void pagina_retornaTemplateDoFlashIA() {
        FlashcardController controller = new FlashcardController(
                mock(FlashcardService.class), new ObjectMapper(), mock(AuditLogService.class));
        Model model = new ConcurrentModel();

        String view = controller.pagina(model);

        assertThat(view).isEqualTo("studyai/flashcards");
        assertThat(model.getAttribute("titulo")).isEqualTo("FlashIA");
    }

    @Test
    void abrirDeck_existente_carregaCartoesEJson() {
        FlashcardService service = mock(FlashcardService.class);
        when(service.buscarDeck(10L)).thenReturn(Optional.of(deckComCartoes()));
        FlashcardController controller = new FlashcardController(
                service, new ObjectMapper(), mock(AuditLogService.class));
        Model model = new ConcurrentModel();

        String view = controller.abrirDeck(10L, model);

        assertThat(view).isEqualTo("studyai/flashcards");
        assertThat(model.getAttribute("deck")).isNotNull();
        assertThat(model.getAttribute("cartoesJson")).asString().contains("P1");
    }

    @Test
    void abrirDeck_inexistente_caiNoPlaceholder() {
        FlashcardService service = mock(FlashcardService.class);
        when(service.buscarDeck(99L)).thenReturn(Optional.empty());
        FlashcardController controller = new FlashcardController(
                service, new ObjectMapper(), mock(AuditLogService.class));
        Model model = new ConcurrentModel();

        String view = controller.abrirDeck(99L, model);

        assertThat(view).isEqualTo("studyai/flashcards");
        assertThat(model.getAttribute("deck")).isNull();
    }

    @Test
    void abrirDeck_erroDeSerializacao_defineDeckComoNulo() throws Exception {
        FlashcardService service = mock(FlashcardService.class);
        when(service.buscarDeck(10L)).thenReturn(Optional.of(deckComCartoes()));
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("falha") {});
        FlashcardController controller = new FlashcardController(service, mapper, mock(AuditLogService.class));
        Model model = new ConcurrentModel();

        String view = controller.abrirDeck(10L, model);

        assertThat(view).isEqualTo("studyai/flashcards");
        // O catch redefine "deck" para null para a página cair no placeholder em vez de quebrar
        assertThat(model.getAttribute("deck")).isNull();
    }

    @Test
    void gerar_sucesso_retornaFragmentoERegistraAuditoria() {
        FlashcardService service = mock(FlashcardService.class);
        when(service.gerar(any(FlashcardRequest.class))).thenReturn(deckComCartoes());
        AuditLogService audit = mock(AuditLogService.class);
        FlashcardController controller = new FlashcardController(service, new ObjectMapper(), audit);
        Model model = new ConcurrentModel();

        String view = controller.gerar(new FlashcardRequest("Cebraspe", "Direito", 2, "txt"), model);

        assertThat(view).isEqualTo("studyai/fragments/flashcard-result :: cards");
        assertThat(model.getAttribute("cartoes")).isNotNull();
        verify(audit).registrar(eq("GERAR_FLASHCARD"), eq("deck"), eq(10L), anyString());
    }

    @Test
    void gerar_iaIndisponivel_retornaFragmentoDeErro_eNaoAudita() {
        FlashcardService service = mock(FlashcardService.class);
        when(service.gerar(any(FlashcardRequest.class)))
                .thenThrow(new IAIndisponivelException("IA fora do ar"));
        AuditLogService audit = mock(AuditLogService.class);
        FlashcardController controller = new FlashcardController(service, new ObjectMapper(), audit);
        Model model = new ConcurrentModel();

        String view = controller.gerar(new FlashcardRequest("Geral", "", 5, "txt"), model);

        assertThat(view).isEqualTo("studyai/fragments/flashcard-result :: erro");
        assertThat(model.getAttribute("mensagem")).isEqualTo("IA fora do ar");
        verify(audit, never()).registrar(anyString(), anyString(), any(), anyString());
    }

    @Test
    void gerar_erroDeSerializacao_retornaFragmentoDeErro() throws Exception {
        FlashcardService service = mock(FlashcardService.class);
        when(service.gerar(any(FlashcardRequest.class))).thenReturn(deckComCartoes());
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("falha") {});
        FlashcardController controller = new FlashcardController(service, mapper, mock(AuditLogService.class));
        Model model = new ConcurrentModel();

        String view = controller.gerar(new FlashcardRequest("Geral", "", 5, "txt"), model);

        assertThat(view).isEqualTo("studyai/fragments/flashcard-result :: erro");
        assertThat(model.getAttribute("mensagem")).asString().contains("preparar os cartões");
    }
}
