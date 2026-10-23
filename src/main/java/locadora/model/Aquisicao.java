package locadora.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Aquisicao {

    private int id;
    private final Titulo titulo;
    private final int quantidade;
    private final BigDecimal custoUnitario;
    private final String fornecedor;
    private final LocalDate data;
    private final Funcionario funcionario;

    public Aquisicao(int id, Titulo titulo, int quantidade, BigDecimal custoUnitario, String fornecedor,
                     LocalDate data, Funcionario funcionario) {
        this.id = id;
        this.titulo = titulo;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.fornecedor = fornecedor;
        this.data = data;
        this.funcionario = funcionario;
    }

    public BigDecimal custoTotal() {
        return BigDecimal.ZERO;
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

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public LocalDate getData() {
        return data;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }
}
