# 02. Configuração de Ambiente

Para rodar o StudyAI localmente e contribuir com o projeto, você precisará de um ambiente de desenvolvimento Java moderno e do Docker.

## Pré-requisitos
- **Java 21** (recomendado Eclipse Temurin ou Amazon Corretto)
- **Maven 3.9+** (ou utilizar o Wrapper do Spring Boot caso configurado)
- **Docker Desktop** (para subir o banco de dados e rodar os TestContainers)
- IDE moderna (IntelliJ IDEA, VS Code ou Cursor)

## Variáveis de Ambiente (`.env`)
Antes de iniciar, o projeto precisa de certas variáveis. Na raiz do projeto, copie o arquivo `.env.example` para `.env`:

```bash
cp .env.example .env
```

Preencha as variáveis de ambiente necessárias. A mais importante para o desenvolvimento local é a chave de API da IA:
```ini
STUDYAI_AI_MODO=real
STUDYAI_AI_PROVEDOR=gemini
STUDYAI_AI_API_KEY=sua-api-key-aqui
```
*(Se você quiser apenas testar a interface sem consumir IA, mude `STUDYAI_AI_MODO=demo`).*

## Subindo as Dependências Locais

Em vez de instalar o PostgreSQL na sua máquina, usamos o Docker Compose para subir uma instância voltada ao desenvolvimento local:

```bash
docker compose up -d postgres
```
Isso vai expor o banco na porta `5432` da sua máquina, com o usuário, senha e schema definidos no `docker-compose.yml`. O Spring Boot (`application-dev.yml`) já está configurado para se conectar nele automaticamente.

## Rodando a Aplicação
Com o banco rodando, inicie o Spring Boot via Maven:

```bash
mvn spring-boot:run
```
O servidor abrirá em `http://127.0.0.1:8111` por padrão.

## Rodando a Aplicação Completa via Docker (Para Demonstração)
Se você não quer instalar o Java e o Maven e quer apenas ver a aplicação rodando:

```bash
docker compose up -d --build
```
Isso compilará a imagem do Spring Boot, conectará ela à imagem do banco de dados na rede do Docker e exporá a porta `8111`.

## Comandos Úteis do Maven
- `mvn clean compile`: Compila as classes.
- `mvn clean test`: Roda todos os testes unitários (requer o Docker rodando para o TestContainers).
- `mvn verify -Psecurity`: Aciona o pipeline local de segurança SAST (SpotBugs, OWASP Dependency-Check).
