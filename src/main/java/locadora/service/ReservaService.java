package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Reserva;

import java.time.Clock;
import java.util.List;

public class ReservaService {

    public ReservaService(ReservaDao reservas, TituloDao titulos, ClienteDao clientes, ExemplarDao exemplares, LocacaoDao locacoes, Clock relogio) {
    }

    public Reserva reservar(int clienteId, int tituloId) {
        throw new UnsupportedOperationException();
    }

    public void cancelar(int reservaId) {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> ativas() {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> prontas() {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> fila(int tituloId) {
        throw new UnsupportedOperationException();
    }

    public int posicao(Reserva reserva) {
        throw new UnsupportedOperationException();
    }
}
