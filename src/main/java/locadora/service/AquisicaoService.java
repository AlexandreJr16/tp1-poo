package locadora.service;

import locadora.dao.AquisicaoDao;
import locadora.dao.ExemplarDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Aquisicao;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class AquisicaoService {

    public AquisicaoService(AquisicaoDao aquisicoes, TituloDao titulos, ExemplarDao exemplares, ReservaDao reservas, Clock relogio) {
    }

    public Aquisicao registrar(int tituloId, int quantidade, BigDecimal custoUnitario, String fornecedor) {
        throw new UnsupportedOperationException();
    }

    public List<Aquisicao> listar() {
        throw new UnsupportedOperationException();
    }
}
