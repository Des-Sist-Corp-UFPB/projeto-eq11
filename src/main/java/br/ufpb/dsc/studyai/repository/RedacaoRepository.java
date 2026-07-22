package br.ufpb.dsc.studyai.repository;

import br.ufpb.dsc.studyai.domain.Redacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedacaoRepository extends JpaRepository<Redacao, Long> {
    java.util.List<Redacao> findAllByOrderByCriadoEmDesc();
}
