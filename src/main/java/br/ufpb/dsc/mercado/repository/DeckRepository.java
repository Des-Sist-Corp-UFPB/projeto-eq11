package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data JPA para {@link Deck}.
 *
 * <p>Ao estender {@code JpaRepository}, ganhamos automaticamente os métodos de CRUD
 * ({@code save}, {@code findById}, {@code findAll}, {@code count}, ...). Métodos de
 * consulta adicionais são derivados pelo nome.
 *
 * @author DSC - UFPB Campus IV
 */
public interface DeckRepository extends JpaRepository<Deck, Long> {

    /**
     * Lista os decks mais recentes primeiro (para a home e o histórico).
     *
     * @return decks ordenados por data de criação decrescente
     */
    List<Deck> findAllByOrderByCriadoEmDesc();
}
