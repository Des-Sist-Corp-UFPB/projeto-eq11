package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogService;
import br.ufpb.dsc.studyai.domain.Roadmap;
import br.ufpb.dsc.studyai.dto.RoadmapRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.service.RoadmapService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RoadmapController.class)
class RoadmapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoadmapService roadmapService;

    @MockBean
    private AuditLogService auditLogService;

    private static final Principal PRINCIPAL = () -> "user";

    private Roadmap roadmapDeExemplo() {
        Roadmap roadmap = new Roadmap("Plano ENEM", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 11, 5), 3);
        roadmap.setId(1L);
        return roadmap;
    }

    @Test
    @WithMockUser
    void pagina_retornaTemplateDoRoadmap() throws Exception {
        mockMvc.perform(get("/roadmap"))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/roadmap"))
                .andExpect(model().attributeExists("titulo"));
    }

    @Test
    @WithMockUser
    void abrirRoadmap_existente_carregaNoModelo() throws Exception {
        when(roadmapService.buscarRoadmap(1L, "user")).thenReturn(Optional.of(roadmapDeExemplo()));

        mockMvc.perform(get("/roadmap/1").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/roadmap"))
                .andExpect(model().attributeExists("roadmap"));
    }

    @Test
    @WithMockUser
    void abrirRoadmap_inexistente_naoAdicionaAoModelo() throws Exception {
        when(roadmapService.buscarRoadmap(99L, "user")).thenReturn(Optional.empty());

        mockMvc.perform(get("/roadmap/99").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/roadmap"))
                .andExpect(model().attributeDoesNotExist("roadmap"));
    }

    @Test
    @WithMockUser
    void gerar_sucesso_retornaFragmentoDoCronogramaEAudita() throws Exception {
        when(roadmapService.gerar(any(), anyString())).thenReturn(roadmapDeExemplo());

        mockMvc.perform(post("/roadmap/gerar")
                        .with(csrf())
                        .principal(PRINCIPAL)
                        .param("dataInicio", "2026-08-10")
                        .param("dataFim", "2026-11-05")
                        .param("horasPorDia", "3")
                        .param("cursoAlvo", "Medicina")
                        .param("dificuldades", "Matemática")
                        .param("dificuldades", "Redação")
                        .param("experiencia", "Nunca fiz o ENEM")
                        .param("observacoes", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/fragments/roadmap-result :: roadmap"))
                .andExpect(model().attributeExists("roadmap"));

        verify(auditLogService).registrar(org.mockito.ArgumentMatchers.eq("GERAR_ROADMAP"),
                org.mockito.ArgumentMatchers.eq("roadmap"),
                org.mockito.ArgumentMatchers.eq(1L),
                anyString());
    }

    @Test
    @WithMockUser
    void gerar_dificuldadesMarcadas_chegamNoServicoComoLista() throws Exception {
        when(roadmapService.gerar(any(), anyString())).thenReturn(roadmapDeExemplo());

        mockMvc.perform(post("/roadmap/gerar")
                        .with(csrf())
                        .principal(PRINCIPAL)
                        .param("dataInicio", "2026-08-10")
                        .param("dataFim", "2026-11-05")
                        .param("horasPorDia", "4")
                        .param("dificuldades", "Matemática")
                        .param("dificuldades", "Ciências da Natureza"))
                .andExpect(status().isOk());

        ArgumentCaptor<RoadmapRequest> captor = ArgumentCaptor.forClass(RoadmapRequest.class);
        verify(roadmapService).gerar(captor.capture(), anyString());

        RoadmapRequest enviado = captor.getValue();
        assertThat(enviado.dificuldades()).containsExactly("Matemática", "Ciências da Natureza");
        assertThat(enviado.horasPorDia()).isEqualTo(4);
        assertThat(enviado.dataInicio()).isEqualTo("2026-08-10");
    }

    @Test
    @WithMockUser
    void gerar_questionarioInvalido_retornaFragmentoDeErro() throws Exception {
        when(roadmapService.gerar(any(), anyString()))
                .thenThrow(new IllegalArgumentException("A data final precisa ser posterior à data de início."));

        mockMvc.perform(post("/roadmap/gerar")
                        .with(csrf())
                        .principal(PRINCIPAL)
                        .param("dataInicio", "2026-11-05")
                        .param("dataFim", "2026-08-10")
                        .param("horasPorDia", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/fragments/roadmap-result :: erro"))
                .andExpect(model().attributeExists("mensagem"));
    }

    @Test
    @WithMockUser
    void gerar_iaIndisponivel_retornaFragmentoDeErro() throws Exception {
        when(roadmapService.gerar(any(), anyString())).thenThrow(new IAIndisponivelException("Fora do ar"));

        mockMvc.perform(post("/roadmap/gerar")
                        .with(csrf())
                        .principal(PRINCIPAL)
                        .param("dataInicio", "2026-08-10")
                        .param("dataFim", "2026-11-05")
                        .param("horasPorDia", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/fragments/roadmap-result :: erro"))
                .andExpect(model().attributeExists("mensagem"));
    }
}
