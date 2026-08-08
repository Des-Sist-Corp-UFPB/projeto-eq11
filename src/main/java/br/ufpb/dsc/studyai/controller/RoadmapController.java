package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogService;
import br.ufpb.dsc.studyai.domain.Roadmap;
import br.ufpb.dsc.studyai.dto.RoadmapRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.service.RoadmapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * Controller do módulo <strong>RoadmapIA</strong>.
 *
 * <p>Rotas:
 * <ul>
 *   <li>{@code GET /roadmap} — questionário de perfil + área de resultado.</li>
 *   <li>{@code GET /roadmap/{id}} — reabre um plano já gerado.</li>
 *   <li>{@code POST /roadmap/gerar} — gera o plano e devolve um <em>fragmento</em>
 *       Thymeleaf, trocado na página via HTMX (sem recarregar).</li>
 * </ul>
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final AuditLogService auditLogService;

    public RoadmapController(RoadmapService roadmapService, AuditLogService auditLogService) {
        this.roadmapService = roadmapService;
        this.auditLogService = auditLogService;
    }

    /**
     * Exibe a página do RoadmapIA com o questionário de perfil.
     *
     * @param model modelo Thymeleaf
     * @return template da página
     */
    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("titulo", "RoadmapIA");
        return "studyai/roadmap";
    }

    /**
     * Reabre um plano salvo, renderizando a página já com o cronograma preenchido.
     *
     * @param id        identificador do roadmap
     * @param model     modelo Thymeleaf
     * @param principal usuário autenticado
     * @return página do RoadmapIA com o plano carregado (ou o placeholder, se não existir)
     */
    @GetMapping("/{id}")
    public String abrirRoadmap(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute("titulo", "RoadmapIA");
        roadmapService.buscarRoadmap(id, principal.getName())
                .ifPresent(roadmap -> model.addAttribute("roadmap", roadmap));
        // Se o plano não existir (ou for de outro usuário), a página exibe o placeholder padrão
        return "studyai/roadmap";
    }

    /**
     * Gera o plano de estudos a partir do questionário e devolve o fragmento do cronograma.
     *
     * <p>Acionado pelo HTMX: {@code hx-post="/roadmap/gerar"},
     * {@code hx-target="#roadmap-resultado"}, {@code hx-indicator="#roadmap-loading"}.
     *
     * @param request   respostas do questionário (mapeadas pelo nome dos campos)
     * @param model     modelo Thymeleaf
     * @param principal usuário autenticado
     * @return fragmento com o cronograma, ou fragmento de erro amigável
     */
    @PostMapping("/gerar")
    public String gerar(@ModelAttribute RoadmapRequest request, Model model, Principal principal) {
        try {
            Roadmap roadmap = roadmapService.gerar(request, principal.getName());

            // Auditoria: registra a geração do plano (usuário e IP resolvidos no service)
            auditLogService.registrar(
                    "GERAR_ROADMAP", "roadmap", roadmap.getId(),
                    roadmap.getSemanas().size() + " semanas; " + roadmap.getTotalTarefas() + " tarefas");

            model.addAttribute("roadmap", roadmap);
            return "studyai/fragments/roadmap-result :: roadmap";
        } catch (IllegalArgumentException e) {
            // Questionário inválido (datas ausentes, período invertido ou longo demais)
            model.addAttribute("mensagem", e.getMessage());
            return "studyai/fragments/roadmap-result :: erro";
        } catch (IAIndisponivelException e) {
            model.addAttribute("mensagem", e.getMessage());
            return "studyai/fragments/roadmap-result :: erro";
        }
    }
}
