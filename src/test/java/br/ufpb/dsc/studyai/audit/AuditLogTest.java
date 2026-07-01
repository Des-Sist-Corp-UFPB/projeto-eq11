package br.ufpb.dsc.studyai.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários da entidade {@link AuditLog} — construtores, {@code prePersist}
 * e acessores.
 */
class AuditLogTest {

    @Test
    void construtorComArgumentos_definirCampos() {
        AuditLog log = new AuditLog("admin", "GERAR_FLASHCARD", "deck", 5L, "2 cartões", "10.0.0.1");

        assertThat(log.getUsuario()).isEqualTo("admin");
        assertThat(log.getAcao()).isEqualTo("GERAR_FLASHCARD");
        assertThat(log.getEntidade()).isEqualTo("deck");
        assertThat(log.getEntidadeId()).isEqualTo(5L);
        assertThat(log.getDetalhes()).isEqualTo("2 cartões");
        assertThat(log.getIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void prePersist_definirDataHoraQuandoNula() {
        AuditLog log = new AuditLog();
        assertThat(log.getDataHora()).isNull();

        log.prePersist();

        assertThat(log.getDataHora()).isNotNull();
    }

    @Test
    void prePersist_naoSobrescreverDataHoraExistente() {
        AuditLog log = new AuditLog();
        Instant fixo = Instant.parse("2026-01-01T00:00:00Z");
        log.setDataHora(fixo);

        log.prePersist();

        assertThat(log.getDataHora()).isEqualTo(fixo);
    }

    @Test
    void settersEgetters_funcionam() {
        AuditLog log = new AuditLog();
        log.setId(3L);
        log.setUsuario("jean");
        log.setAcao("LOGIN");
        log.setEntidade("deck");
        log.setEntidadeId(8L);
        log.setDetalhes("detalhe");
        log.setIp("203.0.113.5");

        assertThat(log.getId()).isEqualTo(3L);
        assertThat(log.getUsuario()).isEqualTo("jean");
        assertThat(log.getAcao()).isEqualTo("LOGIN");
        assertThat(log.getEntidade()).isEqualTo("deck");
        assertThat(log.getEntidadeId()).isEqualTo(8L);
        assertThat(log.getDetalhes()).isEqualTo("detalhe");
        assertThat(log.getIp()).isEqualTo("203.0.113.5");
    }
}
