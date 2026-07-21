package br.ufpb.dsc.studyai.dto;

public record UsuarioRequest(
        String username,
        String password,
        String confirmPassword
) {
}
