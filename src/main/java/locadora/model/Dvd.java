package locadora.model;

import java.math.BigDecimal;

public class Dvd extends Titulo {

    public static final String TIPO = "DVD";
    private static final BigDecimal MULTA_DIARIA = new BigDecimal("3.50");
    private static final BigDecimal LOCACAO = new BigDecimal("8.00");

    public Dvd(int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        super(id, nome, descricao, ano, prazoMaximoDias);
    }

    @Override
    public BigDecimal valorMultaDiaria() {
        return MULTA_DIARIA;
    }

    @Override
    public BigDecimal valorLocacao() {
        return LOCACAO;
    }

    @Override
    public String tipo() {
        return TIPO;
    }
}
