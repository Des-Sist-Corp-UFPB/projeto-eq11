package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.domain.Roadmap;
import br.ufpb.dsc.studyai.domain.SemanaEstudo;
import br.ufpb.dsc.studyai.domain.TarefaEstudo;
import br.ufpb.dsc.studyai.dto.RoadmapRequest;
import br.ufpb.dsc.studyai.dto.RoadmapResponse;
import br.ufpb.dsc.studyai.dto.SemanaPlanejada;
import br.ufpb.dsc.studyai.dto.TarefaPlanejada;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.RoadmapRepository;
import br.ufpb.dsc.studyai.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @Mock
    private RoadmapAiService roadmapAiService;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IAProperties props;

    private RoadmapService service;

    @BeforeEach
    void setUp() {
        service = new RoadmapService(roadmapAiService, roadmapRepository, usuarioRepository, props);
        lenient().when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RoadmapRequest pedido(String inicio, String fim) {
        return new RoadmapRequest(inicio, fim, 3, "Medicina",
                List.of("Matemática", "Redação"), "Nunca fiz o ENEM", "");
    }

    @Test
    void gerar_modoDemo_cobreOPeriodoInteiro() {
        when(props.isDemo()).thenReturn(true);

        // 10/08 a 30/08 = 21 dias = 3 semanas exatas
        Roadmap roadmap = service.gerar(pedido("2026-08-10", "2026-08-30"), "user");

        assertThat(roadmap.getSemanas()).hasSize(3);
        assertThat(roadmap.getDataInicio()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(roadmap.getDataFim()).isEqualTo(LocalDate.of(2026, 8, 30));
        assertThat(roadmap.getSemanas().get(0).getDataInicio()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(roadmap.getSemanas().get(2).getDataFim()).isEqualTo(LocalDate.of(2026, 8, 30));
        assertThat(roadmap.getTotalTarefas()).isGreaterThan(0);
        assertThat(roadmap.getDificuldades()).isEqualTo("Matemática, Redação");
    }

    @Test
    void gerar_ultimaSemanaParcial_terminaExatamenteNaDataFinal() {
        when(props.isDemo()).thenReturn(true);

        // 10/08 a 20/08 = 11 dias = 1 semana cheia + 4 dias
        Roadmap roadmap = service.gerar(pedido("2026-08-10", "2026-08-20"), "user");

        assertThat(roadmap.getSemanas()).hasSize(2);
        SemanaEstudo ultima = roadmap.getSemanas().get(1);
        assertThat(ultima.getDataInicio()).isEqualTo(LocalDate.of(2026, 8, 17));
        assertThat(ultima.getDataFim()).isEqualTo(LocalDate.of(2026, 8, 20));
        // Nenhuma tarefa pode cair fora do período pedido
        assertThat(roadmap.getSemanas().stream().flatMap(s -> s.getTarefas().stream()))
                .allMatch(t -> !t.getData().isBefore(LocalDate.of(2026, 8, 10))
                        && !t.getData().isAfter(LocalDate.of(2026, 8, 20)));
    }

    @Test
    void gerar_comIa_persisteSemanasETarefas() {
        when(props.isDemo()).thenReturn(false);
        when(roadmapAiService.gerarRoadmap(anyString(), anyString(), anyInt(), anyInt(), anyInt(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new RoadmapResponse(
                        "Plano ENEM — Medicina",
                        "Foco em exatas nas primeiras semanas.",
                        List.of(new SemanaPlanejada(1, "Funções", List.of(
                                new TarefaPlanejada("2026-08-11", "Funções do 1º grau", "Teoria + 10 questões", 90)
                        )))));

        Roadmap roadmap = service.gerar(pedido("2026-08-10", "2026-08-16"), "user");

        assertThat(roadmap.getTitulo()).isEqualTo("Plano ENEM — Medicina");
        assertThat(roadmap.getResumoEstrategia()).isEqualTo("Foco em exatas nas primeiras semanas.");
        assertThat(roadmap.getSemanas()).hasSize(1);
        assertThat(roadmap.getSemanas().get(0).getFoco()).isEqualTo("Funções");

        TarefaEstudo tarefa = roadmap.getSemanas().get(0).getTarefas().get(0);
        assertThat(tarefa.getAssunto()).isEqualTo("Funções do 1º grau");
        assertThat(tarefa.getData()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(tarefa.getDuracaoMinutos()).isEqualTo(90);
    }

    @Test
    void gerar_dataDaIaForaDaSemana_ehReposicionadaDentroDoPeriodo() {
        when(props.isDemo()).thenReturn(false);
        when(roadmapAiService.gerarRoadmap(anyString(), anyString(), anyInt(), anyInt(), anyInt(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new RoadmapResponse("Plano", "Resumo", List.of(
                        new SemanaPlanejada(1, "Funções", List.of(
                                new TarefaPlanejada("2030-01-01", "Fora do período", "...", 60),
                                new TarefaPlanejada("data-invalida", "Formato quebrado", "...", 60)
                        )))));

        Roadmap roadmap = service.gerar(pedido("2026-08-10", "2026-08-16"), "user");

        List<TarefaEstudo> tarefas = roadmap.getSemanas().get(0).getTarefas();
        assertThat(tarefas).hasSize(2);
        assertThat(tarefas.get(0).getData()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(tarefas.get(1).getData()).isEqualTo(LocalDate.of(2026, 8, 11));
    }

    @Test
    void gerar_semanaOmitidaPelaIa_ehPreenchidaComPlanoPadrao() {
        when(props.isDemo()).thenReturn(false);
        // Período de 2 semanas, mas a IA devolve só a primeira
        when(roadmapAiService.gerarRoadmap(anyString(), anyString(), anyInt(), anyInt(), anyInt(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new RoadmapResponse("Plano", "Resumo", List.of(
                        new SemanaPlanejada(1, "Funções", List.of(
                                new TarefaPlanejada("2026-08-10", "Funções", "...", 60))))));

        Roadmap roadmap = service.gerar(pedido("2026-08-10", "2026-08-23"), "user");

        assertThat(roadmap.getSemanas()).hasSize(2);
        assertThat(roadmap.getSemanas().get(1).getFoco()).isNotBlank();
        assertThat(roadmap.getSemanas().get(1).getTarefas()).isNotEmpty();
    }

    @Test
    void gerar_duracaoAbsurda_ehLimitadaAoTempoDisponivel() {
        when(props.isDemo()).thenReturn(false);
        when(roadmapAiService.gerarRoadmap(anyString(), anyString(), anyInt(), anyInt(), anyInt(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new RoadmapResponse("Plano", "Resumo", List.of(
                        new SemanaPlanejada(1, "Funções", List.of(
                                new TarefaPlanejada("2026-08-10", "Maratona", "...", 6000),
                                new TarefaPlanejada("2026-08-11", "Rápida demais", "...", 2))))));

        Roadmap roadmap = service.gerar(pedido("2026-08-10", "2026-08-16"), "user");

        List<TarefaEstudo> tarefas = roadmap.getSemanas().get(0).getTarefas();
        assertThat(tarefas.get(0).getDuracaoMinutos()).isEqualTo(3 * 60); // teto = horas por dia
        assertThat(tarefas.get(1).getDuracaoMinutos()).isEqualTo(15);     // piso
    }

    @Test
    void gerar_dataFinalAntesDaInicial_lancaIllegalArgument() {
        assertThatThrownBy(() -> service.gerar(pedido("2026-08-20", "2026-08-10"), "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void gerar_dataAusente_lancaIllegalArgument() {
        assertThatThrownBy(() -> service.gerar(pedido("", "2026-08-10"), "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data de início");
    }

    @Test
    void gerar_periodoAcimaDoLimite_lancaIllegalArgument() {
        assertThatThrownBy(() -> service.gerar(pedido("2026-01-01", "2026-12-31"), "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(RoadmapService.MAX_SEMANAS));
    }

    @Test
    void gerar_iaForaDoAr_lancaIaIndisponivel() {
        when(props.isDemo()).thenReturn(false);
        when(roadmapAiService.gerarRoadmap(anyString(), anyString(), anyInt(), anyInt(), anyInt(),
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> service.gerar(pedido("2026-08-10", "2026-08-16"), "user"))
                .isInstanceOf(IAIndisponivelException.class);
    }

    @Test
    void gerar_iaDevolveuPlanoVazio_lancaIaIndisponivel() {
        when(props.isDemo()).thenReturn(false);
        when(roadmapAiService.gerarRoadmap(anyString(), anyString(), anyInt(), anyInt(), anyInt(),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new RoadmapResponse("Plano", "Resumo", List.of()));

        assertThatThrownBy(() -> service.gerar(pedido("2026-08-10", "2026-08-16"), "user"))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("não conseguiu montar");
    }
}
