package locadora.model;

import java.math.BigDecimal;

/**
 * Titulo em Blu-ray: locacao de R$ 12,00 e multa de R$ 5,00 por dia de atraso.
 */
public class BluRay extends Titulo {

    public static final String TIPO = "Blu-ray";
    private static final BigDecimal MULTA_DIARIA = new BigDecimal("5.00");
    private static final BigDecimal LOCACAO = new BigDecimal("12.00");

    public BluRay(int id, String nome, String descricao, int ano, int prazoMaximoDias) {
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
        return "filme";
    }
}
