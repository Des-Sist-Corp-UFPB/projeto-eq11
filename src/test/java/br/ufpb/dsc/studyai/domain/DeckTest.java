package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários da entidade {@link Deck} — construtores, sincronização da relação
 * com {@link Flashcard} e o {@code prePersist}.
 */
class DeckTest {

    @Test
    void construtor_definirCampos() {
        Deck deck = new Deck("Título", "Cebraspe", "Direito");

        assertThat(deck.getTitulo()).isEqualTo("Título");
        assertThat(deck.getBanca()).isEqualTo("Cebraspe");
        assertThat(deck.getDisciplina()).isEqualTo("Direito");
        assertThat(deck.getFlashcards()).isEmpty();
    }

    @Test
    void adicionarFlashcard_sincronizaOsDoisLadosDaRelacao() {
        Deck deck = new Deck("T", "Geral", "");
        Flashcard card = new Flashcard("Pergunta", "Resposta", 0);

        deck.adicionarFlashcard(card);

        assertThat(deck.getFlashcards()).containsExactly(card);
        // O lado "dono" (Flashcard) deve apontar de volta para o deck
        assertThat(card.getDeck()).isSameAs(deck);
    }

    @Test
    void prePersist_definirCriadoEmQuandoNulo() {
        Deck deck = new Deck("T", "Geral", "");
        assertThat(deck.getCriadoEm()).isNull();

        deck.prePersist();

        assertThat(deck.getCriadoEm()).isNotNull();
    }

    @Test
    void prePersist_naoSobrescreverCriadoEmExistente() {
        Deck deck = new Deck("T", "Geral", "");
        Instant fixo = Instant.parse("2026-01-01T00:00:00Z");
        deck.setCriadoEm(fixo);

        deck.prePersist();

        assertThat(deck.getCriadoEm()).isEqualTo(fixo);
    }

    @Test
    void getEsetters_funcionam() {
        Deck deck = new Deck();
        deck.setId(7L);
        deck.setTitulo("X");
        deck.setBanca("FGV");
        deck.setDisciplina("Português");

        assertThat(deck.getId()).isEqualTo(7L);
        assertThat(deck.getTitulo()).isEqualTo("X");
        assertThat(deck.getBanca()).isEqualTo("FGV");
        assertThat(deck.getDisciplina()).isEqualTo("Português");
    }
}
