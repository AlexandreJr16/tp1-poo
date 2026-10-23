package locadora.service;

import locadora.dao.FuncionarioDao;
import locadora.model.Funcionario;

public class AuthService {

    private final FuncionarioDao funcionarios;

    public AuthService(FuncionarioDao funcionarios) {
        this.funcionarios = funcionarios;
    }

    public Funcionario entrar(String login, String senha) {
        throw new UnsupportedOperationException();
    }

    public static Funcionario logado() {
        return null;
    }

    public static void sair() {
    }

    public static Funcionario exigirLogado() {
        throw new UnsupportedOperationException();
    }

    public static Funcionario exigirGerente() {
        throw new UnsupportedOperationException();
    }
}
