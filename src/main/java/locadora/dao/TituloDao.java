package locadora.dao;

import locadora.model.Titulo;

import java.util.List;

public class TituloDao {

    private final Conexao conexao;

    public TituloDao(Conexao conexao) {
        this.conexao = conexao;
    }

    public static Titulo instanciar(String tipo, int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        throw new UnsupportedOperationException();
    }

    public void inserir(Titulo titulo) {
    }

    public void atualizar(Titulo titulo) {
    }

    public Titulo buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public List<Titulo> listar() {
        throw new UnsupportedOperationException();
    }
}
