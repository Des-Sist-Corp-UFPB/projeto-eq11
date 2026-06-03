package br.ufpb.dsc.mercado.service;

/**
 * Abstração do provedor de IA usado pela aplicação.
 *
 * <p><strong>Por que uma interface?</strong> O resto do sistema (services de negócio)
 * depende apenas deste contrato, não de um provedor específico. Assim é possível trocar
 * Anthropic por Gemini — ou por um modo demonstração — sem tocar na lógica de negócio.
 *
 * <p><strong>Regra de ouro:</strong> a chamada de IA é sempre feita no backend, com a
 * chave em variável de ambiente. A chave nunca vai para o navegador.
 *
 * @author DSC - UFPB Campus IV
 */
public interface IAService {

    /**
     * Envia um prompt de sistema + um prompt de usuário ao provedor e devolve o texto.
     *
     * <p>Em modo demonstração, retorna um conteúdo de exemplo imediatamente, sem
     * realizar nenhuma chamada externa.
     *
     * @param system instruções de sistema (papel/comportamento do modelo)
     * @param user   conteúdo/pergunta do usuário
     * @return texto bruto retornado pela IA (pode conter cercas markdown a serem limpas)
     */
    String completar(String system, String user);
}
