package locadora.model;

import java.time.LocalDate;

public class Perda {

    private int id;
    private final Exemplar exemplar;
    private final String motivo;
    private final LocalDate data;
    private final Funcionario funcionario;
    private final Locacao locacao;

    public Perda(int id, Exemplar exemplar, String motivo, LocalDate data, Funcionario funcionario, Locacao locacao) {
        this.id = id;
        this.exemplar = exemplar;
        this.motivo = motivo;
        this.data = data;
        this.funcionario = funcionario;
        this.locacao = locacao;
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

    public String getMotivo() {
        return motivo;
    }

    public LocalDate getData() {
        return data;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public Locacao getLocacao() {
        return locacao;
    }
}
