package locadora.model;

public enum Cargo {
    GERENTE("Gerente"),
    ATENDENTE("Atendente");

    private final String rotulo;

    Cargo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
