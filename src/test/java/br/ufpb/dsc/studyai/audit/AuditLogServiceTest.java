package br.ufpb.dsc.studyai.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários de {@link AuditLogService}: resolução de usuário/IP do contexto,
 * fallback para anônimo e robustez (falha ao gravar não propaga).
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository repository;

    @Captor
    private ArgumentCaptor<AuditLog> captor;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private void autenticar(String nome) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(nome, "x",
                        AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    private void definirRequest(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void registrar_resolveUsuarioEIpDoContexto() {
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        autenticar("admin");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.7");
        definirRequest(request);

        new AuditLogService(repository).registrar("GERAR_FLASHCARD", "deck", 42L, "2 cartões");

        AuditLog salvo = captor.getValue();
        assertThat(salvo.getUsuario()).isEqualTo("admin");
        assertThat(salvo.getAcao()).isEqualTo("GERAR_FLASHCARD");
        assertThat(salvo.getEntidade()).isEqualTo("deck");
        assertThat(salvo.getEntidadeId()).isEqualTo(42L);
        assertThat(salvo.getDetalhes()).isEqualTo("2 cartões");
        assertThat(salvo.getIp()).isEqualTo("203.0.113.7");
    }

    @Test
    void registrar_semAutenticacao_usaAnonimo() {
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        new AuditLogService(repository).registrar("ACAO", null, null, null);

        assertThat(captor.getValue().getUsuario()).isEqualTo("anonimo");
        assertThat(captor.getValue().getIp()).isNull(); // sem request no contexto
    }

    @Test
    void registrar_priorizaXForwardedFor() {
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "198.51.100.10, 10.0.0.1");
        request.setRemoteAddr("10.0.0.1");
        definirRequest(request);

        new AuditLogService(repository).registrar("ACAO", null, null, null);

        assertThat(captor.getValue().getIp()).isEqualTo("198.51.100.10");
    }

    @Test
    void registrarAutenticacao_gravaUsuarioEAcao() {
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        new AuditLogService(repository).registrarAutenticacao("jean", "LOGIN");

        assertThat(captor.getValue().getUsuario()).isEqualTo("jean");
        assertThat(captor.getValue().getAcao()).isEqualTo("LOGIN");
    }

    @Test
    void registrarAutenticacao_usuarioVazioViraAnonimo() {
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        new AuditLogService(repository).registrarAutenticacao("  ", "LOGOUT");

        assertThat(captor.getValue().getUsuario()).isEqualTo("anonimo");
    }

    @Test
    void registrar_falhaAoGravarNaoPropaga() {
        doThrow(new RuntimeException("db fora do ar")).when(repository).save(org.mockito.ArgumentMatchers.any());

        // Não deve lançar exceção — a auditoria nunca derruba a operação principal.
        new AuditLogService(repository).registrar("ACAO", null, null, null);

        verify(repository).save(org.mockito.ArgumentMatchers.any());
    }
}
