package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.dto.CorretorRequest;
import br.ufpb.dsc.studyai.dto.CorretorResponse;
import br.ufpb.dsc.studyai.dto.CriterioAvaliacao;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.RedacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorretorServiceTest {

    @Mock
    private CorretorAiService corretorAiService;

    @Mock
    private RedacaoRepository redacaoRepository;

    @Mock
    private IAProperties props;

    private CorretorService service;

    @BeforeEach
    void setUp() {
        service = new CorretorService(corretorAiService, redacaoRepository, props);
        lenient().when(redacaoRepository.save(any(Redacao.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void avaliar_happyPath_salvaEretornaRedacao() {
        when(props.isDemo()).thenReturn(false);
        when(corretorAiService.avaliarRedacao(any(), any(), any()))
                .thenReturn(new CorretorResponse(
                        900.0,
                        "Muito bom",
                        List.of(new CriterioAvaliacao("Gramatica", 180.0, "Quase perfeito"))
                ));

        Redacao redacao = service.avaliar(new CorretorRequest("ENEM", "Inteligência Artificial", "Meu texto..."));

        assertThat(redacao.getBanca()).isEqualTo("ENEM");
        assertThat(redacao.getNotaTotal()).isEqualTo(900.0);
        assertThat(redacao.getComentarioGeral()).isEqualTo("Muito bom");
        assertThat(redacao.getCriterios()).hasSize(1);
        assertThat(redacao.getCriterios().get(0).getNome()).isEqualTo("Gramatica");
        assertThat(redacao.getCriterios().get(0).getNota()).isEqualTo(180.0);
    }

    @Test
    void avaliar_modoDemo_retornaRedacaoMock() {
        when(props.isDemo()).thenReturn(true);

        Redacao redacao = service.avaliar(new CorretorRequest("FCC", "Saúde", "texto x"));

        assertThat(redacao.getNotaTotal()).isEqualTo(850.0);
        assertThat(redacao.getCriterios()).hasSize(5); // O demo mock tem 5 itens
    }

    @Test
    void avaliar_erroNoAiServiceLancaExcecao() {
        when(props.isDemo()).thenReturn(false);
        when(corretorAiService.avaliarRedacao(any(), any(), any()))
                .thenThrow(new RuntimeException("Erro genérico da IA"));

        assertThatThrownBy(() -> service.avaliar(new CorretorRequest("Geral", "Tema", "texto")))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("Erro na IA (Corretor)");
    }
}
