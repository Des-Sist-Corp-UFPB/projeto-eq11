package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários da entidade {@link Roadmap} — construtor, sincronização da relação
 * com {@link SemanaEstudo}, contagem de tarefas e o {@code prePersist}.
 */
class RoadmapTest {

    @Test
    void construtor_definirCampos() {
        Roadmap r = new Roadmap("Plano ENEM", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 11, 5), 3);

        assertThat(r.getTitulo()).isEqualTo("Plano ENEM");
        assertThat(r.getDataInicio()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(r.getDataFim()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(r.getHorasPorDia()).isEqualTo(3);
        assertThat(r.getSemanas()).isEmpty();
    }

    @Test
    void adicionarSemana_sincronizaOsDoisLadosDaRelacao() {
        Roadmap r = new Roadmap("P", LocalDate.now(), LocalDate.now().plusDays(6), 2);
        SemanaEstudo semana = new SemanaEstudo(1, LocalDate.now(), LocalDate.now().plusDays(6), "Foco");

        r.adicionarSemana(semana);

        assertThat(r.getSemanas()).containsExactly(semana);
        assertThat(semana.getRoadmap()).isSameAs(r);
    }

    @Test
    void getTotalTarefas_somaAsTarefasDeTodasAsSemanas() {
        Roadmap r = new Roadmap("P", LocalDate.now(), LocalDate.now().plusDays(13), 2);

        SemanaEstudo s1 = new SemanaEstudo(1, LocalDate.now(), LocalDate.now().plusDays(6), "A");
        s1.adicionarTarefa(new TarefaEstudo(LocalDate.now(), "T1", "d", 60, 0));
        s1.adicionarTarefa(new TarefaEstudo(LocalDate.now(), "T2", "d", 60, 1));
        SemanaEstudo s2 = new SemanaEstudo(2, LocalDate.now().plusDays(7), LocalDate.now().plusDays(13), "B");
        s2.adicionarTarefa(new TarefaEstudo(LocalDate.now().plusDays(7), "T3", "d", 60, 0));

        r.adicionarSemana(s1);
        r.adicionarSemana(s2);

        assertThat(r.getTotalTarefas()).isEqualTo(3);
    }

    @Test
    void prePersist_definirCriadoEmApenasQuandoNulo() {
        Roadmap r = new Roadmap("P", LocalDate.now(), LocalDate.now().plusDays(6), 2);
        assertThat(r.getCriadoEm()).isNull();

        r.prePersist();
        assertThat(r.getCriadoEm()).isNotNull();

        Instant fixo = Instant.parse("2026-01-01T00:00:00Z");
        r.setCriadoEm(fixo);
        r.prePersist();
        assertThat(r.getCriadoEm()).isEqualTo(fixo);
    }

    @Test
    void gettersEsetters_funcionam() {
        Roadmap r = new Roadmap();
        Usuario dono = new Usuario();

        r.setId(5L);
        r.setUsuario(dono);
        r.setCursoAlvo("Medicina");
        r.setDificuldades("Matemática, Redação");
        r.setExperiencia("Nunca fiz");
        r.setObservacoes("Só à noite");
        r.setResumoEstrategia("Foco em exatas");

        assertThat(r.getId()).isEqualTo(5L);
        assertThat(r.getUsuario()).isSameAs(dono);
        assertThat(r.getCursoAlvo()).isEqualTo("Medicina");
        assertThat(r.getDificuldades()).isEqualTo("Matemática, Redação");
        assertThat(r.getExperiencia()).isEqualTo("Nunca fiz");
        assertThat(r.getObservacoes()).isEqualTo("Só à noite");
        assertThat(r.getResumoEstrategia()).isEqualTo("Foco em exatas");
    }
}
