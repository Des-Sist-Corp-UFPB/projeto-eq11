package br.ufpb.dsc.studyai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import br.ufpb.dsc.studyai.audit.AuditLogoutHandler;

/**
 * Configuração de segurança da aplicação usando Spring Security 6.
 *
 * <p><strong>Como o Spring Security funciona?</strong><br>
 * O Spring Security é baseado em uma cadeia de filtros (Filter Chain) que intercepta
 * todas as requisições HTTP antes de chegarem ao Controller. Cada filtro tem uma
 * responsabilidade específica (autenticação, autorização, CSRF, etc.).
 *
 * <p><strong>Principais conceitos:</strong>
 * <ul>
 *   <li><strong>Authentication</strong>: Verifica quem é o usuário (login/senha).</li>
 *   <li><strong>Authorization</strong>: Verifica o que o usuário pode fazer (roles/permissões).</li>
 *   <li><strong>CSRF</strong>: Proteção contra Cross-Site Request Forgery.</li>
 *   <li><strong>PasswordEncoder</strong>: Nunca armazene senhas em texto puro! BCrypt aplica um
 *       hash com salt aleatório a cada chamada.</li>
 * </ul>
 *
 * <p><strong>{@code @Configuration} + {@code @EnableWebSecurity}:</strong><br>
 * {@code @Configuration} marca a classe como fonte de definição de beans.
 * {@code @EnableWebSecurity} ativa a integração do Spring Security com o contexto do Spring MVC.
 *
 * @author DSC - UFPB Campus IV
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Provedor de autenticação que utiliza o UsuarioService (banco de dados) e BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder);
        return authProvider;
    }

    /**
     * Define o algoritmo de codificação de senhas.
     *
     * <p><strong>Por que BCrypt?</strong><br>
     * BCrypt é um algoritmo de hash adaptativo — você pode aumentar o "cost factor"
     * conforme os computadores ficam mais rápidos, sem precisar re-hashear as senhas.
     * Ele também adiciona um salt aleatório automaticamente, impedindo ataques de
     * rainbow table (tabelas pré-computadas de hashes).
     *
     * <p>Nunca use MD5, SHA-1 ou SHA-256 simples para senhas!
     *
     * @return instância do BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura a cadeia de filtros de segurança HTTP.
     *
     * <p>Este é o método central da configuração do Spring Security.
     * A API fluente do {@code HttpSecurity} permite configurar:
     * <ul>
     *   <li>Quais URLs são públicas e quais exigem autenticação</li>
     *   <li>Como o login é feito (formulário, OAuth2, JWT, etc.)</li>
     *   <li>Como o logout funciona</li>
     *   <li>Configurações de CSRF, headers de segurança, etc.</li>
     * </ul>
     *
     * @param http              construtor de configuração de segurança HTTP
     * @param auditLogoutHandler handler aditivo que registra o logout na auditoria
     * @return cadeia de filtros configurada
     * @throws Exception se ocorrer erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuditLogoutHandler auditLogoutHandler, br.ufpb.dsc.studyai.service.CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                // === AUTORIZAÇÃO DE REQUISIÇÕES ===
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos e health checks são públicos
                        // /webjars/** → Bootstrap, HTMX (servidos pelo Spring como recursos estáticos)
                        // /css/**, /js/** → arquivos estáticos personalizados
                        // /actuator/health → monitoramento sem autenticação
                        // /ping → health check público da equipe (painel de Status)
                        .requestMatchers("/ping", "/webjars/**", "/css/**", "/js/**", "/actuator/health", "/cadastro", "/cadastro/salvar", "/login").permitAll()
                        // Qualquer outra requisição exige autenticação
                        .anyRequest().authenticated()
                )

                // === FORMULÁRIO DE LOGIN ===
                .formLogin(form -> form
                        // URL da página de login customizada (em vez da padrão do Spring Security)
                        .loginPage("/login")
                        // Após login bem-sucedido, redireciona para a home do StudyAI ("/")
                        // O segundo parâmetro (true) força sempre ir para esta URL,
                        // ignorando a URL que o usuário tentou acessar antes do login
                        .defaultSuccessUrl("/", true)
                        // A página de login deve ser acessível sem autenticação
                        .permitAll()
                )

                // === OAUTH2 (GOOGLE) ===
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )

                // === LOGOUT ===
                .logout(logout -> logout
                        // Após logout, redireciona para a página de login com mensagem
                        .logoutSuccessUrl("/login?logout")
                        // Handler ADITIVO: registra o logout na auditoria sem alterar o redirect acima
                        .addLogoutHandler(auditLogoutHandler)
                        .permitAll()
                )

                // === CSRF (Cross-Site Request Forgery) ===
                // CSRF é um ataque onde um site malicioso faz requisições em nome do usuário autenticado.
                // O Spring Security protege adicionando um token único em cada sessão.
                //
                // O StudyAI mantém o CSRF TOTALMENTE ATIVO (inclusive nos POSTs do HTMX):
                // o token é exposto em meta tags no layout e o studyai.js o envia no header
                // a cada requisição HTMX (evento htmx:configRequest). Por isso não há nenhum
                // endpoint isento aqui — a proteção vale para toda a aplicação.
                .csrf(csrf -> {
                });

        return http.build();
    }

    /**
     * Expõe o {@code AuthenticationManager} como bean do Spring.
     *
     * <p>Necessário quando você precisa injetar o {@code AuthenticationManager} em outras classes,
     * como em um controller de API REST que faz autenticação programática.
     * Para este projeto educacional, serve como exemplo de como expor o bean.
     *
     * @param config configuração de autenticação gerenciada pelo Spring Security
     * @return instância do AuthenticationManager
     * @throws Exception se ocorrer erro ao obter o manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
