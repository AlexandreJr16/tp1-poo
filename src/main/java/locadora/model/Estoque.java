package locadora.model;

/**
 * Quantidade de exemplares de um titulo em cada situacao.
 */
public record Estoque(int disponiveis, int alugados, int reservados, int perdidos) {

    public static final Estoque VAZIO = new Estoque(0, 0, 0, 0);

    /**
     * Todos os exemplares, inclusive os perdidos.
     */
    public int total() {
        return disponiveis + alugados + reservados + perdidos;
    }

    /**
     * Exemplares que ainda existem na locadora (total menos os perdidos).
     */
    public int noAcervo() {
        return total() - perdidos;
    }
}
