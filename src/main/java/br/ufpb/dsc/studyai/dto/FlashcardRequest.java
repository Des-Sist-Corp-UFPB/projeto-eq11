package br.ufpb.dsc.studyai.dto;

/**
 * DTO de entrada do módulo FlashIA — dados enviados pelo formulário de geração.
 *
 * <p>Records Java são imutáveis e ideais para DTOs: o compilador gera construtor,
 * acessores ({@code banca()}, {@code texto()}...), {@code equals}, {@code hashCode}
 * e {@code toString} automaticamente.
 *
 * @param banca       banca/prova alvo (ex.: "Cebraspe", "ENEM" ou "Geral")
 * @param disciplina  disciplina/área (pode ser vazia para "qualquer área")
 * @param quantidade  quantidade de flashcards desejada
 * @param texto       conteúdo bruto a partir do qual os cartões serão gerados
 */
public record FlashcardRequest(
        String banca,
        String disciplina,
        int quantidade,
        String texto
) {
}
