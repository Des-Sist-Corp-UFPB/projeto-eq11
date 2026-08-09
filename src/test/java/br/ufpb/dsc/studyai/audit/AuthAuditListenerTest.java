package br.ufpb.dsc.studyai.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.mockito.Mockito.verify;

/**
 * Testa que {@link AuthAuditListener} traduz eventos do Spring Security em registros
 * de auditoria (login com sucesso e falha de login).
 */
@ExtendWith(MockitoExtension.class)
class AuthAuditListenerTest {

    @Mock
    private AuditLogService auditLogService;

    private Authentication auth(String nome) {
        return new UsernamePasswordAuthenticationToken(nome, "senha",
                AuthorityUtils.createAuthorityList("ROLE_USER"));
    }

    @Test
    void loginComSucesso_registraLogin() {
        AuthAuditListener listener = new AuthAuditListener(auditLogService);

        listener.aoAutenticarComSucesso(new AuthenticationSuccessEvent(auth("admin")));

        verify(auditLogService).registrarAutenticacao("admin", "LOGIN");
    }

    @Test
    void falhaDeLogin_registraLoginFalha() {
        AuthAuditListener listener = new AuthAuditListener(auditLogService);
        var evento = new AuthenticationFailureBadCredentialsEvent(
                auth("intruso"), new BadCredentialsException("senha errada"));

        listener.aoFalharAutenticacao(evento);

        verify(auditLogService).registrarAutenticacao("intruso", "LOGIN_FALHA");
    }
}
