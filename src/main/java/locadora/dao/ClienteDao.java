package locadora.dao;

import locadora.model.Cliente;

import java.util.List;

public class ClienteDao {

    private final Conexao conexao;

    public ClienteDao(Conexao conexao) {
        this.conexao = conexao;
    }

    public void inserir(Cliente cliente) {
    }

    public void atualizar(Cliente cliente) {
    }

    public Cliente buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public Cliente buscarPorCpf(String cpf) {
        throw new UnsupportedOperationException();
    }

    public List<Cliente> listar() {
        throw new UnsupportedOperationException();
    }
}
