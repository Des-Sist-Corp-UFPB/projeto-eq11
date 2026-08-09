package br.ufpb.dsc.studyai.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa os dois construtores de {@link IAIndisponivelException}.
 */
class IAIndisponivelExceptionTest {

    @Test
    void construtorComMensagem() {
        IAIndisponivelException ex = new IAIndisponivelException("falhou");

        assertThat(ex).hasMessage("falhou");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void construtorComMensagemECausa() {
        Throwable causa = new RuntimeException("raiz");
        IAIndisponivelException ex = new IAIndisponivelException("falhou", causa);

        assertThat(ex).hasMessage("falhou");
        assertThat(ex.getCause()).isSameAs(causa);
    }
}
