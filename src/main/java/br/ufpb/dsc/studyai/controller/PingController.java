package br.ufpb.dsc.studyai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
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

    private final JdbcTemplate jdbcTemplate;

    public PingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", SERVICE);
        // Instant.toString() produz ISO-8601 em UTC com sufixo "Z" (ex.: 2026-06-03T14:32:10Z)
        response.put("timestamp", Instant.now().toString());

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("status", "ok");
            response.put("database", "ok");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("database", "error");
            return ResponseEntity.status(500).body(response);
        }
    }
}
