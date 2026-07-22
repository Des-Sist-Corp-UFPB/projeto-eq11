-- Adiciona a coluna usuario_id para isolamento de dados
ALTER TABLE deck ADD COLUMN usuario_id BIGINT;
ALTER TABLE deck ADD CONSTRAINT fk_deck_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);

ALTER TABLE redacao ADD COLUMN usuario_id BIGINT;
ALTER TABLE redacao ADD CONSTRAINT fk_redacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
