package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogService;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.service.CorretorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CorretorController.class)
class CorretorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorretorService corretorService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser
    void pagina_retornaTemplateDoCorretor() throws Exception {
        mockMvc.perform(get("/corretor"))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/corretor"))
                .andExpect(model().attributeExists("titulo"));
    }

    @Test
    @WithMockUser
    void avaliar_sucesso_retornaFragmentoResult() throws Exception {
        Redacao redacao = new Redacao("ENEM", "Tema X", "Texto Y", 1000.0, "Perfeito");
        redacao.setId(1L);

        when(corretorService.avaliar(any(), anyString())).thenReturn(redacao);

        mockMvc.perform(post("/corretor/avaliar")
                        .with(csrf())
                        .principal(new java.security.Principal() {
                            @Override
                            public String getName() {
                                return "user";
                            }
                        })
                        .param("banca", "ENEM")
                        .param("tema", "Tema X")
                        .param("texto", "Texto Y"))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/fragments/corretor-result :: avaliacao"))
                .andExpect(model().attributeExists("redacao"));
    }

    @Test
    @WithMockUser
    void avaliar_iaIndisponivel_retornaFragmentoDeErro() throws Exception {
        when(corretorService.avaliar(any(), anyString())).thenThrow(new IAIndisponivelException("Fora do ar"));

        mockMvc.perform(post("/corretor/avaliar")
                        .with(csrf())
                        .principal(new java.security.Principal() {
                            @Override
                            public String getName() {
                                return "user";
                            }
                        })
                        .param("banca", "ENEM")
                        .param("tema", "Tema X")
                        .param("texto", "Texto Y"))
                .andExpect(status().isOk())
                .andExpect(view().name("studyai/fragments/corretor-result :: erro"))
                .andExpect(model().attributeExists("mensagem"));
    }
}
