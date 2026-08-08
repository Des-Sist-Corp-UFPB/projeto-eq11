package br.ufpb.dsc.studyai.service;

import br.ufpb.dsc.studyai.dto.RoadmapResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * Interface declarativa do LangChain4j para o módulo <strong>RoadmapIA</strong>.
 *
 * <p>Segue o mesmo padrão de {@code FlashcardAiService} e {@code CorretorAiService}: os
 * dados do aluno entram por placeholders {@code {{...}}} tratados pela engine do
 * LangChain4j (nunca por concatenação de string) e a saída é mapeada direto para o
 * record {@link RoadmapResponse}, sem parse manual de JSON.
 *
 * @author DSC - UFPB Campus IV
 */
@AiService
public interface RoadmapAiService {

    @SystemMessage("""
            Você é um coordenador pedagógico especialista em preparação para o ENEM, com anos de
            experiência montando cronogramas que realmente cabem na rotina do aluno. Você conhece
            a matriz de referência do exame, o peso de cada área e os assuntos que mais caem.
            Sua missão é montar um plano de estudos realista, específico e executável.
            """)
    @UserMessage("""
            Monte um plano de estudos para o ENEM cobrindo EXATAMENTE o período de {{dataInicio}}
            até {{dataFim}}, dividido em {{totalSemanas}} semanas numeradas de 1 a {{totalSemanas}}.

            PERFIL DO ALUNO (respostas do questionário):
            - Horas de estudo disponíveis por dia: {{horasPorDia}}
            - Curso pretendido: {{cursoAlvo}}
            - Áreas em que tem mais dificuldade: {{dificuldades}}
            - Experiência anterior com o ENEM: {{experiencia}}
            - Observações do aluno: {{observacoes}}

            REGRAS OBRIGATÓRIAS:
            - Devolva as {{totalSemanas}} semanas, sem pular nenhum número.
            - Cada semana tem no MÁXIMO {{tarefasPorSemana}} tarefas.
            - A data de cada tarefa deve estar entre {{dataInicio}} e {{dataFim}}, no formato
              yyyy-MM-dd, e dentro da semana correspondente (semana 1 começa em {{dataInicio}},
              cada semana seguinte começa 7 dias depois).
            - A soma das durações das tarefas de um mesmo dia não pode passar de {{horasPorDia}} horas.

            COMO PLANEJAR:
            - Dê mais tempo às áreas de dificuldade declaradas, mas não abandone as outras.
            - Considere o peso das áreas para o curso pretendido (ex.: Ciências da Natureza pesa
              mais em Medicina; Matemática pesa mais em Engenharia).
            - Distribua Redação ao longo de todo o plano, não só no final.
            - Alterne teoria, exercícios e revisão; use repetição espaçada para o que já foi visto.
            - Reserve simulados periódicos e, nas últimas semanas, priorize revisão e resolução de
              provas anteriores em vez de conteúdo novo.
            - Inclua pelo menos um dia mais leve por semana, para descanso.

            FORMATO DA RESPOSTA:
            - titulo: nome curto do plano, citando o curso pretendido quando houver.
            - resumoEstrategia: 2 ou 3 frases explicando a lógica do plano para este aluno.
            - semanas: lista com numero, foco (tema principal da semana) e tarefas.
            - Cada tarefa tem data (yyyy-MM-dd), assunto (título curto e específico), descricao
              (o que fazer na prática) e duracaoMinutos (número inteiro).

            Assuntos devem ser ESPECÍFICOS ("Funções do 2º grau: gráfico e raízes"), nunca genéricos
            ("Estudar Matemática").
            """)
    RoadmapResponse gerarRoadmap(
            @V("dataInicio") String dataInicio,
            @V("dataFim") String dataFim,
            @V("totalSemanas") int totalSemanas,
            @V("tarefasPorSemana") int tarefasPorSemana,
            @V("horasPorDia") int horasPorDia,
            @V("cursoAlvo") String cursoAlvo,
            @V("dificuldades") String dificuldades,
            @V("experiencia") String experiencia,
            @V("observacoes") String observacoes);
}
