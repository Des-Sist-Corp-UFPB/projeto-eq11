package br.ufpb.dsc.studyai.repository;

import br.ufpb.dsc.studyai.domain.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para {@link Roadmap}.
 *
 * <p>As consultas sempre filtram pelo {@code username} do dono: é isso que impede um
 * usuário de abrir o roadmap de outro trocando o id na URL.
 *
 * @author DSC - UFPB Campus IV
 */
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    /**
     * Lista os roadmaps do usuário, do mais recente para o mais antigo.
     *
     * @param username dono dos roadmaps
     * @return roadmaps ordenados por data de criação decrescente
     */
    List<Roadmap> findAllByUsuarioUsernameOrderByCriadoEmDesc(String username);

    /**
     * Busca um roadmap específico garantindo que ele pertence ao usuário.
     *
     * @param id       identificador do roadmap
     * @param username dono esperado
     * @return o roadmap, ou vazio se não existir ou não for do usuário
     */
    Optional<Roadmap> findByIdAndUsuarioUsername(Long id, String username);
}
