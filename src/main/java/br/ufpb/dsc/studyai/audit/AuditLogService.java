package br.ufpb.dsc.studyai.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Serviço dedicado de <strong>log de auditoria</strong>.
 *
 * <p>Centraliza a gravação de {@link AuditLog}, resolvendo automaticamente:
 * <ul>
 *   <li><strong>usuário</strong> — do {@link SecurityContextHolder} (ou {@code "anonimo"});</li>
 *   <li><strong>IP</strong> — da requisição HTTP atual ({@link RequestContextHolder}),
 *       respeitando o cabeçalho {@code X-Forwarded-For} quando atrás de proxy.</li>
 * </ul>
 *
 * <p>É chamado explicitamente nos pontos de negócio (ex.: geração de flashcard no
 * {@code FlashcardController}) e a partir do {@link AuthAuditListener} para os eventos
 * de login/logout do Spring Security. Para o escopo atual (poucas ações de usuário),
 * um service dedicado é mais simples e rastreável do que AOP/interceptor.
 *
 * <p>A auditoria <strong>nunca</strong> deve derrubar a operação principal: falhas ao
 * gravar o log são apenas registradas (warn), não propagadas.
 *
 * @author DSC - UFPB Campus IV
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    /** Valor usado quando não há usuário autenticado no contexto. */
    static final String USUARIO_ANONIMO = "anonimo";

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra uma ação de negócio, resolvendo usuário e IP do contexto atual.
     *
     * @param acao       ação realizada (ex.: {@code "GERAR_FLASHCARD"})
     * @param entidade   entidade afetada (ex.: {@code "deck"}), ou {@code null}
     * @param entidadeId id da entidade afetada, ou {@code null}
     * @param detalhes   detalhes livres, ou {@code null}
     */
    @Transactional
    public void registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        gravar(usuarioAtual(), acao, entidade, entidadeId, detalhes);
    }

    /**
     * Registra um evento de autenticação (login/logout), em que o usuário já é
     * conhecido pelo evento do Spring Security.
     *
     * @param usuario nome do usuário autenticado
     * @param acao    ação ({@code "LOGIN"} ou {@code "LOGOUT"})
     */
    @Transactional
    public void registrarAutenticacao(String usuario, String acao) {
        String nome = StringUtils.hasText(usuario) ? usuario : USUARIO_ANONIMO;
        gravar(nome, acao, null, null, null);
    }

    private void gravar(String usuario, String acao, String entidade, Long entidadeId, String detalhes) {
        try {
            AuditLog registro = new AuditLog(usuario, acao, entidade, entidadeId, detalhes, ipAtual());
            repository.save(registro);
            log.debug("Auditoria registrada: usuario={} acao={} entidade={} id={}", usuario, acao, entidade, entidadeId);
        } catch (Exception e) {
            // Auditoria não pode quebrar a operação principal — apenas registra a falha.
            log.warn("Falha ao gravar log de auditoria (acao={}): {}", acao, e.getMessage());
        }
    }

    /**
     * Obtém o nome do usuário autenticado do contexto de segurança.
     *
     * @return nome do usuário, ou {@link #USUARIO_ANONIMO} se não autenticado
     */
    private String usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return USUARIO_ANONIMO;
        }
        return auth.getName();
    }

    /**
     * Obtém o IP de origem da requisição HTTP atual, se houver uma em curso.
     *
     * <p>Prioriza o primeiro IP de {@code X-Forwarded-For} (proxy/reverso) e cai para
     * {@code getRemoteAddr()}. Fora de um contexto web (ex.: testes), devolve {@code null}.
     *
     * @return IP de origem, ou {@code null} se indisponível
     */
    private String ipAtual() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            // X-Forwarded-For pode trazer uma lista "cliente, proxy1, proxy2" — o cliente é o primeiro.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
