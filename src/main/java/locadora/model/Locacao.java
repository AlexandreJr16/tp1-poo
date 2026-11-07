package locadora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Locacao de um exemplar para um cliente, com o funcionario que alugou e o que recebeu a devolucao. Calcula prazo, atraso e multa.
 */
public class Locacao {

    public static final BigDecimal SEM_MULTA = new BigDecimal("0.00");

    private int id;
    private final Exemplar exemplar;
    private final Cliente cliente;
    private final Funcionario funcionarioLocacao;
    private Funcionario funcionarioDevolucao;
    private final LocalDate dataLocacao;
    private LocalDate dataPrevista;
    private LocalDate dataDevolucao;
    private final BigDecimal valorLocacao;
    private BigDecimal multa;
    private LocalDate dataPagamentoMulta;
    private boolean renovada;

    public Locacao(int id, Exemplar exemplar, Cliente cliente, Funcionario funcionarioLocacao, Funcionario funcionarioDevolucao,
                   LocalDate dataLocacao, LocalDate dataPrevista, LocalDate dataDevolucao, BigDecimal valorLocacao,
                   BigDecimal multa, LocalDate dataPagamentoMulta, boolean renovada) {
        this.id = id;
        this.exemplar = exemplar;
        this.cliente = cliente;
        this.funcionarioLocacao = funcionarioLocacao;
        this.funcionarioDevolucao = funcionarioDevolucao;
        this.dataLocacao = dataLocacao;
        this.dataPrevista = dataPrevista;
        this.dataDevolucao = dataDevolucao;
        this.valorLocacao = valorLocacao;
        this.multa = multa;
        this.dataPagamentoMulta = dataPagamentoMulta;
        this.renovada = renovada;
    }

    /**
     * Abre uma locacao nova: aluga o exemplar e calcula a data prevista pelo prazo maximo do titulo.
     */
    public static Locacao abrir(Exemplar exemplar, Cliente cliente, Funcionario funcionario, LocalDate data) {
        exemplar.alugar();
        Titulo titulo = exemplar.getTitulo();
        return new Locacao(0, exemplar, cliente, funcionario, null, data, titulo.devolucaoPrevista(data), null,
                titulo.valorLocacao(), SEM_MULTA, null, false);
    }

    /**
     * Verdadeiro enquanto o exemplar nao foi devolvido.
     */
    public boolean ativa() {
        return dataDevolucao == null;
    }

    /**
     * Ativa e com a data prevista ja vencida.
     */
    public boolean atrasada(LocalDate hoje) {
        return ativa() && hoje.isAfter(dataPrevista);
    }

    /**
     * Dias depois da data prevista ate a data informada (zero se nao atrasou).
     */
    public long diasAtraso(LocalDate ate) {
        return Math.max(0, ChronoUnit.DAYS.between(dataPrevista, ate));
    }

    /**
     * Multa por atraso ate a data: dias de atraso vezes a multa diaria do tipo de midia.
     */
    public BigDecimal multaAte(LocalDate data) {
        return exemplar.getTitulo().valorMultaDiaria().multiply(BigDecimal.valueOf(diasAtraso(data)));
    }

    /**
     * Fecha a locacao, guardando o funcionario da devolucao e a multa do atraso.
     */
    public void devolver(LocalDate data, Funcionario funcionario) {
        exigirAtiva();
        exemplar.devolver();
        encerrar(data, funcionario, multaAte(data));
    }

    /**
     * Perda com o exemplar na mao do cliente: cobra o custo do exemplar mais a multa ate a data.
     */
    public void encerrarPorPerda(LocalDate data, Funcionario funcionario) {
        exigirAtiva();
        exemplar.perder();
        encerrar(data, funcionario, multaAte(data).add(exemplar.getCusto()));
    }

    private void encerrar(LocalDate data, Funcionario funcionario, BigDecimal valor) {
        dataDevolucao = data;
        funcionarioDevolucao = funcionario;
        multa = valor;
    }

    private void exigirAtiva() {
        if (!ativa())
            throw new IllegalStateException("Esta locação já foi encerrada.");
    }

    /**
     * Tem multa e ela ainda nao foi paga.
     */
    public boolean multaPendente() {
        return multa.signum() > 0 && dataPagamentoMulta == null;
    }

    public boolean multaPaga() {
        return dataPagamentoMulta != null;
    }

    /**
     * Registra o pagamento da multa pendente na data.
     */
    public void pagarMulta(LocalDate data) {
        if (!multaPendente())
            throw new IllegalStateException("Não há multa pendente nesta locação.");
        dataPagamentoMulta = data;
    }

    /**
     * So renova uma vez e antes de atrasar.
     */
    public boolean podeRenovar(LocalDate hoje) {
        return ativa() && !renovada && !hoje.isAfter(dataPrevista);
    }

    /**
     * Estende a data prevista por mais um prazo do titulo.
     */
    public void renovar(LocalDate hoje) {
        if (!podeRenovar(hoje))
            throw new IllegalStateException("Esta locação não pode ser renovada.");
        dataPrevista = exemplar.getTitulo().devolucaoPrevista(dataPrevista);
        renovada = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Exemplar getExemplar() {
        return exemplar;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Funcionario getFuncionarioLocacao() {
        return funcionarioLocacao;
    }

    public Funcionario getFuncionarioDevolucao() {
        return funcionarioDevolucao;
    }

    public LocalDate getDataLocacao() {
        return dataLocacao;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public BigDecimal getValorLocacao() {
        return valorLocacao;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public LocalDate getDataPagamentoMulta() {
        return dataPagamentoMulta;
    }

    public boolean isRenovada() {
        return renovada;
    }
}
