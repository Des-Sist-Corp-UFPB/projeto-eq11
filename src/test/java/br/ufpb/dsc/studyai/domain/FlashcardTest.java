package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários da entidade {@link Flashcard} (construtor e acessores).
 */
class FlashcardTest {

    @Test
    void construtor_definirCampos() {
        Flashcard card = new Flashcard("Frente", "Verso", 3);

        assertThat(card.getFrente()).isEqualTo("Frente");
        assertThat(card.getVerso()).isEqualTo("Verso");
        assertThat(card.getOrdem()).isEqualTo(3);
    }

    @Test
    void settersEgetters_funcionam() {
        Flashcard card = new Flashcard();
        Deck deck = new Deck("T", "Geral", "");
        card.setId(9L);
        card.setFrente("F");
        card.setVerso("V");
        card.setOrdem(1);
        card.setDeck(deck);

        assertThat(card.getId()).isEqualTo(9L);
        assertThat(card.getFrente()).isEqualTo("F");
        assertThat(card.getVerso()).isEqualTo("V");
        assertThat(card.getOrdem()).isEqualTo(1);
        assertThat(card.getDeck()).isSameAs(deck);
    }
}
