package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.model.Cliente;
import locadora.model.Locacao;
import locadora.model.Reserva;

import java.math.BigDecimal;
import java.util.List;

public class ClienteService {

    public ClienteService(ClienteDao clientes, LocacaoDao locacoes, ReservaDao reservas) {
    }

    public Cliente salvar(Cliente cliente) {
        throw new UnsupportedOperationException();
    }

    public Cliente buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public List<Cliente> listar() {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> historico(int clienteId) {
        throw new UnsupportedOperationException();
    }

    public BigDecimal multasPendentes(int clienteId) {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> reservas(int clienteId) {
        throw new UnsupportedOperationException();
    }
}
