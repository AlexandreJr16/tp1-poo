package locadora.service;

import locadora.dao.FuncionarioDao;
import locadora.model.Funcionario;

import java.util.Locale;

/* - Sessao (unico estado global do sistema: o funcionario logado):
     entrar: confere login e senha e abre a sessao
     sair: encerra a sessao
     logado: funcionario da sessao, ou nulo
   - Permissao:
     exigirLogado: funcionario da sessao, ou erro se ninguem entrou
     exigirGerente: funcionario da sessao, ou erro se nao for gerente */
public class AuthService {

    private static Funcionario logado;

    private final FuncionarioDao funcionarios;

    public AuthService(FuncionarioDao funcionarios) {
        this.funcionarios = funcionarios;
    }

    public Funcionario entrar(String login, String senha) {
        Funcionario funcionario = funcionarios.buscarPorLogin(normalizarLogin(login));
        if (funcionario == null || !funcionario.senhaConfere(senha))
            throw new RegraException("Login ou senha incorretos.");
        logado = funcionario;
        return funcionario;
    }

    public static void sair() {
        logado = null;
    }

    public static Funcionario logado() {
        return logado;
    }

    public static Funcionario exigirLogado() {
        if (logado == null)
            throw new RegraException("Faça login para continuar.");
        return logado;
    }

    public static Funcionario exigirGerente() {
        if (!exigirLogado().gerente())
            throw new RegraException("Apenas gerentes podem fazer isso.");
        return logado;
    }

    static String normalizarLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }
}
