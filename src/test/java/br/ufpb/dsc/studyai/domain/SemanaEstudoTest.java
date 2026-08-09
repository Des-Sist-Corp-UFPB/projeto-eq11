package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de {@link SemanaEstudo} — construtor, sincronização com
 * {@link TarefaEstudo} e a soma da carga horária.
 */
class SemanaEstudoTest {

    @Test
    void construtor_definirCampos() {
        SemanaEstudo s = new SemanaEstudo(2, LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 23), "Funções");

        assertThat(s.getNumero()).isEqualTo(2);
        assertThat(s.getDataInicio()).isEqualTo(LocalDate.of(2026, 8, 17));
        assertThat(s.getDataFim()).isEqualTo(LocalDate.of(2026, 8, 23));
        assertThat(s.getFoco()).isEqualTo("Funções");
        assertThat(s.getTarefas()).isEmpty();
    }

    @Test
    void adicionarTarefa_sincronizaOsDoisLadosDaRelacao() {
        SemanaEstudo s = new SemanaEstudo(1, LocalDate.now(), LocalDate.now().plusDays(6), "A");
        TarefaEstudo t = new TarefaEstudo(LocalDate.now(), "Assunto", "desc", 90, 0);

        s.adicionarTarefa(t);

        assertThat(s.getTarefas()).containsExactly(t);
        assertThat(t.getSemana()).isSameAs(s);
    }

    @Test
    void getTotalMinutos_somaAsDuracoesDasTarefas() {
        SemanaEstudo s = new SemanaEstudo(1, LocalDate.now(), LocalDate.now().plusDays(6), "A");
        s.adicionarTarefa(new TarefaEstudo(LocalDate.now(), "T1", "d", 90, 0));
        s.adicionarTarefa(new TarefaEstudo(LocalDate.now(), "T2", "d", 30, 1));

        assertThat(s.getTotalMinutos()).isEqualTo(120);
    }

    @Test
    void gettersEsetters_funcionam() {
        SemanaEstudo s = new SemanaEstudo();
        Roadmap r = new Roadmap();

        s.setId(3L);
        s.setRoadmap(r);
        s.setNumero(4);
        s.setDataInicio(LocalDate.of(2026, 9, 1));
        s.setDataFim(LocalDate.of(2026, 9, 7));
        s.setFoco("Química");

        assertThat(s.getId()).isEqualTo(3L);
        assertThat(s.getRoadmap()).isSameAs(r);
        assertThat(s.getNumero()).isEqualTo(4);
        assertThat(s.getDataInicio()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(s.getDataFim()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(s.getFoco()).isEqualTo("Química");
    }
}
