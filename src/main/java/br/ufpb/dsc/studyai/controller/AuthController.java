package br.ufpb.dsc.studyai.controller;

import br.ufpb.dsc.studyai.dto.UsuarioRequest;
import br.ufpb.dsc.studyai.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller de autenticação — gerencia as rotas relacionadas a login/logout.
 *
 * <p>O Spring Security cuida automaticamente do processamento do formulário de login
 * (validação de credenciais, criação da sessão, etc.). Este controller apenas
 * serve a <strong>página</strong> de login — o Spring Security intercepta o POST.
 *
 * <p><strong>Fluxo do Spring Security Form Login:</strong>
 * <ol>
 *   <li>Usuário acessa uma rota protegida (ex.: {@code /flashcards}).</li>
 *   <li>Spring Security detecta que não está autenticado e redireciona para {@code /login}.</li>
 *   <li>Este controller serve a página HTML de login.</li>
 *   <li>Usuário submete o formulário com username/password para {@code POST /login}.</li>
 *   <li>Spring Security intercepta o POST, valida as credenciais e redireciona para {@code /}.</li>
 * </ol>
 *
 * <p>A URL de processamento do login ({@code POST /login}) é gerenciada <em>internamente</em>
 * pelo Spring Security — não precisamos (nem devemos) criar um método para ela aqui.
 *
 * @author DSC - UFPB Campus IV
 */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Serve a página de login customizada.
     *
     * <p>O Spring Security redireciona automaticamente para esta URL quando
     * o usuário tenta acessar um recurso protegido sem estar autenticado.
     * Isso é configurado em {@link SecurityConfig} com {@code .loginPage("/login")}.
     *
     * <p>O Thymeleaf recebe parâmetros {@code ?error} e {@code ?logout} automaticamente
     * na URL quando há erro de autenticação ou logout bem-sucedido, respectivamente.
     * O template usa {@code th:if="${param.error}"} para exibir mensagens.
     *
     * @return nome do template Thymeleaf da página de login
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "auth/cadastro";
    }

    @PostMapping("/cadastro/salvar")
    public String salvarCadastro(
            @RequestParam String username,
            @RequestParam(required = false) String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            usuarioService.cadastrarUsuario(new UsuarioRequest(username, email, password, confirmPassword));
            redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado! Você já pode fazer login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/cadastro";
        }
    }
}
