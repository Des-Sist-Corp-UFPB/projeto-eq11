package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.service.UsuarioService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Testa que {@link AuthController} serve a página de login customizada.
 */
class AuthControllerTest {

    @Test
    void login_retornaTemplateDeLogin() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        assertThat(new AuthController(usuarioService).login()).isEqualTo("auth/login");
    }
}
