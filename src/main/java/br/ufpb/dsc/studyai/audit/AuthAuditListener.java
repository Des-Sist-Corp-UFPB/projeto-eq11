package br.ufpb.dsc.studyai.audit;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Ouve eventos de autenticação do Spring Security e os registra na auditoria.
 *
 * <p><strong>Por que um listener (e não uma chamada no controller)?</strong><br>
 * O {@code POST /login} é processado <em>internamente</em> pelo Spring Security
 * (não há método de controller para ele). O caminho idiomático e de baixo acoplamento
 * é reagir aos eventos publicados pelo framework:
 * <ul>
 *   <li>{@link AuthenticationSuccessEvent} — login bem-sucedido;</li>
 *   <li>{@link AbstractAuthenticationFailureEvent} — tentativa de login que falhou.</li>
 * </ul>
 *
 * <p>O <strong>logout</strong> não gera um evento de aplicação por padrão; ele é
 * auditado pelo {@link AuditLogoutHandler}, registrado na cadeia de logout.
 *
 * @author DSC - UFPB Campus IV
 */
@Component
public class AuthAuditListener {

    private final AuditLogService auditLogService;

    public AuthAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Registra um login bem-sucedido.
     *
     * @param event evento publicado pelo Spring Security ao autenticar
     */
    @EventListener
    public void aoAutenticarComSucesso(AuthenticationSuccessEvent event) {
        auditLogService.registrarAutenticacao(event.getAuthentication().getName(), "LOGIN");
    }

    /**
     * Registra uma tentativa de login que falhou (ex.: senha incorreta).
     *
     * @param event evento de falha publicado pelo Spring Security
     */
    @EventListener
    public void aoFalharAutenticacao(AbstractAuthenticationFailureEvent event) {
        auditLogService.registrarAutenticacao(event.getAuthentication().getName(), "LOGIN_FALHA");
    }
}
