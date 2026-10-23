package locadora.model;

import java.math.BigDecimal;

public abstract class Titulo {

    private int id;
    private final String nome;
    private final String descricao;
    private final int ano;
    private final int prazoMaximoDias;

    protected Titulo(int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ano = ano;
        this.prazoMaximoDias = prazoMaximoDias;
    }

    public abstract BigDecimal valorMultaDiaria();

    public abstract BigDecimal valorLocacao();

    public abstract String tipo();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getAno() {
        return ano;
    }

    public int getPrazoMaximoDias() {
        return prazoMaximoDias;
    }
}
