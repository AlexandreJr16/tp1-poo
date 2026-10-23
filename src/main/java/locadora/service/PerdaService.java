package locadora.service;

import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.PerdaDao;
import locadora.dao.ReservaDao;
import locadora.model.Perda;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class PerdaService {

    public PerdaService(PerdaDao perdas, ExemplarDao exemplares, LocacaoDao locacoes, ReservaDao reservas, Clock relogio) {
    }

    public BigDecimal previaCobranca(int exemplarId) {
        throw new UnsupportedOperationException();
    }

    public Perda registrar(int exemplarId, String motivo) {
        throw new UnsupportedOperationException();
    }

    public List<Perda> listar() {
        throw new UnsupportedOperationException();
    }
}
