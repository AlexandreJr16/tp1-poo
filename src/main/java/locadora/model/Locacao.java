package locadora.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Locacao {

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
        return new Locacao(0, exemplar, cliente, funcionario, null, data, data, null, BigDecimal.ZERO, BigDecimal.ZERO, null, false);
    }

    public boolean ativa() {
        return false;
    }

    public boolean atrasada(LocalDate hoje) {
        return false;
    }

    public long diasAtraso(LocalDate ate) {
        return 0;
    }

    public BigDecimal multaAte(LocalDate data) {
        return BigDecimal.ZERO;
    }

    public void devolver(LocalDate data, Funcionario funcionario) {
    }

    public void encerrarPorPerda(LocalDate data, Funcionario funcionario) {
    }

    public boolean multaPendente() {
        return false;
    }

    public boolean multaPaga() {
        return false;
    }

    public void pagarMulta(LocalDate data) {
    }

    public boolean podeRenovar(LocalDate hoje) {
        return false;
    }

    public void renovar(LocalDate hoje) {
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
