package br.ufpb.dsc.studyai.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entidade JPA que representa uma <strong>tarefa diária</strong> do roadmap de estudos.
 *
 * <p>Cada tarefa pertence a uma {@link SemanaEstudo} e tem uma data concreta dentro do
 * intervalo daquela semana — o serviço valida isso antes de persistir, para que a IA
 * não consiga colocar uma tarefa fora do período escolhido pelo aluno.
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "roadmap_tarefa")
public class TarefaEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semana_id", nullable = false)
    private SemanaEstudo semana;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "assunto", nullable = false, length = 160)
    private String assunto;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    public TarefaEstudo() {
    }

    public TarefaEstudo(LocalDate data, String assunto, String descricao, int duracaoMinutos, int ordem) {
        this.data = data;
        this.assunto = assunto;
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.ordem = ordem;
    }

    /**
     * Duração formatada para exibição (ex.: {@code "1h30"}, {@code "45min"}).
     *
     * @return duração legível da tarefa
     */
    public String getDuracaoFormatada() {
        int horas = duracaoMinutos / 60;
        int minutos = duracaoMinutos % 60;
        if (horas == 0) {
            return minutos + "min";
        }
        return minutos == 0 ? horas + "h" : horas + "h" + String.format("%02d", minutos);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SemanaEstudo getSemana() {
        return semana;
    }

    public void setSemana(SemanaEstudo semana) {
        this.semana = semana;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(int duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
