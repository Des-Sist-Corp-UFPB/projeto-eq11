package br.ufpb.dsc.mercado.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Propriedades de configuração da IA, lidas do prefixo {@code studyai.ia}.
 *
 * <p>Os valores vêm do {@code application.yml}, que por sua vez lê variáveis de
 * ambiente — a chave de API <strong>nunca</strong> é versionada no código.
 *
 * <p>Mapeamento (kebab-case → camelCase):
 * <pre>
 *   studyai.ia.modo             → modo
 *   studyai.ia.provedor         → provedor
 *   studyai.ia.api-key          → apiKey
 *   studyai.ia.modelo           → modelo
 *   studyai.ia.timeout-segundos → timeoutSegundos
 * </pre>
 *
 * @author DSC - UFPB Campus IV
 */
@ConfigurationProperties(prefix = "studyai.ia")
public class IAProperties {

    /** "demo" para flashcards de exemplo sem chamada externa; "real" para chamar o provedor. */
    private String modo = "demo";

    /** Provedor de IA: "anthropic" ou "gemini". */
    private String provedor = "anthropic";

    /** Chave de API do provedor (via env STUDYAI_AI_API_KEY). Vazia em modo demo. */
    private String apiKey = "";

    /** Modelo a ser usado no provedor (via env STUDYAI_AI_MODELO). */
    private String modelo = "";

    /** Timeout das chamadas de IA (chamadas de LLM levam de 5 a 30s). */
    private int timeoutSegundos = 45;

    /**
     * Indica se a aplicação deve operar em modo demonstração.
     *
     * <p>É demo quando {@code modo=demo} (padrão) OU quando não há chave de API
     * configurada — garantindo que a demo do pitch funcione sem internet/chave.
     *
     * @return {@code true} se deve retornar flashcards de exemplo
     */
    public boolean isDemo() {
        return "demo".equalsIgnoreCase(modo) || !StringUtils.hasText(apiKey);
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public String getProvedor() {
        return provedor;
    }

    public void setProvedor(String provedor) {
        this.provedor = provedor;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getTimeoutSegundos() {
        return timeoutSegundos;
    }

    public void setTimeoutSegundos(int timeoutSegundos) {
        this.timeoutSegundos = timeoutSegundos;
    }
}
