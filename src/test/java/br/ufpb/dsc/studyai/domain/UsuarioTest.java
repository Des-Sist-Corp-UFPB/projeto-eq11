package br.ufpb.dsc.studyai.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de {@link Usuario} — construtor, acessores e a implementação de
 * {@code UserDetails} (parse das roles e flags de conta).
 */
class UsuarioTest {

    @Test
    void construtor_definirCampos() {
        Usuario u = new Usuario("joao", "hash", "ROLE_USER");

        assertThat(u.getUsername()).isEqualTo("joao");
        assertThat(u.getPassword()).isEqualTo("hash");
        assertThat(u.getRoles()).isEqualTo("ROLE_USER");
    }

    @Test
    void getAuthorities_umaRole() {
        Usuario u = new Usuario("joao", "h", "ROLE_USER");

        assertThat(u.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void getAuthorities_variasRoles_separaTrimEIgnoraVazias() {
        Usuario u = new Usuario("admin", "h", "ROLE_ADMIN, ROLE_USER, ");

        assertThat(u.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void flagsDeConta_sempreVerdadeiras() {
        Usuario u = new Usuario("joao", "h", "ROLE_USER");

        assertThat(u.isAccountNonExpired()).isTrue();
        assertThat(u.isAccountNonLocked()).isTrue();
        assertThat(u.isCredentialsNonExpired()).isTrue();
        assertThat(u.isEnabled()).isTrue();
    }

    @Test
    void gettersEsetters_funcionam() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("maria");
        u.setPassword("segredo");
        u.setRoles("ROLE_ADMIN");
        u.setEmail("maria@exemplo.com");
        u.setProvider("GOOGLE");

        assertThat(u.getId()).isEqualTo(1L);
        assertThat(u.getUsername()).isEqualTo("maria");
        assertThat(u.getPassword()).isEqualTo("segredo");
        assertThat(u.getRoles()).isEqualTo("ROLE_ADMIN");
        assertThat(u.getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(u.getProvider()).isEqualTo("GOOGLE");
    }

    @Test
    void provider_padraoEhLocal() {
        assertThat(new Usuario().getProvider()).isEqualTo("LOCAL");
    }
}
