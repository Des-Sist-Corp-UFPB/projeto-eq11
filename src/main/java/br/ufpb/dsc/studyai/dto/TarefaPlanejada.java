package br.ufpb.dsc.studyai.dto;

/**
 * Tarefa diária devolvida pela IA dentro de {@link SemanaPlanejada}.
 *
 * @param data            dia da tarefa no formato {@code yyyy-MM-dd}; o serviço valida
 *                        se cai dentro da semana e corrige quando não cai
 * @param assunto         título curto do que estudar (ex.: "Funções do 2º grau")
 * @param descricao       o que fazer na prática (leitura, exercícios, revisão)
 * @param duracaoMinutos  tempo estimado da tarefa, em minutos
 */
public record TarefaPlanejada(
        String data,
        String assunto,
        String descricao,
        int duracaoMinutos
) {
}
