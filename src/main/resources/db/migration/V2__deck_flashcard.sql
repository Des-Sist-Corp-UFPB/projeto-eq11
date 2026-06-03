-- Migração V2: Módulo FlashIA — decks e flashcards
-- Cria as tabelas do primeiro módulo de IA do StudyAI (geração de flashcards).
--
-- IMPORTANTE: nunca edite uma migração já aplicada (o Flyway valida o checksum).
-- Para mudar o schema, crie uma nova migração V3__...sql.
--
-- Nesta entrega os decks NÃO têm dono (sem entidade de usuário ainda).
-- Quando a autenticação em banco for implementada, uma migração futura adiciona
-- a coluna usuario_id com a respectiva FK.

CREATE TABLE deck (
    id          BIGSERIAL PRIMARY KEY,
    titulo      VARCHAR(160)             NOT NULL,
    banca       VARCHAR(60),
    disciplina  VARCHAR(80),
    criado_em   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE flashcard (
    id       BIGSERIAL PRIMARY KEY,
    deck_id  BIGINT  NOT NULL REFERENCES deck (id) ON DELETE CASCADE,
    frente   TEXT    NOT NULL,
    verso    TEXT    NOT NULL,
    ordem    INTEGER NOT NULL DEFAULT 0
);

-- Índice para carregar os cartões de um deck já na ordem de exibição
CREATE INDEX idx_flashcard_deck ON flashcard (deck_id, ordem);

COMMENT ON TABLE deck IS 'Conjunto de flashcards gerados pela IA a partir de um texto';
COMMENT ON TABLE flashcard IS 'Cartão de memorização (frente = pergunta, verso = resposta)';
COMMENT ON COLUMN flashcard.ordem IS 'Posição do cartão dentro do deck (0-based)';
COMMENT ON COLUMN deck.criado_em IS 'Timestamp de criação do deck (UTC)';
