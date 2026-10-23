package locadora.dao;

import locadora.model.Funcionario;

import java.util.List;

public class FuncionarioDao {

    private final Conexao conexao;

    public FuncionarioDao(Conexao conexao) {
        this.conexao = conexao;
    }

    public void inserir(Funcionario funcionario) {
    }

    public void atualizar(Funcionario funcionario) {
    }

    public Funcionario buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public Funcionario buscarPorLogin(String login) {
        throw new UnsupportedOperationException();
    }

    public Funcionario buscarPorCpf(String cpf) {
        throw new UnsupportedOperationException();
    }

    public List<Funcionario> listar() {
        throw new UnsupportedOperationException();
    }
}
