package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.model.Exemplar;
import locadora.model.Locacao;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class LocacaoService {

    public LocacaoService(LocacaoDao locacoes, ExemplarDao exemplares, ClienteDao clientes, ReservaDao reservas, Clock relogio) {
    }

    public Locacao locar(int clienteId, int exemplarId) {
        throw new UnsupportedOperationException();
    }

    public BigDecimal previaMulta(int locacaoId) {
        throw new UnsupportedOperationException();
    }

    public Locacao devolver(int locacaoId) {
        throw new UnsupportedOperationException();
    }

    public void pagarMulta(int locacaoId) {
        throw new UnsupportedOperationException();
    }

    public Locacao renovar(int locacaoId) {
        throw new UnsupportedOperationException();
    }

    public List<Exemplar> alugaveisPara(int clienteId) {
        throw new UnsupportedOperationException();
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

    public List<Locacao> atrasadas() {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> comMultaPendente() {
        throw new UnsupportedOperationException();
    }

    public LocalDate hoje() {
        throw new UnsupportedOperationException();
    }
}
