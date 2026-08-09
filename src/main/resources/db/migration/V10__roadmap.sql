-- Migração V10: Módulo Roadmap de Estudos (foco ENEM)
--
-- Guarda o plano de estudos gerado pela IA a partir do questionário de perfil do
-- aluno. A hierarquia é: roadmap 1→N semanas 1→N tarefas diárias.
--
-- IMPORTANTE: nunca edite uma migração já aplicada (o Flyway valida o checksum).
-- Para mudar o schema, crie uma nova migração V11__...sql.

CREATE TABLE roadmap (
    id                BIGSERIAL PRIMARY KEY,
    usuario_id        BIGINT REFERENCES usuario (id),
    titulo            VARCHAR(160)             NOT NULL,
    data_inicio       DATE                     NOT NULL,
    data_fim          DATE                     NOT NULL,
    horas_por_dia     INTEGER                  NOT NULL,
    curso_alvo        VARCHAR(120),
    dificuldades      VARCHAR(255),
    experiencia       VARCHAR(120),
    observacoes       TEXT,
    resumo_estrategia TEXT,
    criado_em         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE roadmap_semana (
    id          BIGSERIAL PRIMARY KEY,
    roadmap_id  BIGINT  NOT NULL REFERENCES roadmap (id) ON DELETE CASCADE,
    numero      INTEGER NOT NULL,
    data_inicio DATE    NOT NULL,
    data_fim    DATE    NOT NULL,
    foco        VARCHAR(160)
);

CREATE TABLE roadmap_tarefa (
    id              BIGSERIAL PRIMARY KEY,
    semana_id       BIGINT       NOT NULL REFERENCES roadmap_semana (id) ON DELETE CASCADE,
    data            DATE         NOT NULL,
    assunto         VARCHAR(160) NOT NULL,
    descricao       TEXT,
    duracao_minutos INTEGER      NOT NULL DEFAULT 60,
    ordem           INTEGER      NOT NULL DEFAULT 0
);

-- Consultas típicas: "meus roadmaps mais recentes" e a montagem da árvore semana→tarefa
CREATE INDEX idx_roadmap_usuario ON roadmap (usuario_id, criado_em DESC);
CREATE INDEX idx_roadmap_semana ON roadmap_semana (roadmap_id, numero);
CREATE INDEX idx_roadmap_tarefa ON roadmap_tarefa (semana_id, ordem);

COMMENT ON TABLE roadmap IS 'Plano de estudos gerado pela IA a partir do questionário de perfil do aluno';
COMMENT ON TABLE roadmap_semana IS 'Bloco semanal do plano, com o foco principal daquela semana';
COMMENT ON TABLE roadmap_tarefa IS 'Tarefa de um dia específico dentro da semana';
COMMENT ON COLUMN roadmap.dificuldades IS 'Áreas de maior dificuldade declaradas pelo aluno (separadas por vírgula)';
COMMENT ON COLUMN roadmap.resumo_estrategia IS 'Visão geral da estratégia, escrita pela IA';
COMMENT ON COLUMN roadmap_tarefa.ordem IS 'Posição da tarefa dentro da semana (0-based)';
