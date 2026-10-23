package locadora.model;

import java.time.LocalDate;

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

    public static Reserva abrir(Titulo titulo, Cliente cliente, Funcionario funcionario, LocalDate data) {
        return new Reserva(0, titulo, cliente, funcionario, data, SituacaoReserva.AGUARDANDO, null);
    }

    public boolean ativa() {
        return situacao == SituacaoReserva.AGUARDANDO || situacao == SituacaoReserva.PRONTA;
    }

    public void preparar(Exemplar exemplar) {
        exigir(situacao == SituacaoReserva.AGUARDANDO);
        exemplar.reservar();
        this.exemplar = exemplar;
        situacao = SituacaoReserva.PRONTA;
    }

    public void atender() {
        exigir(situacao == SituacaoReserva.PRONTA);
        situacao = SituacaoReserva.ATENDIDA;
    }

    public void cancelar() {
        exigir(ativa());
        if (situacao == SituacaoReserva.PRONTA)
            exemplar.liberar();
        situacao = SituacaoReserva.CANCELADA;
    }

    // usado quando o exemplar separado se perde: a reserva volta a esperar na mesma posicao
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
