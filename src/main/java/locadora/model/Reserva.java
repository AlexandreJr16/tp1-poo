package locadora.model;

import java.time.LocalDate;

public class Reserva {

    private final int id;
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
        return new Reserva(0, titulo, cliente, funcionario, data, SituacaoReserva.ATENDIDA, null);
    }

    public boolean ativa() {
        return false;
    }

    public void preparar(Exemplar exemplar) {
    }

    public void atender() {
    }

    public void cancelar() {
    }

    public void voltarParaFila() {
    }

    public int getId() {
        return id;
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
