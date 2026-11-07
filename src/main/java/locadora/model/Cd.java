package locadora.model;

import java.math.BigDecimal;

/**
 * Titulo em CD (album): locacao de R$ 5,00 e multa de R$ 2,00 por dia de atraso.
 */
public class Cd extends Titulo {

    public static final String TIPO = "CD";
    private static final BigDecimal MULTA_DIARIA = new BigDecimal("2.00");
    private static final BigDecimal LOCACAO = new BigDecimal("5.00");

    public Cd(int id, String nome, String descricao, int ano, int prazoMaximoDias) {
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

    @Override
    public String categoria() {
        return "álbum";
    }
}
