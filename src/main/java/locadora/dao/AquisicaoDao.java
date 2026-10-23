package locadora.dao;

import locadora.model.Aquisicao;
import locadora.model.Exemplar;
import locadora.model.Reserva;

import java.util.List;

public class AquisicaoDao {

    private final Conexao conexao;

    public AquisicaoDao(Conexao conexao, TituloDao titulos, FuncionarioDao funcionarios, ExemplarDao exemplares, ReservaDao reservas) {
        this.conexao = conexao;
    }

    public void registrar(Aquisicao aquisicao, List<Exemplar> novos, List<Reserva> preparadas) {
    }

    public List<Aquisicao> listar() {
        throw new UnsupportedOperationException();
    }

    public Aquisicao buscarDoExemplar(int exemplarId) {
        throw new UnsupportedOperationException();
    }
}
