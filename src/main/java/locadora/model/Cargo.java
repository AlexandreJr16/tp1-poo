package locadora.model;

/**
 * Cargo do funcionario. O gerente pode tudo; o atendente nao registra aquisicoes, perdas nem funcionarios.
 */
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
