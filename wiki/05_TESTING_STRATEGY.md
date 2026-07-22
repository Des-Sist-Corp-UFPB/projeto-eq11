# 05. Estratégia de Testes

O StudyAI exige uma qualidade corporativa rigorosa. Por isso, a meta de cobertura de código do nosso CI/CD é configurada no JaCoCo para **85%**.

## Ferramentas Adotadas
- **JUnit 5**: O motor padrão.
- **Mockito**: Mock de classes secundárias para testes isolados.
- **MockMvc**: Utilizado para testar os *Controllers* e a renderização do *Thymeleaf/HTMX*, sem precisar levantar todo o Tomcat. Permite verificar status HTTP, atributos no Model, nome da view de retorno e segurança.
- **TestContainers**: Levanta de forma efêmera e automática uma imagem Docker real de banco de dados (`postgres`) quando rodamos testes de integração (`@SpringBootTest`).
- **AssertJ**: Uso da interface fluente para facilitar as asserções (`assertThat(x).isEqualTo(y)`).

## Padrão de Nomenclatura dos Testes
Usamos o padrão *Snake_case_camelCase* nos nomes dos métodos, no seguinte formato:
`nomedoMetodo_cenario_resultadoEsperado`.

Exemplo:
```java
@Test
void avaliar_modoDemo_retornaRedacaoMock() { ... }
```

## Executando os Testes

- Você deve manter o **Docker Engine em execução** no seu computador localmente para o TestContainers funcionar.
- Rode o comando:
```bash
mvn clean test
```

Para gerar e consultar o relatório de cobertura de código (quebra por classe, método, e if/elses cobertos):
```bash
mvn jacoco:report
```
*Em seguida abra o arquivo `target/site/jacoco/index.html` em qualquer navegador.*
