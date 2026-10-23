package locadora.model;

public enum Situacao {
    DISPONIVEL("Disponível"),
    ALUGADO("Alugado"),
    RESERVADO("Reservado"),
    PERDIDO("Perdido");

    private final String rotulo;

    Situacao(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
