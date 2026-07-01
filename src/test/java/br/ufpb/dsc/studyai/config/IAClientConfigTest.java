package br.ufpb.dsc.studyai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa que {@link IAClientConfig} produz um {@link RestClient} utilizável.
 */
class IAClientConfigTest {

    @Test
    void iaRestClient_naoNulo() {
        IAProperties props = new IAProperties();
        props.setTimeoutSegundos(10);

        RestClient client = new IAClientConfig().iaRestClient(props, new ObjectMapper());

        assertThat(client).isNotNull();
    }
}
