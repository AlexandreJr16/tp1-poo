package locadora.service;

public class RegraException extends RuntimeException {

    public RegraException(String mensagem) {
        super(mensagem);
    }

    // atalho das validacoes: quebra a regra quando a condicao e verdadeira
    public static void se(boolean condicao, String mensagem) {
        if (condicao)
            throw new RegraException(mensagem);
    }
}
