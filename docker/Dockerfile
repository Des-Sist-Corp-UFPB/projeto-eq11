# =============================================================================
# Dockerfile de PRODUÇÃO — Multi-stage build
# =============================================================================
# Estágio 1 (builder): compila o projeto com Maven e gera o JAR.
# Estágio 2 (runtime): imagem mínima com apenas o JRE, sem ferramentas de build.
#
# Vantagens do multi-stage:
#   - Imagem final muito menor (sem Maven, sem fontes Java, sem classes temp.)
#   - Superfície de ataque reduzida (menos binários = menos vulnerabilidades)
#   - Separação entre ambiente de build e runtime
#
# Como construir:
#   docker build -f docker/Dockerfile -t studyai:latest .
# =============================================================================

# ---- Estágio 1: Build -------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copiar pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copiar código-fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# Baixa o agente Java do OpenTelemetry (instrumentação automática — traces, métricas
# e logs — sem alterar o código). Baixado aqui no builder, que já tem curl; o runtime
# recebe só o .jar. Para builds reproduzíveis, troque 'latest' por uma versão fixa.
RUN curl -fsSL -o /build/otel-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# ---- Estágio 2: Runtime -----------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Criar usuário não-root por segurança
# Executar aplicações como root em container é má prática de segurança
RUN groupadd --gid 1001 studyai && \
    useradd --uid 1001 --gid studyai --shell /bin/false studyai

WORKDIR /app

# Copiar o JAR da aplicação e o agente do OpenTelemetry do estágio anterior
COPY --from=builder /build/target/*.jar app.jar
COPY --from=builder /build/otel-javaagent.jar otel-javaagent.jar

# Definir owner dos arquivos
RUN chown studyai:studyai app.jar otel-javaagent.jar

# Trocar para usuário não-root
USER studyai

EXPOSE 8080

# Health check: o Actuator expõe /actuator/health
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Opções JVM para produção:
#   -XX:+UseContainerSupport: respeita limites de memória do container
#   -XX:MaxRAMPercentage=75: usa até 75% da RAM disponível para o heap
#   -javaagent: anexa o OpenTelemetry. Fica INERTE até OTEL_SDK_DISABLED=false e as
#     variáveis OTEL_* serem definidas (ver docker-compose.yml). Sem isso, não exporta
#     nada e não polui o log — o agente só instrumenta quando configurado.
ENTRYPOINT ["java", \
    "-javaagent:/app/otel-javaagent.jar", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]
