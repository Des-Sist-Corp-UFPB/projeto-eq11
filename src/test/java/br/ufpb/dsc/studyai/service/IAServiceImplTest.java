package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpMethod;

/**
 * Testes de {@link IAServiceImpl} cobrindo os três caminhos do Strategy
 * (demo / anthropic / gemini) e os tratamentos de erro.
 *
 * <p>Os provedores reais são testados com {@link MockRestServiceServer} ligado ao
 * {@link RestClient} — nenhuma chamada real à internet nem chave verdadeira.
 */
class IAServiceImplTest {

    private IAProperties propsReal(String provedor) {
        IAProperties props = new IAProperties();
        props.setModo("real");
        props.setProvedor(provedor);
        props.setApiKey("chave-de-teste");
        props.setModelo("modelo-x");
        return props;
    }

    // ---------------------------------------------------------------- DEMO

    @Test
    void demo_devolveExemploSemChamadaExterna() {
        IAProperties props = new IAProperties(); // modo demo é o padrão
        IAServiceImpl service = new IAServiceImpl(props, RestClient.builder().build());

        String resposta = service.completar("sys", "user");

        assertThat(resposta).contains("legalidade");
    }

    @Test
    void demo_quandoModoRealMasSemChave() {
        IAProperties props = new IAProperties();
        props.setModo("real");
        props.setApiKey(""); // sem chave → isDemo() true
        IAServiceImpl service = new IAServiceImpl(props, RestClient.builder().build());

        assertThat(service.completar("sys", "user")).contains("LIMPE");
    }

    // ------------------------------------------------------------ ANTHROPIC

    @Test
    void anthropic_sucesso_extraiTexto() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        IAServiceImpl service = new IAServiceImpl(propsReal("anthropic"), builder.build());

        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", "chave-de-teste"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andRespond(withSuccess("{\"content\":[{\"text\":\"RESPOSTA IA\"}]}",
                        MediaType.APPLICATION_JSON));

        assertThat(service.completar("sys", "user")).isEqualTo("RESPOSTA IA");
        server.verify();
    }

    @Test
    void anthropic_respostaInesperada_lancaExcecao() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        IAServiceImpl service = new IAServiceImpl(propsReal("anthropic"), builder.build());

        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.completar("sys", "user"))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("Anthropic");
    }

    // --------------------------------------------------------------- GEMINI

    @Test
    void gemini_sucesso_usaHeaderDaChave_eExtraiTexto() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        IAServiceImpl service = new IAServiceImpl(propsReal("gemini"), builder.build());

        server.expect(requestTo(
                        "https://generativelanguage.googleapis.com/v1beta/models/modelo-x:generateContent"))
                .andExpect(method(HttpMethod.POST))
                // A chave deve ir no header, NÃO na query string
                .andExpect(header("x-goog-api-key", "chave-de-teste"))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"GEM OK\"}]}}]}",
                        MediaType.APPLICATION_JSON));

        assertThat(service.completar("sys", "user")).isEqualTo("GEM OK");
        server.verify();
    }

    @Test
    void gemini_respostaInesperada_lancaExcecao() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        IAServiceImpl service = new IAServiceImpl(propsReal("gemini"), builder.build());

        server.expect(requestTo(
                        "https://generativelanguage.googleapis.com/v1beta/models/modelo-x:generateContent"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.completar("sys", "user"))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("Gemini");
    }

    // ----------------------------------------------------------- ERRO/OUTROS

    @Test
    void provedorDesconhecido_lancaExcecao() {
        IAServiceImpl service = new IAServiceImpl(propsReal("xpto"), RestClient.builder().build());

        assertThatThrownBy(() -> service.completar("sys", "user"))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("desconhecido");
    }

    @Test
    void erroHttp_eEnvolvidoEmIAIndisponivel() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        IAServiceImpl service = new IAServiceImpl(propsReal("anthropic"), builder.build());

        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.completar("sys", "user"))
                .isInstanceOf(IAIndisponivelException.class)
                .hasMessageContaining("contatar");
    }
}
