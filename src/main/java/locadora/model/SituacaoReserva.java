package locadora.model;

/**
 * Etapas de uma reserva: aguardando na fila, pronta com exemplar separado, atendida ou cancelada.
 */
public enum SituacaoReserva {
    AGUARDANDO("Aguardando"),
    PRONTA("Pronta para retirada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    private final String rotulo;

    SituacaoReserva(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
