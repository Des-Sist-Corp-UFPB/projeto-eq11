package br.ufpb.dsc.studyai.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa que {@link GlobalModelAttributes} expõe a URI da requisição atual aos templates.
 */
class GlobalModelAttributesTest {

    @Test
    void requestURI_devolveAUriDaRequisicao() {
        GlobalModelAttributes advice = new GlobalModelAttributes();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/flashcards");

        assertThat(advice.requestURI(request)).isEqualTo("/flashcards");
    }
}
