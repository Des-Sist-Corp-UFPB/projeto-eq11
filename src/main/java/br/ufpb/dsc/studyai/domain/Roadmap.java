package br.ufpb.dsc.studyai.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA que representa um <strong>roadmap de estudos</strong> para o ENEM.
 *
 * <p>É o resultado do módulo RoadmapIA: o aluno responde um questionário de perfil
 * (período disponível, horas por dia, áreas de dificuldade, curso pretendido) e a IA
 * devolve um plano que cobre integralmente o intervalo {@link #dataInicio} →
 * {@link #dataFim}, organizado em {@link SemanaEstudo blocos semanais}.
 *
 * <p><strong>Relacionamento 1→N:</strong> um roadmap possui várias semanas; cada
 * semana possui várias {@link TarefaEstudo tarefas} diárias.
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "roadmap")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "horas_por_dia", nullable = false)
    private int horasPorDia;

    @Column(name = "curso_alvo", length = 120)
    private String cursoAlvo;

    /** Áreas de maior dificuldade declaradas pelo aluno, separadas por vírgula. */
    @Column(name = "dificuldades", length = 255)
    private String dificuldades;

    @Column(name = "experiencia", length = 120)
    private String experiencia;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    /** Visão geral da estratégia, escrita pela IA. */
    @Column(name = "resumo_estrategia", columnDefinition = "TEXT")
    private String resumoEstrategia;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    /**
     * Semanas do plano.
     *
     * <p>{@code cascade = ALL} + {@code orphanRemoval = true}: ao salvar o roadmap, as
     * semanas (e, em cascata, suas tarefas) são persistidas juntas.
     */
    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<SemanaEstudo> semanas = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = Instant.now();
        }
    }

    public Roadmap() {
    }

    public Roadmap(String titulo, LocalDate dataInicio, LocalDate dataFim, int horasPorDia) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horasPorDia = horasPorDia;
    }

    /**
     * Adiciona uma semana mantendo os dois lados da relação sincronizados.
     *
     * @param semana bloco semanal a associar ao roadmap
     */
    public void adicionarSemana(SemanaEstudo semana) {
        semana.setRoadmap(this);
        this.semanas.add(semana);
    }

    /**
     * Total de tarefas do plano — usado nas listagens e no cabeçalho do resultado.
     *
     * @return soma das tarefas de todas as semanas
     */
    public int getTotalTarefas() {
        return semanas.stream().mapToInt(s -> s.getTarefas().size()).sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public int getHorasPorDia() {
        return horasPorDia;
    }

    public void setHorasPorDia(int horasPorDia) {
        this.horasPorDia = horasPorDia;
    }

    public String getCursoAlvo() {
        return cursoAlvo;
    }

    public void setCursoAlvo(String cursoAlvo) {
        this.cursoAlvo = cursoAlvo;
    }

    public String getDificuldades() {
        return dificuldades;
    }

    public void setDificuldades(String dificuldades) {
        this.dificuldades = dificuldades;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getResumoEstrategia() {
        return resumoEstrategia;
    }

    public void setResumoEstrategia(String resumoEstrategia) {
        this.resumoEstrategia = resumoEstrategia;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<SemanaEstudo> getSemanas() {
        return semanas;
    }

    public void setSemanas(List<SemanaEstudo> semanas) {
        this.semanas = semanas;
    }
}
