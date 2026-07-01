package br.ufpb.dsc.studyai.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes da lógica de {@link IAProperties} — especialmente {@link IAProperties#isDemo()},
 * que decide se a aplicação chama a IA real ou devolve exemplos.
 */
class IAPropertiesTest {

    @Test
    void isDemo_verdadeiroQuandoModoDemo() {
        IAProperties props = new IAProperties();
        props.setModo("demo");
        props.setApiKey("uma-chave-qualquer");

        assertThat(props.isDemo()).isTrue();
    }

    @Test
    void isDemo_verdadeiroQuandoSemChave_aindaQueModoReal() {
        IAProperties props = new IAProperties();
        props.setModo("real");
        props.setApiKey("");

        // Sem chave configurada, força o modo demo (garante a demo sem internet/chave).
        assertThat(props.isDemo()).isTrue();
    }

    @Test
    void isDemo_falsoQuandoModoRealComChave() {
        IAProperties props = new IAProperties();
        props.setModo("real");
        props.setApiKey("sk-teste");

        assertThat(props.isDemo()).isFalse();
    }

    @Test
    void acessores_funcionam() {
        IAProperties props = new IAProperties();
        props.setModo("real");
        props.setProvedor("gemini");
        props.setApiKey("k");
        props.setModelo("gemini-2.0");
        props.setTimeoutSegundos(30);

        assertThat(props.getModo()).isEqualTo("real");
        assertThat(props.getProvedor()).isEqualTo("gemini");
        assertThat(props.getApiKey()).isEqualTo("k");
        assertThat(props.getModelo()).isEqualTo("gemini-2.0");
        assertThat(props.getTimeoutSegundos()).isEqualTo(30);
    }

    @Test
    void valoresPadrao_saoDemoEAnthropic() {
        IAProperties props = new IAProperties();

        assertThat(props.getModo()).isEqualTo("demo");
        assertThat(props.getProvedor()).isEqualTo("anthropic");
        assertThat(props.getApiKey()).isEmpty();
        assertThat(props.getTimeoutSegundos()).isEqualTo(45);
        assertThat(props.isDemo()).isTrue();
    }
}
