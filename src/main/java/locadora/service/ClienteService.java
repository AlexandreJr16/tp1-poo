package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.model.Cliente;
import locadora.model.Locacao;
import locadora.model.Pessoa;
import locadora.model.Reserva;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regras do cadastro de clientes: CPF valido e unico, e desativacao so pelo gerente.
 */
public class ClienteService {

    private final ClienteDao clientes;
    private final LocacaoDao locacoes;
    private final ReservaDao reservas;

    public ClienteService(ClienteDao clientes, LocacaoDao locacoes, ReservaDao reservas) {
        this.clientes = clientes;
        this.locacoes = locacoes;
        this.reservas = reservas;
    }

    /**
     * Valida e grava um cliente novo (id 0) ou editado; desativar exige gerente.
     */
    public Cliente salvar(Cliente cliente) {
        AuthService.exigirLogado();
        Cliente limpo = new Cliente(cliente.getId(), cliente.getNome().trim(), cliente.getCpf(), cliente.getTelefone().trim(),
                cliente.getEmail().trim(), cliente.getEndereco().trim(), cliente.isAtivo());
        RegraException.se(limpo.getNome().isEmpty(), "Informe o nome do cliente.");
        RegraException.se(!Pessoa.cpfValido(limpo.getCpf()), "CPF inválido.");
        Cliente mesmoCpf = clientes.buscarPorCpf(limpo.getCpf());
        RegraException.se(mesmoCpf != null && mesmoCpf.getId() != limpo.getId(), "Já existe um cliente com este CPF.");
        RegraException.se(!Pessoa.emailValido(limpo.getEmail()), "E-mail inválido.");
        if (limpo.getId() == 0) {
            clientes.inserir(limpo);
        } else {
            if (!limpo.isAtivo() && clientes.buscar(limpo.getId()).isAtivo())
                AuthService.exigirGerente();
            clientes.atualizar(limpo);
        }
        return limpo;
    }

    /**
     * Cliente pelo id.
     */
    public Cliente buscar(int id) {
        return clientes.buscar(id);
    }

    /**
     * Todos os clientes em ordem de nome.
     */
    public List<Cliente> listar() {
        return clientes.listar();
    }

    /**
     * Locacoes do cliente, da mais recente para a mais antiga.
     */
    public List<Locacao> historico(int clienteId) {
        return locacoes.doCliente(clienteId);
    }

    /**
     * Soma das multas que o cliente ainda deve.
     */
    public BigDecimal multasPendentes(int clienteId) {
        return locacoes.multasPendentesDoCliente(clienteId);
    }

    /**
     * Reservas do cliente, da mais recente para a mais antiga.
     */
    public List<Reserva> reservas(int clienteId) {
        return reservas.doCliente(clienteId);
    }
}
