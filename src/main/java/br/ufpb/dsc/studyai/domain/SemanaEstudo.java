package br.ufpb.dsc.studyai.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA que representa um <strong>bloco semanal</strong> do roadmap de estudos.
 *
 * <p>As datas de início e fim de cada semana são calculadas pelo servidor a partir do
 * intervalo escolhido pelo aluno (nunca pela IA), garantindo que o plano cubra
 * exatamente o período pedido. A IA define apenas o {@link #foco} e as tarefas.
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "roadmap_semana")
public class SemanaEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    /** Posição da semana no plano, começando em 1. */
    @Column(name = "numero", nullable = false)
    private int numero;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    /** Tema principal da semana definido pela IA (ex.: "Funções e Geometria Plana"). */
    @Column(name = "foco", length = 160)
    private String foco;

    @OneToMany(mappedBy = "semana", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<TarefaEstudo> tarefas = new ArrayList<>();

    public SemanaEstudo() {
    }

    public SemanaEstudo(int numero, LocalDate dataInicio, LocalDate dataFim, String foco) {
        this.numero = numero;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.foco = foco;
    }

    /**
     * Adiciona uma tarefa mantendo os dois lados da relação sincronizados.
     *
     * @param tarefa tarefa diária a associar à semana
     */
    public void adicionarTarefa(TarefaEstudo tarefa) {
        tarefa.setSemana(this);
        this.tarefas.add(tarefa);
    }

    /**
     * Carga horária total planejada para a semana.
     *
     * @return soma das durações das tarefas, em minutos
     */
    public int getTotalMinutos() {
        return tarefas.stream().mapToInt(TarefaEstudo::getDuracaoMinutos).sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(Roadmap roadmap) {
        this.roadmap = roadmap;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
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

    public String getFoco() {
        return foco;
    }

    public void setFoco(String foco) {
        this.foco = foco;
    }

    public List<TarefaEstudo> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<TarefaEstudo> tarefas) {
        this.tarefas = tarefas;
    }
}
