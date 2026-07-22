CREATE TABLE IF NOT EXISTS redacao (
    id BIGSERIAL PRIMARY KEY,
    banca VARCHAR(100) NOT NULL,
    tema VARCHAR(255) NOT NULL,
    texto TEXT NOT NULL,
    nota_total DECIMAL(5, 2) NOT NULL,
    comentario_geral TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS redacao_criterio (
    id BIGSERIAL PRIMARY KEY,
    redacao_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    nota DECIMAL(5, 2) NOT NULL,
    comentario TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_redacao_criterio FOREIGN KEY (redacao_id) REFERENCES redacao (id) ON DELETE CASCADE
);
