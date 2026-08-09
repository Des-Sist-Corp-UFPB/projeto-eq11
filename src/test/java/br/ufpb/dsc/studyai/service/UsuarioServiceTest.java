package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.domain.Usuario;
import br.ufpb.dsc.studyai.dto.UsuarioRequest;
import br.ufpb.dsc.studyai.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    }

    @Test
    void loadUserByUsername_encontrado_retornaUsuario() {
        Usuario u = new Usuario("joao", "HASH", "ROLE_USER");
        when(usuarioRepository.findByUsernameOrEmail("joao", "joao")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("joao");

        assertThat(details.getUsername()).isEqualTo("joao");
    }

    @Test
    void loadUserByUsername_naoEncontrado_lancaExcecao() {
        when(usuarioRepository.findByUsernameOrEmail("x", "x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void cadastrarUsuario_happyPath_salvaComSenhaCodificadaERoleUser() {
        when(usuarioRepository.findByUsernameOrEmail("joao", "joao@x.com")).thenReturn(Optional.empty());

        service.cadastrarUsuario(new UsuarioRequest("joao", "joao@x.com", "senha123", "senha123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();

        assertThat(salvo.getUsername()).isEqualTo("joao");
        assertThat(salvo.getEmail()).isEqualTo("joao@x.com");
        assertThat(salvo.getPassword()).isEqualTo("HASH");
        assertThat(salvo.getProvider()).isEqualTo("LOCAL");
        assertThat(salvo.getRoles()).isEqualTo("ROLE_USER");
    }

    @Test
    void cadastrarUsuario_senhasDiferentes_lancaEnaoSalva() {
        assertThatThrownBy(() -> service.cadastrarUsuario(
                new UsuarioRequest("joao", "j@x.com", "senha123", "outra")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("senhas não conferem");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastrarUsuario_usuarioJaExiste_lancaEnaoSalva() {
        when(usuarioRepository.findByUsernameOrEmail("joao", "j@x.com"))
                .thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> service.cadastrarUsuario(
                new UsuarioRequest("joao", "j@x.com", "senha123", "senha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já existe");

        verify(usuarioRepository, never()).save(any());
    }
}
