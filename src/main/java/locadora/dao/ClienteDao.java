package locadora.dao;

import locadora.model.Cliente;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acesso a tabela de clientes.
 */
public class ClienteDao {

    private static final String SELECIONAR = "SELECT id, nome, cpf, telefone, email, endereco, ativo FROM cliente";

    private final Conexao conexao;

    public ClienteDao(Conexao conexao) {
        this.conexao = conexao;
    }

    /**
     * Grava um cliente novo e preenche o id.
     */
    public void inserir(Cliente cliente) {
        cliente.setId(conexao.inserir("INSERT INTO cliente (nome, cpf, telefone, email, endereco, ativo) VALUES (?, ?, ?, ?, ?, ?)",
                cliente.getNome(), cliente.getCpf(), cliente.getTelefone(), cliente.getEmail(), cliente.getEndereco(), cliente.isAtivo()));
    }

    /**
     * Grava os dados de contato e se o cliente esta ativo.
     */
    public void atualizar(Cliente cliente) {
        conexao.executar("UPDATE cliente SET nome = ?, cpf = ?, telefone = ?, email = ?, endereco = ?, ativo = ? WHERE id = ?",
                cliente.getNome(), cliente.getCpf(), cliente.getTelefone(), cliente.getEmail(), cliente.getEndereco(), cliente.isAtivo(),
                cliente.getId());
    }

    /**
     * Cliente pelo id.
     */
    public Cliente buscar(int id) {
        return conexao.transacao(c -> buscar(c, id));
    }

    /**
     * Cliente pelo cpf (so digitos), ou nulo.
     */
    public Cliente buscarPorCpf(String cpf) {
        return conexao.buscar(SELECIONAR + " WHERE cpf = ?", ClienteDao::ler, cpf);
    }

    /**
     * Todos os clientes em ordem de nome.
     */
    public List<Cliente> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY nome COLLATE NOCASE", ClienteDao::ler);
    }

    /**
     * Cliente pelo id.
     */
    Cliente buscar(Connection c, int id) throws SQLException {
        return Conexao.buscar(c, SELECIONAR + " WHERE id = ?", ClienteDao::ler, id);
    }

    private static Cliente ler(ResultSet rs, Connection c) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getString("cpf"), rs.getString("telefone"),
                rs.getString("email"), rs.getString("endereco"), rs.getBoolean("ativo"));
    }
}
