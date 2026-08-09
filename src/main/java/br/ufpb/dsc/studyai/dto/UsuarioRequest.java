package br.ufpb.dsc.studyai.dto;

public record UsuarioRequest(
        String username,
        String email,
        String password,
        String confirmPassword
) {
}
