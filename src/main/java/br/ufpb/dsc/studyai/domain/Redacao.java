package br.ufpb.dsc.studyai.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "redacao")
public class Redacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String banca;

    @Column(nullable = false, length = 255)
    private String tema;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "nota_total", nullable = false)
    private Double notaTotal;

    @Column(name = "comentario_geral", columnDefinition = "TEXT")
    private String comentarioGeral;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "redacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Criterio> criterios = new ArrayList<>();

    public Redacao() {
    }

    public Redacao(String banca, String tema, String texto, Double notaTotal, String comentarioGeral) {
        this.banca = banca;
        this.tema = tema;
        this.texto = texto;
        this.notaTotal = notaTotal;
        this.comentarioGeral = comentarioGeral;
    }

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public void adicionarCriterio(Criterio criterio) {
        criterio.setRedacao(this);
        this.criterios.add(criterio);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBanca() {
        return banca;
    }

    public void setBanca(String banca) {
        this.banca = banca;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Double getNotaTotal() {
        return notaTotal;
    }

    public void setNotaTotal(Double notaTotal) {
        this.notaTotal = notaTotal;
    }

    public String getComentarioGeral() {
        return comentarioGeral;
    }

    public void setComentarioGeral(String comentarioGeral) {
        this.comentarioGeral = comentarioGeral;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<Criterio> getCriterios() {
        return criterios;
    }

    public void setCriterios(List<Criterio> criterios) {
        this.criterios = criterios;
    }
}
