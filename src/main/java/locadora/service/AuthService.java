package locadora.service;

import locadora.dao.FuncionarioDao;
import locadora.model.Funcionario;

import java.util.Locale;

/**
 * Login e permissoes. Guarda o funcionario logado, unico estado global do sistema.
 */
public class AuthService {

    private static Funcionario logado;

    private final FuncionarioDao funcionarios;

    public AuthService(FuncionarioDao funcionarios) {
        this.funcionarios = funcionarios;
    }

    /**
     * Confere login e senha e abre a sessao.
     */
    public Funcionario entrar(String login, String senha) {
        Funcionario funcionario = funcionarios.buscarPorLogin(normalizarLogin(login));
        if (funcionario == null || !funcionario.senhaConfere(senha))
            throw new RegraException("Login ou senha incorretos.");
        logado = funcionario;
        return funcionario;
    }

    /**
     * Encerra a sessao.
     */
    public static void sair() {
        logado = null;
    }

    /**
     * Funcionario da sessao, ou nulo.
     */
    public static Funcionario logado() {
        return logado;
    }

    /**
     * Funcionario da sessao, ou erro se ninguem entrou.
     */
    public static Funcionario exigirLogado() {
        if (logado == null)
            throw new RegraException("Faça login para continuar.");
        return logado;
    }

    /**
     * Funcionario da sessao, ou erro se nao for gerente.
     */
    public static Funcionario exigirGerente() {
        if (!exigirLogado().gerente())
            throw new RegraException("Apenas gerentes podem fazer isso.");
        return logado;
    }

    static String normalizarLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }
}
