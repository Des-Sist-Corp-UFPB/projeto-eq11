package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogoutHandler;
import br.ufpb.dsc.studyai.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testa o health check público {@code GET /ping}.
 *
 * <p>Usa {@code @WebMvcTest} (fatia web, sem banco/Testcontainers) e importa o
 * {@link SecurityConfig} real para garantir que a rota está liberada com
 * {@code permitAll()} — ou seja, responde sem autenticação.
 */
@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // O SecurityConfig importado injeta um AuditLogoutHandler na cadeia de logout;
    // no slice web ele não é escaneado, então fornecemos um mock para satisfazer a dependência.
    @MockBean
    private AuditLogoutHandler auditLogoutHandler;

    @Test
    void ping_deveResponder200JsonPublicamente_semLogin() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("eq11"))
                // timestamp em ISO-8601 UTC (termina com Z), ex.: 2026-06-03T14:32:10Z
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.endsWith("Z")));
    }
}
