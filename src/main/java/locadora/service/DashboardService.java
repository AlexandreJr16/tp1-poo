package locadora.service;

import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Locacao;
import locadora.model.Reserva;
import locadora.model.SituacaoReserva;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/* - Painel:
     resumo: numeros dos cards (ativas, atrasadas, multas em aberto, titulos, faturamento do mes, reservas prontas)
     proximasDevolucoes: locacoes no prazo, da mais proxima para a mais distante
     atrasadas: locacoes vencidas, da mais atrasada para a menos atrasada
     reservasProntas: reservas com exemplar separado esperando a retirada */
public class DashboardService {

    public record Resumo(int locacoesAtivas, int atrasadas, BigDecimal multasEmAberto, int titulos, BigDecimal faturamentoMes,
                         int reservasProntas) {
    }

    private static final int PROXIMAS_DEVOLUCOES = 8;
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final LocacaoDao locacoes;
    private final TituloDao titulos;
    private final ReservaDao reservas;
    private final Clock relogio;

    public DashboardService(LocacaoDao locacoes, TituloDao titulos, ReservaDao reservas, Clock relogio) {
        this.locacoes = locacoes;
        this.titulos = titulos;
        this.reservas = reservas;
        this.relogio = relogio;
    }

    public Resumo resumo() {
        LocalDate hoje = LocalDate.now(relogio);
        List<Locacao> ativas = locacoes.ativas();
        int atrasadas = (int) ativas.stream().filter(l -> l.atrasada(hoje)).count();
        BigDecimal multas = locacoes.comMultaPendente().stream().map(Locacao::getMulta).reduce(ZERO, BigDecimal::add);
        BigDecimal faturamento = locacoes.faturamentoEntre(hoje.withDayOfMonth(1), hoje.withDayOfMonth(hoje.lengthOfMonth()));
        return new Resumo(ativas.size(), atrasadas, multas, titulos.listar().size(), faturamento, reservasProntas().size());
    }

    public List<Locacao> proximasDevolucoes() {
        LocalDate hoje = LocalDate.now(relogio);
        return locacoes.ativas().stream().filter(l -> !l.atrasada(hoje)).limit(PROXIMAS_DEVOLUCOES).toList();
    }

    public List<Locacao> atrasadas() {
        LocalDate hoje = LocalDate.now(relogio);
        return locacoes.ativas().stream().filter(l -> l.atrasada(hoje)).toList();
    }

    public List<Reserva> reservasProntas() {
        return reservas.ativas().stream().filter(r -> r.getSituacao() == SituacaoReserva.PRONTA).toList();
    }
}
