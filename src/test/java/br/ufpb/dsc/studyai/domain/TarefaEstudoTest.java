package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de {@link TarefaEstudo} — construtor, acessores e, principalmente,
 * a formatação de duração ({@code getDuracaoFormatada}).
 */
class TarefaEstudoTest {

    @Test
    void construtor_definirCampos() {
        TarefaEstudo t = new TarefaEstudo(LocalDate.of(2026, 8, 10), "Funções", "Teoria + exercícios", 90, 2);

        assertThat(t.getData()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(t.getAssunto()).isEqualTo("Funções");
        assertThat(t.getDescricao()).isEqualTo("Teoria + exercícios");
        assertThat(t.getDuracaoMinutos()).isEqualTo(90);
        assertThat(t.getOrdem()).isEqualTo(2);
    }

    @Test
    void getDuracaoFormatada_menosDeUmaHora_mostraApenasMinutos() {
        assertThat(new TarefaEstudo(LocalDate.now(), "a", "d", 45, 0).getDuracaoFormatada()).isEqualTo("45min");
        assertThat(new TarefaEstudo(LocalDate.now(), "a", "d", 15, 0).getDuracaoFormatada()).isEqualTo("15min");
    }

    @Test
    void getDuracaoFormatada_horaExata_omiteOsMinutos() {
        assertThat(new TarefaEstudo(LocalDate.now(), "a", "d", 60, 0).getDuracaoFormatada()).isEqualTo("1h");
        assertThat(new TarefaEstudo(LocalDate.now(), "a", "d", 120, 0).getDuracaoFormatada()).isEqualTo("2h");
    }

    @Test
    void getDuracaoFormatada_horaComMinutos_usaDoisDigitos() {
        assertThat(new TarefaEstudo(LocalDate.now(), "a", "d", 90, 0).getDuracaoFormatada()).isEqualTo("1h30");
        assertThat(new TarefaEstudo(LocalDate.now(), "a", "d", 125, 0).getDuracaoFormatada()).isEqualTo("2h05");
    }

    @Test
    void gettersEsetters_funcionam() {
        TarefaEstudo t = new TarefaEstudo();
        SemanaEstudo s = new SemanaEstudo();

        t.setId(8L);
        t.setSemana(s);
        t.setData(LocalDate.of(2026, 9, 1));
        t.setAssunto("Cinemática");
        t.setDescricao("Resolver questões");
        t.setDuracaoMinutos(75);
        t.setOrdem(3);

        assertThat(t.getId()).isEqualTo(8L);
        assertThat(t.getSemana()).isSameAs(s);
        assertThat(t.getData()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(t.getAssunto()).isEqualTo("Cinemática");
        assertThat(t.getDescricao()).isEqualTo("Resolver questões");
        assertThat(t.getDuracaoMinutos()).isEqualTo(75);
        assertThat(t.getOrdem()).isEqualTo(3);
    }
}
