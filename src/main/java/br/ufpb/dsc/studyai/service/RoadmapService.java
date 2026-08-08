package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.domain.Roadmap;
import br.ufpb.dsc.studyai.domain.SemanaEstudo;
import br.ufpb.dsc.studyai.domain.TarefaEstudo;
import br.ufpb.dsc.studyai.dto.RoadmapRequest;
import br.ufpb.dsc.studyai.dto.RoadmapResponse;
import br.ufpb.dsc.studyai.dto.SemanaPlanejada;
import br.ufpb.dsc.studyai.dto.TarefaPlanejada;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.RoadmapRepository;
import br.ufpb.dsc.studyai.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Regra de negócio do módulo <strong>RoadmapIA</strong>.
 *
 * <p>Fluxo: valida o questionário → calcula os blocos semanais no servidor → pede o
 * conteúdo à IA → valida o que voltou → persiste o plano.
 *
 * <p><strong>Por que o calendário é calculado aqui e não pela IA?</strong><br>
 * Modelos de linguagem erram aritmética de datas com frequência. Calculando as semanas
 * em Java garantimos que o plano cobre exatamente o intervalo escolhido pelo aluno; a
 * IA fica responsável só pelo conteúdo pedagógico (foco da semana e tarefas). Se a IA
 * devolver uma data fora da semana, o serviço corrige em vez de gravar lixo.
 *
 * @author DSC - UFPB Campus IV
 */
@Service
@Transactional(readOnly = true)
public class RoadmapService {

    private static final Logger log = LoggerFactory.getLogger(RoadmapService.class);

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Teto de semanas por plano (~6 meses).
     *
     * <p>Existe por dois motivos: um plano detalhado de mais de meio ano estoura o limite
     * de resposta do modelo e vira conteúdo repetido, e a chamada demoraria mais que o
     * timeout configurado em {@code studyai.ia.timeout-segundos}.
     */
    static final int MAX_SEMANAS = 26;

    /** Acima deste número de semanas, reduzimos as tarefas por semana para caber na resposta. */
    private static final int LIMIAR_PLANO_LONGO = 8;

    private static final int TAREFAS_PLANO_CURTO = 6;
    private static final int TAREFAS_PLANO_LONGO = 4;

    private static final int DURACAO_MINIMA_MINUTOS = 15;

    /** Focos usados no modo demo e como rede de segurança quando a IA pula uma semana. */
    private static final String[] FOCOS_PADRAO = {
            "Matemática: funções e proporcionalidade",
            "Linguagens: interpretação e figuras de linguagem",
            "Ciências da Natureza: mecânica e termologia",
            "Ciências Humanas: Brasil República e geopolítica",
            "Redação: estrutura e proposta de intervenção",
            "Matemática: geometria e estatística",
            "Ciências da Natureza: química orgânica e ecologia",
            "Revisão geral e simulado completo"
    };

    private final RoadmapAiService roadmapAiService;
    private final RoadmapRepository roadmapRepository;
    private final UsuarioRepository usuarioRepository;
    private final IAProperties props;

    public RoadmapService(RoadmapAiService roadmapAiService,
                          RoadmapRepository roadmapRepository,
                          UsuarioRepository usuarioRepository,
                          IAProperties props) {
        this.roadmapAiService = roadmapAiService;
        this.roadmapRepository = roadmapRepository;
        this.usuarioRepository = usuarioRepository;
        this.props = props;
    }

    /**
     * Gera e persiste um roadmap de estudos a partir do questionário de perfil.
     *
     * @param request  respostas do questionário
     * @param username dono do plano
     * @return roadmap já persistido, com semanas e tarefas
     * @throws IllegalArgumentException  se o período informado for inválido
     * @throws IAIndisponivelException   se a chamada à IA falhar
     */
    @Transactional
    public Roadmap gerar(RoadmapRequest request, String username) {
        LocalDate inicio = parseData(request.dataInicio(), "data de início");
        LocalDate fim = parseData(request.dataFim(), "data final");
        validarPeriodo(inicio, fim);

        int horasPorDia = normalizarHoras(request.horasPorDia());
        String dificuldades = juntarDificuldades(request.dificuldades());
        String cursoAlvo = ouPadrao(request.cursoAlvo(), "não informado");
        String experiencia = ouPadrao(request.experiencia(), "não informado");
        String observacoes = ouPadrao(request.observacoes(), "nenhuma");

        List<Periodo> periodos = calcularSemanas(inicio, fim);
        int tarefasPorSemana = periodos.size() <= LIMIAR_PLANO_LONGO
                ? TAREFAS_PLANO_CURTO
                : TAREFAS_PLANO_LONGO;

        RoadmapResponse resposta;
        if (props.isDemo()) {
            log.info("RoadmapIA operando em MODO DEMO.");
            resposta = respostaDemo(periodos, cursoAlvo, horasPorDia, tarefasPorSemana);
        } else {
            resposta = chamarIa(inicio, fim, periodos.size(), tarefasPorSemana,
                    horasPorDia, cursoAlvo, dificuldades, experiencia, observacoes);
        }

        Roadmap roadmap = montarRoadmap(resposta, periodos, inicio, fim, horasPorDia, cursoAlvo);
        roadmap.setDificuldades(dificuldades);
        roadmap.setExperiencia(experiencia);
        roadmap.setObservacoes(StringUtils.hasText(request.observacoes()) ? request.observacoes().trim() : null);
        usuarioRepository.findByUsername(username).ifPresent(roadmap::setUsuario);

        return roadmapRepository.save(roadmap);
    }

    /**
     * Busca um roadmap do usuário já com as semanas e tarefas carregadas.
     *
     * @param id       identificador do roadmap
     * @param username dono esperado
     * @return o roadmap, ou vazio se não existir ou não pertencer ao usuário
     */
    public Optional<Roadmap> buscarRoadmap(Long id, String username) {
        return roadmapRepository.findByIdAndUsuarioUsername(id, username).map(roadmap -> {
            // Força a inicialização das coleções lazy ainda dentro da transação
            roadmap.getSemanas().forEach(semana -> semana.getTarefas().size());
            return roadmap;
        });
    }

    // ==========================================================================
    // Chamada à IA
    // ==========================================================================

    private RoadmapResponse chamarIa(LocalDate inicio, LocalDate fim, int totalSemanas,
                                     int tarefasPorSemana, int horasPorDia, String cursoAlvo,
                                     String dificuldades, String experiencia, String observacoes) {
        try {
            log.info("Gerando roadmap de {} semanas ({} a {})...", totalSemanas, inicio, fim);
            RoadmapResponse resposta = roadmapAiService.gerarRoadmap(
                    inicio.toString(), fim.toString(), totalSemanas, tarefasPorSemana,
                    horasPorDia, cursoAlvo, dificuldades, experiencia, observacoes);

            if (resposta == null || resposta.semanas() == null || resposta.semanas().isEmpty()) {
                throw new IAIndisponivelException("A IA não conseguiu montar um plano de estudos. Tente novamente.");
            }
            return resposta;
        } catch (IAIndisponivelException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao gerar roadmap de estudos", e);
            throw new IAIndisponivelException(
                    "Não foi possível gerar seu plano de estudos agora. Tente novamente em alguns instantes.", e);
        }
    }

    // ==========================================================================
    // Calendário (calculado no servidor)
    // ==========================================================================

    /** Intervalo de uma semana do plano. A última pode ser mais curta que 7 dias. */
    private record Periodo(int numero, LocalDate inicio, LocalDate fim) {
    }

    private List<Periodo> calcularSemanas(LocalDate inicio, LocalDate fim) {
        List<Periodo> periodos = new ArrayList<>();
        LocalDate cursor = inicio;
        int numero = 1;
        while (!cursor.isAfter(fim)) {
            LocalDate ultimoDia = cursor.plusDays(6);
            if (ultimoDia.isAfter(fim)) {
                ultimoDia = fim;
            }
            periodos.add(new Periodo(numero++, cursor, ultimoDia));
            cursor = ultimoDia.plusDays(1);
        }
        return periodos;
    }

    // ==========================================================================
    // Montagem do plano (valida o que a IA devolveu)
    // ==========================================================================

    private Roadmap montarRoadmap(RoadmapResponse resposta, List<Periodo> periodos,
                                  LocalDate inicio, LocalDate fim, int horasPorDia, String cursoAlvo) {
        String titulo = StringUtils.hasText(resposta.titulo())
                ? truncar(resposta.titulo().trim(), 160)
                : "Plano ENEM — " + DATA_BR.format(inicio) + " a " + DATA_BR.format(fim);

        Roadmap roadmap = new Roadmap(titulo, inicio, fim, horasPorDia);
        roadmap.setCursoAlvo(truncar(cursoAlvo, 120));
        roadmap.setResumoEstrategia(resposta.resumoEstrategia());

        // Indexa o que a IA devolveu pelo número da semana, para casar com o calendário real.
        Map<Integer, SemanaPlanejada> planejadas = new LinkedHashMap<>();
        for (SemanaPlanejada semana : resposta.semanas()) {
            if (semana != null) {
                planejadas.putIfAbsent(semana.numero(), semana);
            }
        }

        // Percorre o calendário (e não a resposta da IA): assim o plano cobre o período inteiro
        // mesmo que a IA tenha pulado ou repetido semanas.
        for (Periodo periodo : periodos) {
            SemanaPlanejada planejada = planejadas.get(periodo.numero());
            List<TarefaPlanejada> tarefas = planejada != null && planejada.tarefas() != null
                    ? planejada.tarefas()
                    : tarefasPadrao(periodo, horasPorDia, TAREFAS_PLANO_CURTO);

            String foco = planejada != null && StringUtils.hasText(planejada.foco())
                    ? truncar(planejada.foco().trim(), 160)
                    : focoPadrao(periodo.numero());

            if (planejada == null) {
                log.warn("A IA não devolveu a semana {}; preenchida com o plano padrão.", periodo.numero());
            }

            SemanaEstudo semana = new SemanaEstudo(periodo.numero(), periodo.inicio(), periodo.fim(), foco);
            int ordem = 0;
            for (TarefaPlanejada tarefa : tarefas) {
                if (tarefa == null || !StringUtils.hasText(tarefa.assunto())) {
                    continue;
                }
                semana.adicionarTarefa(new TarefaEstudo(
                        resolverData(tarefa.data(), periodo, ordem),
                        truncar(tarefa.assunto().trim(), 160),
                        tarefa.descricao(),
                        normalizarDuracao(tarefa.duracaoMinutos(), horasPorDia),
                        ordem++));
            }
            roadmap.adicionarSemana(semana);
        }
        return roadmap;
    }

    /**
     * Converte a data devolvida pela IA, garantindo que ela caia dentro da semana.
     *
     * <p>Quando a data vem ausente, mal formatada ou fora do intervalo, a tarefa é
     * distribuída pela ordem em que veio — o plano continua coerente em vez de gravar
     * uma data inválida.
     */
    private LocalDate resolverData(String bruta, Periodo periodo, int indice) {
        if (StringUtils.hasText(bruta)) {
            try {
                LocalDate data = LocalDate.parse(bruta.trim());
                if (!data.isBefore(periodo.inicio()) && !data.isAfter(periodo.fim())) {
                    return data;
                }
                log.debug("Data {} fora da semana {}; reposicionada.", bruta, periodo.numero());
            } catch (DateTimeParseException e) {
                log.debug("Data '{}' em formato inesperado na semana {}; reposicionada.", bruta, periodo.numero());
            }
        }
        long diasNaSemana = ChronoUnit.DAYS.between(periodo.inicio(), periodo.fim());
        return periodo.inicio().plusDays(Math.min(indice, diasNaSemana));
    }

    private int normalizarDuracao(int minutos, int horasPorDia) {
        int maximo = horasPorDia * 60;
        if (minutos < DURACAO_MINIMA_MINUTOS) {
            return DURACAO_MINIMA_MINUTOS;
        }
        return Math.min(minutos, maximo);
    }

    // ==========================================================================
    // Modo demo (funciona sem chave de API e sem internet)
    // ==========================================================================

    private RoadmapResponse respostaDemo(List<Periodo> periodos, String cursoAlvo,
                                         int horasPorDia, int tarefasPorSemana) {
        List<SemanaPlanejada> semanas = periodos.stream()
                .map(p -> new SemanaPlanejada(
                        p.numero(),
                        focoPadrao(p.numero()),
                        tarefasPadrao(p, horasPorDia, tarefasPorSemana)))
                .toList();

        String titulo = "Plano ENEM — " + periodos.size() + " semanas"
                + ("não informado".equals(cursoAlvo) ? "" : " · " + cursoAlvo);

        return new RoadmapResponse(
                titulo,
                "Plano de exemplo gerado em modo demonstração: as áreas do ENEM se alternam ao longo "
                        + "das semanas, com redação recorrente e simulado ao final de cada bloco. "
                        + "Configure uma chave de IA para receber um plano personalizado pelo seu perfil.",
                semanas);
    }

    private List<TarefaPlanejada> tarefasPadrao(Periodo periodo, int horasPorDia, int quantidade) {
        String[] modelos = {
                "Teoria do foco da semana|Leitura ativa do conteúdo principal, com resumo em tópicos.",
                "Exercícios do foco da semana|Resolução de questões do ENEM sobre o tema estudado.",
                "Redação|Produção de um texto dissertativo-argumentativo cronometrado.",
                "Revisão espaçada|Revisão dos assuntos das semanas anteriores usando flashcards.",
                "Questões de anos anteriores|Bateria de questões de provas passadas, com correção comentada.",
                "Simulado parcial|Bloco de questões cronometrado, simulando o ritmo da prova."
        };

        long diasNaSemana = ChronoUnit.DAYS.between(periodo.inicio(), periodo.fim()) + 1;
        int total = (int) Math.min(quantidade, diasNaSemana);
        int minutosPorTarefa = Math.max(DURACAO_MINIMA_MINUTOS, (horasPorDia * 60) / 2);

        List<TarefaPlanejada> tarefas = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            String[] partes = modelos[i % modelos.length].split("\\|");
            tarefas.add(new TarefaPlanejada(
                    periodo.inicio().plusDays(i).toString(),
                    partes[0],
                    partes[1],
                    minutosPorTarefa));
        }
        return tarefas;
    }

    private String focoPadrao(int numeroSemana) {
        return FOCOS_PADRAO[(numeroSemana - 1) % FOCOS_PADRAO.length];
    }

    // ==========================================================================
    // Validação e normalização das respostas do questionário
    // ==========================================================================

    private LocalDate parseData(String valor, String campo) {
        if (!StringUtils.hasText(valor)) {
            throw new IllegalArgumentException("Informe a " + campo + ".");
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("A " + campo + " informada não é uma data válida.");
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("A data final precisa ser posterior à data de início.");
        }
        long dias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        long semanas = (long) Math.ceil(dias / 7.0);
        if (semanas > MAX_SEMANAS) {
            throw new IllegalArgumentException(
                    "O período informado tem " + semanas + " semanas. Gere planos de no máximo "
                            + MAX_SEMANAS + " semanas (cerca de 6 meses) por vez.");
        }
    }

    private int normalizarHoras(int horas) {
        if (horas < 1) {
            return 2;
        }
        return Math.min(horas, 12);
    }

    private String juntarDificuldades(List<String> dificuldades) {
        if (dificuldades == null || dificuldades.isEmpty()) {
            return "não informou áreas específicas";
        }
        String juntas = String.join(", ", dificuldades);
        return truncar(juntas, 255);
    }

    private String ouPadrao(String valor, String padrao) {
        return StringUtils.hasText(valor) ? valor.trim() : padrao;
    }

    private String truncar(String valor, int limite) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= limite ? valor : valor.substring(0, limite);
    }
}
