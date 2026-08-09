package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de {@link Redacao} e {@link Criterio} — construtores, sincronização
 * da relação e o {@code onCreate}.
 */
class RedacaoTest {

    @Test
    void construtor_definirCampos() {
        Redacao r = new Redacao("ENEM", "Tema X", "Texto...", 850.0, "Bom texto");

        assertThat(r.getBanca()).isEqualTo("ENEM");
        assertThat(r.getTema()).isEqualTo("Tema X");
        assertThat(r.getTexto()).isEqualTo("Texto...");
        assertThat(r.getNotaTotal()).isEqualTo(850.0);
        assertThat(r.getComentarioGeral()).isEqualTo("Bom texto");
        assertThat(r.getCriterios()).isEmpty();
    }

    @Test
    void adicionarCriterio_sincronizaOsDoisLadosDaRelacao() {
        Redacao r = new Redacao("ENEM", "T", "txt", 0.0, "");
        Criterio c = new Criterio("Competência 1", 160.0, "ok");

        r.adicionarCriterio(c);

        assertThat(r.getCriterios()).containsExactly(c);
        assertThat(c.getRedacao()).isSameAs(r);
    }

    @Test
    void onCreate_definirCriadoEmApenasQuandoNulo() {
        Redacao r = new Redacao();
        assertThat(r.getCriadoEm()).isNull();

        r.onCreate();
        assertThat(r.getCriadoEm()).isNotNull();

        LocalDateTime fixo = LocalDateTime.of(2026, 1, 1, 0, 0);
        r.setCriadoEm(fixo);
        r.onCreate();
        assertThat(r.getCriadoEm()).isEqualTo(fixo);
    }

    @Test
    void redacao_gettersEsetters_funcionam() {
        Redacao r = new Redacao();
        Usuario dono = new Usuario();
        r.setId(2L);
        r.setBanca("Cebraspe");
        r.setTema("Educação");
        r.setTexto("...");
        r.setNotaTotal(90.0);
        r.setComentarioGeral("c");
        r.setUsuario(dono);

        assertThat(r.getId()).isEqualTo(2L);
        assertThat(r.getBanca()).isEqualTo("Cebraspe");
        assertThat(r.getTema()).isEqualTo("Educação");
        assertThat(r.getNotaTotal()).isEqualTo(90.0);
        assertThat(r.getComentarioGeral()).isEqualTo("c");
        assertThat(r.getUsuario()).isSameAs(dono);
    }

    @Test
    void criterio_construtorEacessores_funcionam() {
        Criterio c = new Criterio("Gramática", 180.0, "quase perfeito");
        assertThat(c.getNome()).isEqualTo("Gramática");
        assertThat(c.getNota()).isEqualTo(180.0);
        assertThat(c.getComentario()).isEqualTo("quase perfeito");

        Criterio vazio = new Criterio();
        vazio.setId(4L);
        vazio.setNome("Coesão");
        vazio.setNota(120.0);
        vazio.setComentario("repetição de conectivos");
        vazio.onCreate();

        assertThat(vazio.getId()).isEqualTo(4L);
        assertThat(vazio.getNome()).isEqualTo("Coesão");
        assertThat(vazio.getNota()).isEqualTo(120.0);
        assertThat(vazio.getComentario()).isEqualTo("repetição de conectivos");
        assertThat(vazio.getCriadoEm()).isNotNull();
    }
}
