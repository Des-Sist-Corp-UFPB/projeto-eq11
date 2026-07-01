package br.ufpb.dsc.studyai.audit;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entidade JPA que representa um registro de <strong>log de auditoria</strong>.
 *
 * <p>Cada linha guarda uma ação relevante de usuário (login, logout, geração de
 * flashcard), com quem fez, quando, de qual IP e detalhes livres. É um registro
 * <em>append-only</em>: nunca é alterado nem apagado pela aplicação.
 *
 * <p>O schema é controlado pelo Flyway (migração {@code V4__audit_log.sql}); aqui
 * apenas mapeamos as colunas, sem deixar o Hibernate alterar o banco
 * ({@code ddl-auto: validate}).
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuário autenticado que realizou a ação (ou {@code "anonimo"} se não houver). */
    @Column(name = "usuario", length = 120)
    private String usuario;

    /** Ação realizada, em caixa alta (ex.: {@code LOGIN}, {@code LOGOUT}, {@code GERAR_FLASHCARD}). */
    @Column(name = "acao", nullable = false, length = 60)
    private String acao;

    /** Entidade afetada, quando aplicável (ex.: {@code deck}). Pode ser nulo. */
    @Column(name = "entidade", length = 60)
    private String entidade;

    /** Identificador da entidade afetada, quando aplicável. Pode ser nulo. */
    @Column(name = "entidade_id")
    private Long entidadeId;

    /** Detalhes livres da ação (ex.: quantidade de cartões, banca). Pode ser nulo. */
    @Column(name = "detalhes", columnDefinition = "TEXT")
    private String detalhes;

    /** IP de origem da requisição (suporta IPv6, por isso 45 caracteres). Pode ser nulo. */
    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private Instant dataHora;

    @PrePersist
    protected void prePersist() {
        if (this.dataHora == null) {
            this.dataHora = Instant.now();
        }
    }

    public AuditLog() {
    }

    public AuditLog(String usuario, String acao, String entidade, Long entidadeId, String detalhes, String ip) {
        this.usuario = usuario;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhes = detalhes;
        this.ip = ip;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(Long entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }
}
