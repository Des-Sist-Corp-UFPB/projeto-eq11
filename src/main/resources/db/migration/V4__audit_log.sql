-- Migração V4: Módulo de Log de Auditoria
-- Cria a tabela que registra ações relevantes de usuário (login, logout, geração
-- de flashcard), atendendo ao requisito de auditoria da disciplina.
--
-- IMPORTANTE: nunca edite uma migração já aplicada (o Flyway valida o checksum).
-- Para mudar o schema, crie uma nova migração V5__...sql.
--
-- Registro append-only: a aplicação apenas insere; não altera nem remove linhas.

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    usuario     VARCHAR(120),
    acao        VARCHAR(60)              NOT NULL,
    entidade    VARCHAR(60),
    entidade_id BIGINT,
    detalhes    TEXT,
    ip          VARCHAR(45),
    data_hora   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Consultas típicas: "últimos eventos" e "eventos de um usuário"
CREATE INDEX idx_audit_log_data ON audit_log (data_hora DESC);
CREATE INDEX idx_audit_log_usuario ON audit_log (usuario);

COMMENT ON TABLE audit_log IS 'Trilha de auditoria de ações de usuário (login, logout, geração de flashcard)';
COMMENT ON COLUMN audit_log.usuario IS 'Usuário autenticado que realizou a ação (ou "anonimo")';
COMMENT ON COLUMN audit_log.acao IS 'Ação em caixa alta: LOGIN, LOGOUT, LOGIN_FALHA, GERAR_FLASHCARD';
COMMENT ON COLUMN audit_log.entidade IS 'Entidade afetada, quando aplicável (ex.: deck)';
COMMENT ON COLUMN audit_log.entidade_id IS 'Id da entidade afetada, quando aplicável';
COMMENT ON COLUMN audit_log.ip IS 'IP de origem da requisição (suporta IPv6)';
COMMENT ON COLUMN audit_log.data_hora IS 'Timestamp do evento (UTC)';
