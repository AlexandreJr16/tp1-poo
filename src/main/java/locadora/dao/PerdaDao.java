package locadora.dao;

import locadora.model.Perda;
import locadora.model.Reserva;

import java.util.List;

public class PerdaDao {

    private final Conexao conexao;

    public PerdaDao(Conexao conexao, ExemplarDao exemplares, FuncionarioDao funcionarios, LocacaoDao locacoes, ReservaDao reservas) {
        this.conexao = conexao;
    }

    public void registrar(Perda perda, Reserva devolvidaAFila) {
    }

    public List<Perda> listar() {
        throw new UnsupportedOperationException();
    }

    public Perda doExemplar(int exemplarId) {
        throw new UnsupportedOperationException();
    }
}
