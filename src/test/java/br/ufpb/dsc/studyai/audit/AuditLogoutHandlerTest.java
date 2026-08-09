package br.ufpb.dsc.studyai.audit;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Testa que {@link AuditLogoutHandler} registra o logout quando há usuário autenticado
 * e não faz nada quando não há.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogoutHandlerTest {

    @Mock
    private AuditLogService auditLogService;

    @Test
    void logout_comAutenticacao_registraLogout() {
        AuditLogoutHandler handler = new AuditLogoutHandler(auditLogService);
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "x",
                AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        handler.logout(new MockHttpServletRequest(), mock(HttpServletResponse.class), auth);

        verify(auditLogService).registrarAutenticacao("admin", "LOGOUT");
    }

    @Test
    void logout_semAutenticacao_naoRegistraNada() {
        AuditLogoutHandler handler = new AuditLogoutHandler(auditLogService);

        handler.logout(new MockHttpServletRequest(), mock(HttpServletResponse.class), null);

        verifyNoInteractions(auditLogService);
    }
}
