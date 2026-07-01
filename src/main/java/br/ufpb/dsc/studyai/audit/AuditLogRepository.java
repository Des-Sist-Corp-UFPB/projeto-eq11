package br.ufpb.dsc.studyai.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data JPA para {@link AuditLog}.
 *
 * <p>Além do CRUD herdado de {@code JpaRepository}, expõe uma consulta derivada
 * para listar os registros mais recentes primeiro (útil para uma futura tela de
 * auditoria).
 *
 * @author DSC - UFPB Campus IV
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Lista os registros de auditoria do mais recente para o mais antigo.
     *
     * @return registros ordenados por {@code dataHora} decrescente
     */
    List<AuditLog> findAllByOrderByDataHoraDesc();
}
