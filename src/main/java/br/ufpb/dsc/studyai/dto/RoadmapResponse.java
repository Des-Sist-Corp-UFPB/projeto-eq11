package br.ufpb.dsc.studyai.dto;

import java.util.List;

/**
 * DTO de saída da IA no módulo RoadmapIA — o plano de estudos completo.
 *
 * <p>Assim como {@code FlashcardResponse} e {@code CorretorResponse}, este record é um
 * <em>wrapper</em> em volta da lista. Retornar a lista direto da interface
 * {@code @AiService} esbarraria no <em>type erasure</em> do Java, que faz o LangChain4j
 * perder o tipo genérico em tempo de execução.
 *
 * @param titulo           nome curto do plano (ex.: "Plano ENEM 2026 — Medicina")
 * @param resumoEstrategia visão geral da estratégia, em 2 ou 3 frases
 * @param semanas          blocos semanais do plano
 */
public record RoadmapResponse(
        String titulo,
        String resumoEstrategia,
        List<SemanaPlanejada> semanas
) {
}
