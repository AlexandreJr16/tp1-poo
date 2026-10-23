package locadora.model;

public record Estoque(int disponiveis, int alugados, int reservados, int perdidos) {

    public static final Estoque VAZIO = new Estoque(0, 0, 0, 0);

    public int total() {
        return disponiveis + alugados + reservados + perdidos;
    }

    public int noAcervo() {
        return total() - perdidos;
    }
}
