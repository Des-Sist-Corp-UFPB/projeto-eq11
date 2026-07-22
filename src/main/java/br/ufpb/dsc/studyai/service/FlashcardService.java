package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.config.IAProperties;
import br.ufpb.dsc.studyai.domain.Deck;
import br.ufpb.dsc.studyai.domain.Flashcard;
import br.ufpb.dsc.studyai.dto.FlashcardDTO;
import br.ufpb.dsc.studyai.dto.FlashcardRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FlashcardService {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final FlashcardAiService flashcardAiService;
    private final DeckRepository deckRepository;
    private final IAProperties props;

    public FlashcardService(FlashcardAiService flashcardAiService, DeckRepository deckRepository, IAProperties props) {
        this.flashcardAiService = flashcardAiService;
        this.deckRepository = deckRepository;
        this.props = props;
    }

    @Transactional
    public Deck gerar(FlashcardRequest request) {
        String banca = StringUtils.hasText(request.banca()) ? request.banca().trim() : "Geral";
        String disciplina = request.disciplina() == null ? "" : request.disciplina().trim();
        int quantidade = request.quantidade() <= 0 ? 15 : Math.min(request.quantidade(), 30);
        
        String texto = request.texto() == null ? "" : request.texto();
        if (texto.length() > 8000) {
            texto = texto.substring(0, 8000);
        }

        List<FlashcardDTO> cartoes;

        if (props.isDemo()) {
            cartoes = getFlashcardsDemo();
        } else {
            try {
                cartoes = flashcardAiService.gerarFlashcards(banca, disciplina, quantidade, texto);
                if (cartoes == null || cartoes.isEmpty()) {
                    throw new IAIndisponivelException("A IA não gerou nenhum flashcard válido.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IAIndisponivelException("Erro na IA: " + e.getMessage(), e);
            }
        }

        Deck deck = new Deck(montarTitulo(banca, disciplina), banca, disciplina);
        int ordem = 0;
        for (FlashcardDTO dto : cartoes) {
            deck.adicionarFlashcard(new Flashcard(dto.frente(), dto.verso(), ordem++));
        }
        return deckRepository.save(deck);
    }

    public Optional<Deck> buscarDeck(Long id) {
        return deckRepository.findById(id).map(deck -> {
            deck.getFlashcards().size(); // força a inicialização da coleção lazy
            return deck;
        });
    }

    private String montarTitulo(String banca, String disciplina) {
        String base = StringUtils.hasText(disciplina) ? disciplina
                : (!"Geral".equalsIgnoreCase(banca) ? banca : "Deck");
        return base + " — " + DATA_BR.format(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private List<FlashcardDTO> getFlashcardsDemo() {
        return List.of(
            new FlashcardDTO("O que é o princípio da legalidade na Administração Pública?", "O administrador só pode fazer o que a lei autoriza ou determina (art. 37 da CF/88). Diferente do particular, que pode fazer tudo que a lei não proíbe."),
            new FlashcardDTO("Quais são os princípios expressos da Administração Pública no art. 37 da CF/88?", "Legalidade, Impessoalidade, Moralidade, Publicidade e Eficiência — o mnemônico LIMPE."),
            new FlashcardDTO("Qual o prazo prescricional geral para a Fazenda Pública cobrar seus créditos tributários?", "5 anos, contados do lançamento definitivo (art. 174 do CTN)."),
            new FlashcardDTO("O que caracteriza o ato administrativo discricionário?", "Aquele em que a lei confere ao administrador margem de escolha quanto à conveniência e oportunidade (mérito administrativo), dentro dos limites legais."),
            new FlashcardDTO("Qual a diferença entre revogação e anulação de um ato administrativo?", "Revogação: ato legal, mas inconveniente/inoportuno, com efeitos ex nunc. Anulação: ato ilegal, com efeitos ex tunc (retroativos)."),
            new FlashcardDTO("O que estabelece o princípio da impessoalidade?", "A Administração deve tratar todos sem discriminação, visando ao interesse público, sem favorecer ou prejudicar pessoas determinadas."),
            new FlashcardDTO("Quem pode propor Ação Direta de Inconstitucionalidade (ADI)?", "Os legitimados do art. 103 da CF/88, como o Presidente, a Mesa do Senado/Câmara, o PGR, o Conselho Federal da OAB e partidos políticos com representação no Congresso."),
            new FlashcardDTO("O que é o controle de constitucionalidade difuso?", "Aquele exercido por qualquer juiz ou tribunal, no caso concreto, de forma incidental, com efeitos inter partes.")
        );
    }
}
