package br.ufpb.dsc.studyai.exception;

/**
 * Exceção de domínio lançada quando a geração via IA falha.
 *
 * <p>Cobre os cenários: provedor fora do ar, timeout, chave inválida ou resposta
 * em formato inesperado (JSON inválido). A mensagem é amigável e pode ser exibida
 * diretamente ao usuário no fragmento de erro.
 *
 * @author DSC - UFPB Campus IV
 */
public class IAIndisponivelException extends RuntimeException {

    public IAIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public IAIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
