package br.ufpb.dsc.studyai.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Registra o <strong>logout</strong> na auditoria.
 *
 * <p>O Spring Security não publica um evento de aplicação para logout, então
 * registramos um {@link LogoutHandler} na cadeia de logout. Ele é invocado
 * <em>antes</em> de o contexto de segurança ser limpo — por isso a
 * {@link Authentication} ainda está disponível aqui.
 *
 * <p>É <strong>aditivo</strong>: roda em conjunto com o handler padrão de sucesso de
 * logout, sem alterar o redirecionamento ({@code logoutSuccessUrl}) configurado no
 * {@code SecurityConfig}.
 *
 * @author DSC - UFPB Campus IV
 */
@Component
public class AuditLogoutHandler implements LogoutHandler {

    private final AuditLogService auditLogService;

    public AuditLogoutHandler(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // authentication é nulo se o logout for chamado sem sessão autenticada.
        if (authentication != null) {
            auditLogService.registrarAutenticacao(authentication.getName(), "LOGOUT");
        }
    }
}
