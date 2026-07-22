package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.domain.Criterio;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.dto.CorretorRequest;
import br.ufpb.dsc.studyai.dto.CorretorResponse;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.RedacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CorretorService {

    private static final Logger log = LoggerFactory.getLogger(CorretorService.class);

    private final CorretorAiService corretorAiService;
    private final RedacaoRepository redacaoRepository;
    private final br.ufpb.dsc.studyai.repository.UsuarioRepository usuarioRepository;
    private final IAProperties iaProperties;

    public CorretorService(CorretorAiService corretorAiService,
                           RedacaoRepository redacaoRepository,
                           br.ufpb.dsc.studyai.repository.UsuarioRepository usuarioRepository,
                           IAProperties iaProperties) {
        this.corretorAiService = corretorAiService;
        this.redacaoRepository = redacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.iaProperties = iaProperties;
    }

    @Transactional
    public Redacao avaliar(CorretorRequest request, String username) {
        if (iaProperties.isDemo()) {
            return gerarAvaliacaoMock(request, username);
        }

        try {
            log.info("Enviando redação para correção da banca {}...", request.banca());
            CorretorResponse response = corretorAiService.avaliarRedacao(
                    request.banca(), request.tema(), request.texto()
            );

            Redacao redacao = new Redacao(
                    request.banca(),
                    request.tema(),
                    request.texto(),
                    response.notaTotal(),
                    response.comentarioGeral()
            );
            usuarioRepository.findByUsername(username).ifPresent(redacao::setUsuario);

            if (response.criterios() != null) {
                response.criterios().forEach(c -> {
                    redacao.adicionarCriterio(new Criterio(c.nome(), c.nota(), c.comentario()));
                });
            }

            return redacaoRepository.save(redacao);

        } catch (Exception e) {
            log.error("Erro ao avaliar redação", e);
            throw new IAIndisponivelException("Erro na IA (Corretor): " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Redacao> buscarRedacao(Long id, String username) {
        Optional<Redacao> redacaoOpt = redacaoRepository.findByIdAndUsuarioUsername(id, username);
        // Força a inicialização da lazy collection dentro da transação
        redacaoOpt.ifPresent(r -> r.getCriterios().size());
        return redacaoOpt;
    }

    private Redacao gerarAvaliacaoMock(CorretorRequest request, String username) {
        log.info("CorretorIA operando em MODO DEMO.");
        Redacao mock = new Redacao(
                request.banca(),
                request.tema(),
                request.texto(),
                850.0,
                "Texto bem estruturado, mas apresenta falhas de coesão no segundo parágrafo e desvios gramaticais pontuais."
        );
        usuarioRepository.findByUsername(username).ifPresent(mock::setUsuario);

        mock.adicionarCriterio(new Criterio("Competência 1 (Gramática)", 160.0, "Alguns erros de pontuação e ortografia."));
        mock.adicionarCriterio(new Criterio("Competência 2 (Tema/Estrutura)", 200.0, "Excelente domínio do tema e estrutura dissertativa correta."));
        mock.adicionarCriterio(new Criterio("Competência 3 (Argumentação)", 160.0, "Bons argumentos, mas poderiam ser melhor desenvolvidos."));
        mock.adicionarCriterio(new Criterio("Competência 4 (Coesão)", 120.0, "Repetição de conectivos e falta de elementos coesivos entre parágrafos."));
        mock.adicionarCriterio(new Criterio("Competência 5 (Proposta)", 200.0, "Proposta de intervenção completa com os 5 elementos essenciais."));

        return redacaoRepository.save(mock);
    }
}
