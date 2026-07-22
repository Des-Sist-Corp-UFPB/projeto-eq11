package br.ufpb.dsc.studyai.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA que representa um <strong>deck</strong> de flashcards.
 *
 * <p>Um deck é o resultado de uma geração do módulo FlashIA: a partir de um texto,
 * a IA produz vários cartões ({@link Flashcard}) que ficam agrupados aqui.
 *
 * <p><strong>Relacionamento 1→N:</strong> um deck possui muitos flashcards.
 * O lado "dono" da relação é o {@link Flashcard} (que carrega a coluna {@code deck_id});
 * aqui usamos {@code mappedBy = "deck"} para indicar isso.
 *
 * <p>Nesta entrega o deck ainda não tem dono (sem entidade de usuário).
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "deck")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "banca", length = 60)
    private String banca;

    @Column(name = "disciplina", length = 80)
    private String disciplina;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Cartões do deck.
     *
     * <p>{@code cascade = ALL} + {@code orphanRemoval = true}: ao salvar o deck,
     * os flashcards associados são persistidos juntos; ao remover um flashcard da
     * lista, ele é apagado do banco.
     *
     * <p>{@code @OrderBy("ordem")} garante que a lista venha ordenada pela posição.
     */
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<Flashcard> flashcards = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = Instant.now();
        }
    }

    public Deck() {
    }

    public Deck(String titulo, String banca, String disciplina) {
        this.titulo = titulo;
        this.banca = banca;
        this.disciplina = disciplina;
    }

    /**
     * Adiciona um flashcard mantendo os dois lados da relação sincronizados.
     *
     * @param flashcard cartão a associar ao deck
     */
    public void adicionarFlashcard(Flashcard flashcard) {
        flashcard.setDeck(this);
        this.flashcards.add(flashcard);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getBanca() {
        return banca;
    }

    public void setBanca(String banca) {
        this.banca = banca;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<Flashcard> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
