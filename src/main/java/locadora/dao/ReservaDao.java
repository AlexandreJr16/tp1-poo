package locadora.dao;

import locadora.model.Reserva;

import java.util.List;

public class ReservaDao {

    private final Conexao conexao;

    public ReservaDao(Conexao conexao, TituloDao titulos, ClienteDao clientes, FuncionarioDao funcionarios, ExemplarDao exemplares) {
        this.conexao = conexao;
    }

    public void inserir(Reserva reserva) {
        throw new UnsupportedOperationException();
    }

    public void atualizar(Reserva reserva) {
        throw new UnsupportedOperationException();
    }

    public void registrarCancelamento(Reserva cancelada, Reserva proxima) {
        throw new UnsupportedOperationException();
    }

    public Reserva buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> fila(int tituloId) {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> ativas() {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> doCliente(int clienteId) {
        throw new UnsupportedOperationException();
    }

    public Reserva prontaDoExemplar(int exemplarId) {
        throw new UnsupportedOperationException();
    }
}
