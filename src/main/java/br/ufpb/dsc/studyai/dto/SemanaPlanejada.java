package br.ufpb.dsc.studyai.dto;

import java.util.List;

/**
 * Bloco semanal devolvido pela IA dentro de {@link RoadmapResponse}.
 *
 * <p>A IA define apenas o {@code numero} (para casar com a semana correspondente), o
 * {@code foco} e as tarefas. As datas de início e fim de cada semana são calculadas
 * pelo servidor a partir do intervalo escolhido pelo aluno.
 *
 * @param numero  posição da semana no plano, começando em 1
 * @param foco    tema principal da semana
 * @param tarefas tarefas diárias da semana
 */
public record SemanaPlanejada(
        int numero,
        String foco,
        List<TarefaPlanejada> tarefas
) {
}
