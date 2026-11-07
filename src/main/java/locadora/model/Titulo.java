package locadora.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Titulo do acervo (filme ou album). Cada tipo de midia e uma subclasse que define o valor da locacao e da multa diaria.
 */
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

    /**
     * Multa cobrada por dia de atraso, definida por cada tipo de midia.
     */
    public abstract BigDecimal valorMultaDiaria();

    /**
     * Preco da locacao, definido por cada tipo de midia.
     */
    public abstract BigDecimal valorLocacao();

    /**
     * Nome do tipo de midia, como aparece na tela e no banco.
     */
    public abstract String tipo();

    /**
     * O tipo de obra gravada na midia (filme ou album), usado para buscar a capa.
     */
    public abstract String categoria();

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

    /**
     * Data de devolucao contando o prazo maximo a partir do inicio.
     */
    public LocalDate devolucaoPrevista(LocalDate inicio) {
        return inicio.plusDays(prazoMaximoDias);
    }
}
