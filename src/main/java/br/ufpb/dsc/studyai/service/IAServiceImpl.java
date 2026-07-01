package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Implementação do {@link IAService} com <strong>modo demonstração obrigatório</strong>.
 *
 * <p>Comportamento:
 * <ul>
 *   <li><strong>Modo demo</strong> ({@code studyai.ia.modo=demo} ou chave vazia): devolve
 *       um JSON de flashcards de exemplo imediatamente, sem chamada externa. Garante que a
 *       demo funcione sem internet nem chave de API.</li>
 *   <li><strong>Modo real</strong>: chama o provedor configurado (Anthropic ou Gemini) via
 *       {@link RestClient}, com timeout generoso.</li>
 * </ul>
 *
 * @author DSC - UFPB Campus IV
 */
@Service
public class IAServiceImpl implements IAService {

    private static final Logger log = LoggerFactory.getLogger(IAServiceImpl.class);

    /**
     * Flashcards de exemplo retornados em modo demonstração.
     * Mesmo formato esperado da IA real, para que o fluxo (parse + persistência) seja idêntico.
     */
    private static final String EXEMPLO_JSON = """
            [
              {"frente":"O que é o princípio da legalidade na Administração Pública?","verso":"O administrador só pode fazer o que a lei autoriza ou determina (art. 37 da CF/88). Diferente do particular, que pode fazer tudo que a lei não proíbe."},
              {"frente":"Quais são os princípios expressos da Administração Pública no art. 37 da CF/88?","verso":"Legalidade, Impessoalidade, Moralidade, Publicidade e Eficiência — o mnemônico LIMPE."},
              {"frente":"Qual o prazo prescricional geral para a Fazenda Pública cobrar seus créditos tributários?","verso":"5 anos, contados do lançamento definitivo (art. 174 do CTN)."},
              {"frente":"O que caracteriza o ato administrativo discricionário?","verso":"Aquele em que a lei confere ao administrador margem de escolha quanto à conveniência e oportunidade (mérito administrativo), dentro dos limites legais."},
              {"frente":"Qual a diferença entre revogação e anulação de um ato administrativo?","verso":"Revogação: ato legal, mas inconveniente/inoportuno, com efeitos ex nunc. Anulação: ato ilegal, com efeitos ex tunc (retroativos)."},
              {"frente":"O que estabelece o princípio da impessoalidade?","verso":"A Administração deve tratar todos sem discriminação, visando ao interesse público, sem favorecer ou prejudicar pessoas determinadas."},
              {"frente":"Quem pode propor Ação Direta de Inconstitucionalidade (ADI)?","verso":"Os legitimados do art. 103 da CF/88, como o Presidente, a Mesa do Senado/Câmara, o PGR, o Conselho Federal da OAB e partidos políticos com representação no Congresso."},
              {"frente":"O que é o controle de constitucionalidade difuso?","verso":"Aquele exercido por qualquer juiz ou tribunal, no caso concreto, de forma incidental, com efeitos inter partes."}
            ]
            """;

    private final IAProperties props;
    private final RestClient restClient;

    public IAServiceImpl(IAProperties props, RestClient iaRestClient) {
        this.props = props;
        this.restClient = iaRestClient;
    }

    @Override
    public String completar(String system, String user) {
        // === MODO DEMO === retorna exemplo imediatamente, sem rede
        if (props.isDemo()) {
            log.info("IAService em MODO DEMO — retornando flashcards de exemplo (sem chamada externa).");
            return EXEMPLO_JSON;
        }

        // === MODO REAL === chama o provedor configurado
        String provedor = props.getProvedor() == null ? "" : props.getProvedor().trim().toLowerCase();
        try {
            return switch (provedor) {
                case "anthropic" -> chamarAnthropic(system, user);
                case "gemini" -> chamarGemini(system, user);
                default -> throw new IAIndisponivelException(
                        "Provedor de IA desconhecido: '" + provedor + "'. Use 'anthropic' ou 'gemini'.");
            };
        } catch (IAIndisponivelException e) {
            throw e;
        } catch (Exception e) {
            log.error("Falha ao chamar o provedor de IA '{}'", provedor, e);
            throw new IAIndisponivelException(
                    "Não foi possível contatar o serviço de IA agora. Tente novamente em instantes.", e);
        }
    }

    // =========================================================================
    // PROVEDORES
    // =========================================================================

    private String chamarAnthropic(String system, String user) {
        Map<String, Object> body = Map.of(
                "model", props.getModelo(),
                "max_tokens", 4096,
                "system", system,
                "messages", new Object[]{Map.of("role", "user", "content", user)}
        );

        JsonNode resposta = restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", props.getApiKey())
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        JsonNode texto = resposta == null ? null : resposta.at("/content/0/text");
        if (texto == null || texto.isMissingNode()) {
            throw new IAIndisponivelException("Resposta inesperada do provedor Anthropic.");
        }
        return texto.asText();
    }

    private String chamarGemini(String system, String user) {
        // A chave vai no header x-goog-api-key (e NÃO na query string ?key=...), evitando
        // que o segredo vaze em logs de acesso/proxies.
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + props.getModelo() + ":generateContent";

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", new Object[]{Map.of("text", system)}),
                "contents", new Object[]{
                        Map.of("role", "user", "parts", new Object[]{Map.of("text", user)})
                }
        );

        JsonNode resposta = restClient.post()
                .uri(url)
                .header("x-goog-api-key", props.getApiKey())
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        JsonNode texto = resposta == null ? null
                : resposta.at("/candidates/0/content/parts/0/text");
        if (texto == null || texto.isMissingNode()) {
            throw new IAIndisponivelException("Resposta inesperada do provedor Gemini.");
        }
        return texto.asText();
    }
}
