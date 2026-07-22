# 06. CI/CD e Deployment

Todo código empurrado (`git push`) para a branch `main` engatilha o Pipeline do GitHub Actions do nosso projeto, orquestrado de ponta a ponta.

## Fluxo do GitHub Actions
O arquivo `.github/workflows/deploy.yml` orquestra a Integração e Entrega Contínuas:

1. **Build e Testes (CI)**: Usa Maven para compilar o código e rodar as baterias de testes (o TestContainers cria e destrói um PostgreSQL dinâmico dentro do servidor de CI).
2. **SAST**: Aciona a bateria de varreduras de segurança sobre o código em busca de CVEs e más práticas (ver [Artigo 07](07_SECURITY.md)).
3. **Build da Imagem**: Se o código passar, o Maven cria um arquivo `.jar`. Um passo extra faz o build do contêiner Docker a partir do script multi-stage `docker/Dockerfile`.
4. **Push (Registry)**: O contêiner de produção é subido no GHCR (*GitHub Container Registry*).
5. **Deployment**: O GitHub Actions se conecta, através de chaves SSH (`SSH_DEPLOY_KEY`), ao nosso servidor privado (ex: `dsc.rodrigor.com`). Ele acessa o terminal de implantação e aciona o novo `docker compose up -d` para baixar e executar a nova versão com indisponibilidade próxima de zero (rolling update básico).

## O Arquivo `Dockerfile`
Fica alocado em `docker/Dockerfile`. Ele é chamado de *multi-stage build*:
- A primeira imagem (Eclipse Temurin JDK 21) apenas pega o código, injeta o Maven e constrói o Jar empacotado.
- A segunda imagem, de ambiente de produção (JRE leve ou Alpine Linux otimizado para Java 21), recebe os binários compilados da etapa anterior e joga fora todo o cache, código-fonte ou binários de compilação.
- O resultado é uma imagem final de deploy leve, segura (com menos binários nativos suscetíveis a ataques no servidor) e rápida de transferir na rede.
