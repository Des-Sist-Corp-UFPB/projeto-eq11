# 07. Segurança do Projeto

O StudyAI lida com transações que podem envolver chaves de APIs pagas e informações pessoais no banco de dados. Uma postura rígida de Segurança desde as raízes é obrigatória no nosso projeto.

## Ferramentas de SAST (Static Application Security Testing)

A cada Build e Push que realizamos, várias ferramentas escaneiam o projeto atrás de vazamentos ou falhas de configuração.

### SpotBugs + FindSecBugs
Analisa o **bytecode Java compilado** buscando padrões de vulnerabilidade conhecidas por ferramentas de hacking ou pelo banco de dados do FindSecBugs.

```bash
# Rodar análise local
mvn verify -Psecurity

# Ver relatório HTML
open target/spotbugsXml.html
```

**O que detecta**: SQL Injection, XXE, Path Traversal, uso inseguro de criptografia, random generators fracos, etc.
**Supressões**: Em caso de falsos positivos, documentamos no arquivo `spotbugs-exclude.xml`.

### Semgrep
Diferente do Spotbugs, ele analisa o **código-fonte** (texto antes da compilação). Ele caça más práticas específicas e senhas digitadas acidentalmente nas classes. Ele tem uma checagem customizada baseada em regras de mercado (ex: OWASP).

### Trivy
Verifica contêineres e imagens Docker:
- Identifica pacotes Linux/Alpine desatualizados.
- Faz o scan de segredos (.env que foram erroneamente enviados para a imagem).

### OWASP Dependency-Check
Verifica CVEs (vulnerabilidades conhecidas) nas dependências (libs do Maven) do projeto.

```bash
mvn verify -Psecurity
# Relatório em: target/dependency-check-report.html
```
- **Threshold**: Nosso pipeline no GitHub Actions cancela a entrega inteira caso identifique vulnerabilidades CVSS ≥ 7.0 (High/Critical) nos JARs de terceiros usados.
- **Supressões**: Ficam no arquivo `owasp-suppressions.xml`.

## Top 10 OWASP — Proteções Implementadas

| Risco | Proteção Implementada |
|---|---|
| A01: Broken Access Control | Spring Security — todo endpoint requer autenticação e escopos. |
| A02: Cryptographic Failures | BCrypt para hashes de senhas irreversíveis. |
| A03: Injection | JPA/Hibernate blinda de SQL Injection pelo uso nativo de *PreparedStatement*. |
| A05: Security Misconfiguration | Headers rigorosos de segurança padronizados via Spring Security. |
| A07: Auth Failures | Filtro nativo (Form Login) robusto acompanhado pelo Spring CSRF. |
| A08: Software Integrity | O Flyway Migration trava bancos corrompidos impedindo inicializações espúrias. |
| A09: Logging Failures | Log centralizado via Spring Boot Logger e Slf4j, salvando no HD do provedor e persistindo no banco de dados com a Entidade `AuditLog`. |
