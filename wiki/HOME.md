# 📚 Wiki do StudyAI BR

Bem-vindo(a) à documentação oficial do **StudyAI**, a plataforma corporativa de estudos com IA para Concursos e ENEM.

Esta Wiki foi projetada para facilitar a vida de novos desenvolvedores que chegam ao projeto, explicando as escolhas tecnológicas, como o ambiente é configurado e como a arquitetura foi desenhada.

## 🗂️ Índice de Artigos

Abaixo estão todos os artigos disponíveis na nossa base de conhecimento:

- [**01. Arquitetura do Sistema**](01_ARCHITECTURE.md): Entenda como as camadas do Spring se comunicam e como o HTMX evita a necessidade de uma SPA (React/Vue).
- [**02. Configuração de Ambiente**](02_ENVIRONMENT_SETUP.md): Como subir o banco e a aplicação via Docker, configurar as variáveis de IA e rodar o projeto localmente.
- [**03. Módulo: FlashIA**](03_MODULE_FLASHIA.md): Deep dive em como o gerador de flashcards se integra com a IA e persiste os baralhos de forma segura.
- [**04. Módulo: CorretorIA**](04_MODULE_CORRETORIA.md): Deep dive no corretor de redações, lidando com JSON complexo e prevenindo *Type Erasure* através de DTOs rigorosos no LangChain4j.
- [**05. Estratégia de Testes**](05_TESTING_STRATEGY.md): Como garantir os 85% de cobertura usando MockMvc, Mockito, e banco de dados real via TestContainers.
- [**06. CI/CD e Deployment**](06_CI_CD_DEPLOYMENT.md): Como funcionam as Actions do GitHub para build, análise de segurança e push pro Registry (GHCR).
- [**07. Segurança e Auditoria**](07_SECURITY.md): SAST (Semgrep, SpotBugs), proteção do top 10 OWASP e logs de auditoria automatizados.
- [**08. Convenções do Projeto**](08_CONVENTIONS.md): Padronização de Commits, nomenclatura Java e regras de Migrations Flyway.
- [**09. Pitch do Produto**](09_PITCH.md): O discurso oficial de venda do produto para apresentações.
