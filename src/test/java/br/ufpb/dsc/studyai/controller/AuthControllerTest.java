package br.ufpb.dsc.studyai.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa que {@link AuthController} serve a página de login customizada.
 */
class AuthControllerTest {

    @Test
    void login_retornaTemplateDeLogin() {
        assertThat(new AuthController().login()).isEqualTo("auth/login");
    }
}
