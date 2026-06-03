# AI Agents Context — StudyAI DSC/UFPB

> Este arquivo fornece contexto para ferramentas de IA (Cursor, GitHub Copilot, etc.)
> Para Claude Code, veja CLAUDE.md (mais completo).

## Projeto
Boilerplate Spring Boot para disciplina universitária. Java 21, Spring Boot 3.4.5, PostgreSQL, Thymeleaf+HTMX+Bootstrap.

## Pacote base
`br.ufpb.dsc.studyai`

## Padrões importantes
- DTOs são Records Java imutáveis
- Service layer com `@Transactional`
- Controllers retornam fragments Thymeleaf para HTMX
- Migrations de banco via Flyway em `src/main/resources/db/migration/`
- Variáveis de ambiente para configuração de produção (`.env`)
- NUNCA commitar `.env` ou senhas

## Comandos rápidos
```bash
mvn spring-boot:run                    # rodar local
mvn test                               # testes (requer Docker)
mvn verify -Psecurity                  # SAST + CVE check
docker compose up  # ambiente completo
```

Leia `docs/ARCHITECTURE.md` para detalhes arquiteturais.
