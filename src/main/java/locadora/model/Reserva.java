package locadora.model;

import java.time.LocalDate;

/**
 * Lugar na fila de espera de um cliente por um titulo sem exemplar disponivel.
 */
public class Reserva {

    private int id;
    private final Titulo titulo;
    private final Cliente cliente;
    private final Funcionario funcionario;
    private final LocalDate data;
    private SituacaoReserva situacao;
    private Exemplar exemplar;

    public Reserva(int id, Titulo titulo, Cliente cliente, Funcionario funcionario, LocalDate data,
                   SituacaoReserva situacao, Exemplar exemplar) {
        this.id = id;
        this.titulo = titulo;
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.data = data;
        this.situacao = situacao;
        this.exemplar = exemplar;
    }

    /**
     * Cria uma reserva nova aguardando na fila.
     */
    public static Reserva abrir(Titulo titulo, Cliente cliente, Funcionario funcionario, LocalDate data) {
        return new Reserva(0, titulo, cliente, funcionario, data, SituacaoReserva.AGUARDANDO, null);
    }

    /**
     * Ainda aguardando ou pronta para retirada.
     */
    public boolean ativa() {
        return situacao == SituacaoReserva.AGUARDANDO || situacao == SituacaoReserva.PRONTA;
    }

    /**
     * Separa um exemplar para esta reserva, que fica pronta para retirada.
     */
    public void preparar(Exemplar exemplar) {
        exigir(situacao == SituacaoReserva.AGUARDANDO);
        exemplar.reservar();
        this.exemplar = exemplar;
        situacao = SituacaoReserva.PRONTA;
    }

    /**
     * Marca como atendida quando o cliente retira o exemplar.
     */
    public void atender() {
        exigir(situacao == SituacaoReserva.PRONTA);
        situacao = SituacaoReserva.ATENDIDA;
    }

    /**
     * Cancela a reserva e, se havia exemplar separado, libera ele.
     */
    public void cancelar() {
        exigir(ativa());
        if (situacao == SituacaoReserva.PRONTA)
            exemplar.liberar();
        situacao = SituacaoReserva.CANCELADA;
    }

    /**
     * Usado quando o exemplar separado se perde: a reserva volta a esperar na mesma posicao.
     */
    public void voltarParaFila() {
        exigir(situacao == SituacaoReserva.PRONTA);
        exemplar = null;
        situacao = SituacaoReserva.AGUARDANDO;
    }

    private void exigir(boolean permitido) {
        if (!permitido)
            throw new IllegalStateException("A reserva está " + situacao.toString().toLowerCase() + " e não permite esta operação.");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Titulo getTitulo() {
        return titulo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public LocalDate getData() {
        return data;
    }

    public SituacaoReserva getSituacao() {
        return situacao;
    }

    public Exemplar getExemplar() {
        return exemplar;
    }
}
