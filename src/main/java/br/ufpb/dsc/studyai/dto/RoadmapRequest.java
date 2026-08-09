package br.ufpb.dsc.studyai.dto;

import java.util.List;

/**
 * DTO de entrada do módulo <strong>RoadmapIA</strong> — respostas do questionário de
 * perfil enviadas pelo formulário.
 *
 * <p>As datas chegam como {@code String} no formato {@code yyyy-MM-dd} (o que o
 * {@code <input type="date">} envia). A conversão e a validação acontecem no
 * {@code RoadmapService}, para que uma data inválida vire uma mensagem amigável no
 * fragmento de erro em vez de um 400 do Spring.
 *
 * @param dataInicio   primeiro dia do plano ({@code yyyy-MM-dd})
 * @param dataFim      último dia do plano ({@code yyyy-MM-dd}), normalmente a data da prova
 * @param horasPorDia  horas de estudo disponíveis por dia
 * @param cursoAlvo    curso pretendido (ex.: "Medicina"), pode ser vazio
 * @param dificuldades áreas de maior dificuldade marcadas no questionário
 * @param experiencia  experiência anterior com o ENEM (ex.: "Já fiz, nota entre 500 e 650")
 * @param observacoes  informações livres adicionais, pode ser vazio
 */
public record RoadmapRequest(
        String dataInicio,
        String dataFim,
        int horasPorDia,
        String cursoAlvo,
        List<String> dificuldades,
        String experiencia,
        String observacoes
) {
}
