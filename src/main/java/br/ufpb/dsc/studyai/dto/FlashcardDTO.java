package br.ufpb.dsc.studyai.dto;

/**
 * DTO de saída de um flashcard — espelha o JSON produzido pela IA.
 *
 * <p>Usado tanto para desserializar a resposta da IA (Jackson) quanto para
 * enviar os cartões ao template/JS client-side.
 *
 * @param frente pergunta (face da frente do cartão)
 * @param verso  resposta (face do verso do cartão)
 */
public record FlashcardDTO(
        String frente,
        String verso
) {
}
