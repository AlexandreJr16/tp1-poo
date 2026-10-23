package locadora.dao;

import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.Situacao;

import java.util.List;
import java.util.Map;

public class ExemplarDao {

    private final Conexao conexao;
    private final TituloDao titulos;

    public ExemplarDao(Conexao conexao, TituloDao titulos) {
        this.conexao = conexao;
        this.titulos = titulos;
    }

    public Exemplar buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public Exemplar buscarPorCodigo(String codigo) {
        throw new UnsupportedOperationException();
    }

    public List<Exemplar> listarPorTitulo(int tituloId) {
        throw new UnsupportedOperationException();
    }

    public List<Exemplar> listarPorSituacao(Situacao situacao) {
        throw new UnsupportedOperationException();
    }

    public Map<Integer, Estoque> estoquePorTitulo() {
        throw new UnsupportedOperationException();
    }

    public int proximaSequencia(int tituloId) {
        return 1;
    }

    public void atualizarSituacao(Exemplar exemplar) {
        throw new UnsupportedOperationException();
    }
}
