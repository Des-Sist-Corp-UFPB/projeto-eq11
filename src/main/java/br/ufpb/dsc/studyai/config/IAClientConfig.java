package br.ufpb.dsc.studyai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuração do cliente HTTP usado para falar com os provedores de IA.
 *
 * <p>Centraliza a criação do {@link RestClient} (timeout generoso — chamadas de LLM
 * são lentas — e o {@link ObjectMapper} da aplicação para (de)serializar JSON). Expor
 * o cliente como bean, em vez de construí-lo dentro do service, segue o princípio de
 * injeção de dependências e <strong>torna o {@code IAServiceImpl} testável</strong>:
 * os testes injetam um {@code RestClient} ligado a um {@code MockRestServiceServer},
 * sem bater na API real nem depender de chave/rede.
 *
 * @author DSC - UFPB Campus IV
 */
@Configuration
public class IAClientConfig {

    /**
     * Cria o {@link RestClient} dos provedores de IA com timeout configurável.
     *
     * @param props        propriedades da IA (fornecem o timeout)
     * @param objectMapper mapeador JSON da aplicação
     * @return cliente HTTP pronto para uso pelos provedores
     */
    @Bean
    public RestClient iaRestClient(IAProperties props, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(props.getTimeoutSegundos());
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        return RestClient.builder()
                .requestFactory(factory)
                // Garante que o JSON use o ObjectMapper da aplicação (config consistente).
                .messageConverters(converters ->
                        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper)))
                .build();
    }
}
