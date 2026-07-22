package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.audit.AuditLogService;
import br.ufpb.dsc.studyai.domain.Redacao;
import br.ufpb.dsc.studyai.dto.CorretorRequest;
import br.ufpb.dsc.studyai.exception.IAIndisponivelException;
import br.ufpb.dsc.studyai.service.CorretorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/corretor")
public class CorretorController {

    private final CorretorService corretorService;
    private final AuditLogService auditLogService;

    public CorretorController(CorretorService corretorService, AuditLogService auditLogService) {
        this.corretorService = corretorService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("titulo", "CorretorIA");
        return "studyai/corretor";
    }

    @GetMapping("/{id}")
    public String abrirRedacao(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute("titulo", "CorretorIA");
        var redacaoOpt = corretorService.buscarRedacao(id, principal.getName());
        if (redacaoOpt.isPresent()) {
            model.addAttribute("redacao", redacaoOpt.get());
        }
        return "studyai/corretor";
    }

    @PostMapping("/avaliar")
    public String avaliar(@ModelAttribute CorretorRequest request, Model model, Principal principal) {
        try {
            Redacao redacao = corretorService.avaliar(request, principal.getName());

            auditLogService.registrar(
                    "AVALIAR_REDACAO", "redacao", redacao.getId(),
                    "banca=" + redacao.getBanca() + "; nota=" + redacao.getNotaTotal());

            model.addAttribute("redacao", redacao);
            return "studyai/fragments/corretor-result :: avaliacao";
        } catch (IAIndisponivelException e) {
            model.addAttribute("mensagem", e.getMessage());
            return "studyai/fragments/corretor-result :: erro";
        } catch (Exception e) {
            model.addAttribute("mensagem", "Ocorreu um erro interno ao processar a redação.");
            return "studyai/fragments/corretor-result :: erro";
        }
    }
}
