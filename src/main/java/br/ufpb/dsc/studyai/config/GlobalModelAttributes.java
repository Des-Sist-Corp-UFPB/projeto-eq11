package br.ufpb.dsc.studyai.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adiciona atributos globais ao modelo de todos os templates Thymeleaf.
 *
 * <p><strong>Por que esta classe existe?</strong><br>
 * O Thymeleaf 3.1 (padrão no Spring Boot 3.x) removeu o acesso direto a objetos
 * como {@code #request}, {@code #session} e {@code #response} nos templates,
 * por questões de segurança (evita vazamento acidental de informações sensíveis).
 *
 * <p>A solução recomendada é expor apenas o que for necessário via {@code @ModelAttribute},
 * deixando o controller responsável por decidir o que o template pode acessar.
 *
 * <p><strong>{@code @ControllerAdvice}:</strong><br>
 * Permite aplicar comportamentos transversais (cross-cutting concerns) a múltiplos
 * controllers sem duplicar código. Métodos anotados com {@code @ModelAttribute} aqui
 * são executados antes de QUALQUER método de qualquer controller, adicionando atributos
 * automaticamente ao modelo.
 *
 * <p>Uso no template Thymeleaf:
 * <pre>
 *   {@code th:classappend="${requestURI.startsWith('/flashcards')} ? 'active'"}
 * </pre>
 *
 * @author DSC - UFPB Campus IV
 */
@ControllerAdvice
public class GlobalModelAttributes {

    /** URL do script do Umami (vazia = analytics desligado). */
    @Value("${studyai.umami.src:}")
    private String umamiSrc;

    /** Identificador do site no Umami (vazio = analytics desligado). */
    @Value("${studyai.umami.website-id:}")
    private String umamiWebsiteId;

    /**
     * Disponibiliza a URI da requisição atual para todos os templates.
     *
     * <p>Utilizado pelo layout para marcar o item de menu ativo na navbar.
     *
     * @param request objeto da requisição HTTP injetado pelo Spring
     * @return URI da requisição atual (ex.: "/", "/flashcards")
     */
    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * URL do script do Umami, para o layout injetar o snippet de analytics.
     *
     * @return src configurado, ou string vazia quando o Umami está desligado
     */
    @ModelAttribute("umamiSrc")
    public String umamiSrc() {
        return umamiSrc;
    }

    /**
     * Identificador do site no Umami. O layout só injeta o script quando este valor
     * está presente, então a página funciona normalmente sem analytics configurado.
     *
     * @return website-id configurado, ou string vazia quando o Umami está desligado
     */
    @ModelAttribute("umamiWebsiteId")
    public String umamiWebsiteId() {
        return umamiWebsiteId;
    }
}
