-- Corrige o tipo das colunas de nota para casar com o tipo Double do Java
-- (Double mapeia para DOUBLE PRECISION / FLOAT8 no Postgres)

ALTER TABLE redacao ALTER COLUMN nota_total TYPE DOUBLE PRECISION;
ALTER TABLE redacao_criterio ALTER COLUMN nota TYPE DOUBLE PRECISION;
