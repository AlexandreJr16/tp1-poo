package locadora.dao;

import locadora.model.Cargo;
import locadora.model.Funcionario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/* - Cadastro:
     inserir: grava um funcionario novo (com hash e sal) e preenche o id
     atualizar: grava dados, cargo, hash e sal
   - Consulta:
     buscar: funcionario pelo id
     buscarPorLogin: funcionario pelo login, ou nulo
     buscarPorCpf: funcionario pelo cpf, ou nulo
     listar: todos os funcionarios em ordem de nome */
public class FuncionarioDao {

    private static final String SELECIONAR = "SELECT id, nome, cpf, telefone, email, login, senha_hash, sal, cargo FROM funcionario";

    private final Conexao conexao;

    public FuncionarioDao(Conexao conexao) {
        this.conexao = conexao;
    }

    public void inserir(Funcionario funcionario) {
        funcionario.setId(conexao.inserir(
                "INSERT INTO funcionario (nome, cpf, telefone, email, login, senha_hash, sal, cargo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                funcionario.getNome(), funcionario.getCpf(), funcionario.getTelefone(), funcionario.getEmail(), funcionario.getLogin(),
                funcionario.getSenhaHash(), funcionario.getSal(), funcionario.getCargo()));
    }

    public void atualizar(Funcionario funcionario) {
        conexao.executar("UPDATE funcionario SET nome = ?, cpf = ?, telefone = ?, email = ?, login = ?, senha_hash = ?, sal = ?, cargo = ? "
                        + "WHERE id = ?",
                funcionario.getNome(), funcionario.getCpf(), funcionario.getTelefone(), funcionario.getEmail(), funcionario.getLogin(),
                funcionario.getSenhaHash(), funcionario.getSal(), funcionario.getCargo(), funcionario.getId());
    }

    public Funcionario buscar(int id) {
        return conexao.transacao(c -> buscar(c, id));
    }

    public Funcionario buscarPorLogin(String login) {
        return conexao.buscar(SELECIONAR + " WHERE login = ?", FuncionarioDao::ler, login);
    }

    public Funcionario buscarPorCpf(String cpf) {
        return conexao.buscar(SELECIONAR + " WHERE cpf = ?", FuncionarioDao::ler, cpf);
    }

    public List<Funcionario> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY nome COLLATE NOCASE", FuncionarioDao::ler);
    }

    Funcionario buscar(Connection c, int id) throws SQLException {
        return Conexao.buscar(c, SELECIONAR + " WHERE id = ?", FuncionarioDao::ler, id);
    }

    private static Funcionario ler(ResultSet rs, Connection c) throws SQLException {
        return new Funcionario(rs.getInt("id"), rs.getString("nome"), rs.getString("cpf"), rs.getString("telefone"),
                rs.getString("email"), rs.getString("login"), rs.getString("senha_hash"), rs.getString("sal"),
                Cargo.valueOf(rs.getString("cargo")));
    }
}
