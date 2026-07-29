package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.domain.Usuario;
import br.ufpb.dsc.studyai.repository.UsuarioRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    public CustomOAuth2UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Deixa o Spring buscar as informações no Google
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email não retornado pelo provedor OAuth2");
        }

        // Verifica se o usuário já existe
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(email);
        
        Usuario usuario;
        if (usuarioOpt.isEmpty()) {
            // Se não existe, cria um novo
            usuario = new Usuario();
            usuario.setUsername(email); // Usa o e-mail como username
            usuario.setEmail(email);
            usuario.setProvider("GOOGLE");
            usuario.setRoles("ROLE_USER");
            usuarioRepository.save(usuario);
        } else {
            usuario = usuarioOpt.get();
            // Se já existia (criado via local), atualiza o provider
            if (!"GOOGLE".equals(usuario.getProvider())) {
                usuario.setProvider("GOOGLE");
                usuarioRepository.save(usuario);
            }
        }

        // Para simplificar a integração com a sessão, 
        // retornamos o próprio objeto do Google, mas os filtros do Spring 
        // vão lidar com isso. O importante é garantir que ele esteja no nosso banco.
        return oauth2User;
    }
}
