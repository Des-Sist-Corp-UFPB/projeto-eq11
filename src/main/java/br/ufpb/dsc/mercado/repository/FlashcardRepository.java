package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório Spring Data JPA para {@link Flashcard}.
 *
 * <p>Normalmente os flashcards são acessados através do {@link Deck} (cascade),
 * mas este repositório expõe {@code count()} para as estatísticas da home.
 *
 * @author DSC - UFPB Campus IV
 */
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
}
