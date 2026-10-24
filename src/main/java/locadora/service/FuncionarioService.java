package locadora.service;

import locadora.dao.FuncionarioDao;
import locadora.model.Cargo;
import locadora.model.Funcionario;
import locadora.model.Pessoa;

import java.util.List;

/* - Cadastro (so gerente):
     cadastrar: valida e grava um funcionario novo com a senha em hash
     atualizar: grava dados e cargo, mantendo login e senha; nunca deixa a locadora sem gerente
     redefinirSenha: troca a senha de outro funcionario
   - Conta do logado:
     trocarSenha: confere a senha atual e grava a nova
   - Consulta:
     listar: todos os funcionarios em ordem de nome */
public class FuncionarioService {

    public static final int TAMANHO_MINIMO_SENHA = 6;

    private final FuncionarioDao funcionarios;

    public FuncionarioService(FuncionarioDao funcionarios) {
        this.funcionarios = funcionarios;
    }

    public Funcionario cadastrar(String nome, String cpf, String telefone, String email, String login, String senha, Cargo cargo) {
        AuthService.exigirGerente();
        String loginLimpo = AuthService.normalizarLogin(login);
        Funcionario novo = new Funcionario(0, nome.trim(), cpf, telefone.trim(), email.trim(), loginLimpo, null, null, cargo);
        validar(novo);
        RegraException.se(loginLimpo.isEmpty(), "Informe o login.");
        RegraException.se(funcionarios.buscarPorLogin(loginLimpo) != null, "Este login já está em uso.");
        validarSenha(senha);
        novo.definirSenha(senha);
        funcionarios.inserir(novo);
        return novo;
    }

    public void atualizar(int id, String nome, String cpf, String telefone, String email, Cargo cargo) {
        AuthService.exigirGerente();
        Funcionario atual = funcionarios.buscar(id);
        RegraException.se(atual == null, "Funcionário não encontrado.");
        Funcionario editado = new Funcionario(id, nome.trim(), cpf, telefone.trim(), email.trim(), atual.getLogin(),
                atual.getSenhaHash(), atual.getSal(), cargo);
        validar(editado);
        RegraException.se(atual.gerente() && !editado.gerente() && quantidadeDeGerentes() == 1,
                "A locadora precisa de pelo menos um gerente.");
        funcionarios.atualizar(editado);
    }

    public void redefinirSenha(int funcionarioId, String novaSenha) {
        AuthService.exigirGerente();
        validarSenha(novaSenha);
        Funcionario funcionario = funcionarios.buscar(funcionarioId);
        funcionario.definirSenha(novaSenha);
        funcionarios.atualizar(funcionario);
    }

    public void trocarSenha(String senhaAtual, String novaSenha) {
        Funcionario logado = funcionarios.buscar(AuthService.exigirLogado().getId());
        RegraException.se(!logado.senhaConfere(senhaAtual), "A senha atual está incorreta.");
        validarSenha(novaSenha);
        logado.definirSenha(novaSenha);
        funcionarios.atualizar(logado);
    }

    public List<Funcionario> listar() {
        return funcionarios.listar();
    }

    private void validar(Funcionario funcionario) {
        RegraException.se(funcionario.getNome().isEmpty(), "Informe o nome do funcionário.");
        RegraException.se(!Pessoa.cpfValido(funcionario.getCpf()), "CPF inválido.");
        Funcionario mesmoCpf = funcionarios.buscarPorCpf(funcionario.getCpf());
        RegraException.se(mesmoCpf != null && mesmoCpf.getId() != funcionario.getId(), "Já existe um funcionário com este CPF.");
        RegraException.se(!Pessoa.emailValido(funcionario.getEmail()), "E-mail inválido.");
        RegraException.se(funcionario.getCargo() == null, "Escolha o cargo.");
    }

    private static void validarSenha(String senha) {
        RegraException.se(senha == null || senha.length() < TAMANHO_MINIMO_SENHA,
                "A senha precisa ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
    }

    private long quantidadeDeGerentes() {
        return funcionarios.listar().stream().filter(Funcionario::gerente).count();
    }
}
