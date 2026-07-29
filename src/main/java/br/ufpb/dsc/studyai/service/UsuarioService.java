package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.domain.Usuario;
import br.ufpb.dsc.studyai.dto.UsuarioRequest;
import br.ufpb.dsc.studyai.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário ou e-mail não encontrado: " + username));
    }

    @Transactional
    public void cadastrarUsuario(UsuarioRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("As senhas não conferem.");
        }
        if (usuarioRepository.findByUsernameOrEmail(request.username(), request.email() != null ? request.email() : "").isPresent()) {
            throw new IllegalArgumentException("Nome de usuário ou e-mail já existe.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setProvider("LOCAL");
        usuario.setRoles("ROLE_USER");

        usuarioRepository.save(usuario);
    }
}
