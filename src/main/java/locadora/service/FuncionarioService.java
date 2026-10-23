package locadora.service;

import locadora.dao.FuncionarioDao;
import locadora.model.Cargo;
import locadora.model.Funcionario;

import java.util.List;

public class FuncionarioService {

    public FuncionarioService(FuncionarioDao funcionarios) {
    }

    public Funcionario cadastrar(String nome, String cpf, String telefone, String email, String login, String senha, Cargo cargo) {
        throw new UnsupportedOperationException();
    }

    public void atualizar(int id, String nome, String cpf, String telefone, String email, Cargo cargo) {
        throw new UnsupportedOperationException();
    }

    public void trocarSenha(String senhaAtual, String novaSenha) {
        throw new UnsupportedOperationException();
    }

    public void redefinirSenha(int funcionarioId, String novaSenha) {
        throw new UnsupportedOperationException();
    }

    public List<Funcionario> listar() {
        throw new UnsupportedOperationException();
    }
}
