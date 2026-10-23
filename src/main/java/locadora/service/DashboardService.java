package locadora.service;

import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Locacao;
import locadora.model.Reserva;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class DashboardService {

    public record Resumo(int locacoesAtivas, int atrasadas, BigDecimal multasEmAberto, int titulos, BigDecimal faturamentoMes,
                         int reservasProntas) {
    }

    public DashboardService(LocacaoDao locacoes, TituloDao titulos, ReservaDao reservas, Clock relogio) {
    }

    public Resumo resumo() {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> proximasDevolucoes() {
        throw new UnsupportedOperationException();
    }

    public List<Locacao> atrasadas() {
        throw new UnsupportedOperationException();
    }

    public List<Reserva> reservasProntas() {
        throw new UnsupportedOperationException();
    }
}
