package locadora.service;

/**
 * Quebra de regra de negocio, com uma mensagem pronta para mostrar ao usuario.
 */
public class RegraException extends RuntimeException {

    public RegraException(String mensagem) {
        super(mensagem);
    }

    /**
     * Atalho das validacoes: quebra a regra quando a condicao e verdadeira.
     */
    public static void se(boolean condicao, String mensagem) {
        if (condicao)
            throw new RegraException(mensagem);
    }
}
