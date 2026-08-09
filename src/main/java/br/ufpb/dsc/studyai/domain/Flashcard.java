package br.ufpb.dsc.studyai.domain;

import jakarta.persistence.*;

/**
 * Entidade JPA que representa um <strong>flashcard</strong> (cartão de memorização).
 *
 * <p>Cada cartão pertence a um {@link Deck} e tem duas faces:
 * <ul>
 *   <li>{@code frente} — a pergunta;</li>
 *   <li>{@code verso} — a resposta.</li>
 * </ul>
 *
 * <p>Este é o lado "dono" da relação 1→N: a coluna {@code deck_id} vive nesta tabela.
 *
 * @author DSC - UFPB Campus IV
 */
@Entity
@Table(name = "flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Deck ao qual o cartão pertence.
     *
     * <p>{@code FetchType.LAZY} evita carregar o deck (e potencialmente todos os seus
     * cartões) sempre que um flashcard é lido.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(name = "frente", nullable = false, columnDefinition = "TEXT")
    private String frente;

    @Column(name = "verso", nullable = false, columnDefinition = "TEXT")
    private String verso;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    public Flashcard() {
    }

    public Flashcard(String frente, String verso, int ordem) {
        this.frente = frente;
        this.verso = verso;
        this.ordem = ordem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getFrente() {
        return frente;
    }

    public void setFrente(String frente) {
        this.frente = frente;
    }

    public String getVerso() {
        return verso;
    }

    public void setVerso(String verso) {
        this.verso = verso;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
