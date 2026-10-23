package locadora.dao;

import locadora.model.Locacao;
import locadora.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LocacaoDao {

    private final Conexao conexao;

    public LocacaoDao(Conexao conexao, ExemplarDao exemplares, ClienteDao clientes, FuncionarioDao funcionarios, ReservaDao reservas) {
        this.conexao = conexao;
    }

    public void registrar(Locacao locacao, Reserva atendida) {
    }

    public void registrarDevolucao(Locacao locacao, Reserva preparada) {
    }

    public void atualizar(Locacao locacao) {
    }

    public Locacao buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> listar() {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> ativas() {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> doCliente(int clienteId) {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> doExemplar(int exemplarId) {
        throw new UnsupportedOperationException();
    }

    public Locacao ativaDoExemplar(int exemplarId) {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> comMultaPendente() {
        throw new UnsupportedOperationException();
    }

    public BigDecimal faturamentoEntre(LocalDate inicio, LocalDate fim) {
        throw new UnsupportedOperationException();
    }
}
