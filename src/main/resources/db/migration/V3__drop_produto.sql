-- Migração V3: remove a tabela de exemplo "produto"
--
-- A entidade Produto era apenas o exemplo do boilerplate. O StudyAI não a utiliza,
-- então a tabela é descartada. A migração V1 (que a criou) NÃO é editada — o histórico
-- de schema é preservado e esta nova migração apenas reverte aquele estado.
--
-- IF EXISTS torna a operação idempotente/segura mesmo em bancos onde a V1 tenha
-- sido adaptada manualmente.

DROP TABLE IF EXISTS produto;
