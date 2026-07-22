package br.ufpb.dsc.studyai.dto;

import java.util.List;

public record CorretorResponse(
        double notaTotal,
        String comentarioGeral,
        List<CriterioAvaliacao> criterios
) {
}
