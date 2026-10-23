package locadora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Locacao {

    private static final BigDecimal SEM_MULTA = new BigDecimal("0.00");

    private final int id;
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

    public static Locacao abrir(Exemplar exemplar, Cliente cliente, Funcionario funcionario, LocalDate data) {
        exemplar.alugar();
        Titulo titulo = exemplar.getTitulo();
        return new Locacao(0, exemplar, cliente, funcionario, null, data, data.plusDays(titulo.getPrazoMaximoDias()), null,
                titulo.valorLocacao(), SEM_MULTA, null, false);
    }

    public boolean ativa() {
        return dataDevolucao == null;
    }

    public boolean atrasada(LocalDate hoje) {
        return ativa() && hoje.isAfter(dataPrevista);
    }

    public long diasAtraso(LocalDate ate) {
        return Math.max(0, ChronoUnit.DAYS.between(dataPrevista, ate));
    }

    public BigDecimal multaAte(LocalDate data) {
        return exemplar.getTitulo().valorMultaDiaria().multiply(BigDecimal.valueOf(diasAtraso(data)));
    }

    public void devolver(LocalDate data, Funcionario funcionario) {
        exigirAtiva();
        exemplar.devolver();
        encerrar(data, funcionario, multaAte(data));
    }

    // perda com o exemplar na mao do cliente: cobra o custo do exemplar mais a multa ate a data
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

    public boolean multaPendente() {
        return multa.signum() > 0 && dataPagamentoMulta == null;
    }

    public boolean multaPaga() {
        return dataPagamentoMulta != null;
    }

    public void pagarMulta(LocalDate data) {
        if (!multaPendente())
            throw new IllegalStateException("Não há multa pendente nesta locação.");
        dataPagamentoMulta = data;
    }

    public boolean podeRenovar(LocalDate hoje) {
        return ativa() && !renovada && !hoje.isAfter(dataPrevista);
    }

    public void renovar(LocalDate hoje) {
        if (!podeRenovar(hoje))
            throw new IllegalStateException("Esta locação não pode ser renovada.");
        dataPrevista = dataPrevista.plusDays(exemplar.getTitulo().getPrazoMaximoDias());
        renovada = true;
    }

    public int getId() {
        return id;
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
