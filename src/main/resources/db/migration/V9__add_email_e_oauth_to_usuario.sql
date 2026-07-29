-- V9__add_email_e_oauth_to_usuario.sql

ALTER TABLE usuario 
ADD COLUMN email VARCHAR(100),
ADD COLUMN provider VARCHAR(20) DEFAULT 'LOCAL';

-- Permite que a senha seja nula para usuários que fazem login com o Google
ALTER TABLE usuario 
ALTER COLUMN password DROP NOT NULL;
