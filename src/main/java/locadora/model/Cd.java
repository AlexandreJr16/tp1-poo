package locadora.model;

import java.math.BigDecimal;

public class Cd extends Titulo {

    public Cd(int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        super(id, nome, descricao, ano, prazoMaximoDias);
    }

    @Override
    public BigDecimal valorMultaDiaria() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal valorLocacao() {
        return BigDecimal.ZERO;
    }

    @Override
    public String tipo() {
        return "";
    }
}
