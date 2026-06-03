package br.ufpb.dsc.studyai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check público da equipe — usado pelo painel de Status para marcar o
 * serviço como "verde".
 *
 * <p>Contrato: {@code GET /ping} → 200 com JSON
 * <pre>
 * {
 *   "status": "ok",
 *   "service": "eq11",
 *   "timestamp": "2026-06-03T14:32:10Z"
 * }
 * </pre>
 *
 * <p>A rota é <strong>pública</strong> (liberada no {@code SecurityConfig} com
 * {@code permitAll()}) e não depende do banco — responde mesmo se a aplicação
 * estiver com dependências indisponíveis, pois o objetivo é apenas sinalizar que
 * o processo HTTP está no ar.
 *
 * <p>{@code @RestController} faz o Spring serializar o {@code Map} de retorno como
 * JSON automaticamente (via Jackson).
 *
 * @author DSC - UFPB Campus IV
 */
@RestController
public class PingController {

    /** Identificador da equipe exigido pelo contrato do painel de Status. */
    private static final String SERVICE = "eq11";

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "status", "ok",
                "service", SERVICE,
                // Instant.toString() produz ISO-8601 em UTC com sufixo "Z" (ex.: 2026-06-03T14:32:10Z)
                "timestamp", Instant.now().toString()
        );
    }
}
