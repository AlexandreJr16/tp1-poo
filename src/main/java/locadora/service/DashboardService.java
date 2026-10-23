package locadora.service;

import locadora.model.Locacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/* - Painel (junta numeros dos outros services):
     resumo: numeros dos cards (ativas, atrasadas, multas em aberto, titulos, faturamento do mes, reservas prontas)
     proximasDevolucoes: locacoes no prazo, da mais proxima para a mais distante */
public class DashboardService {

    public record Resumo(int locacoesAtivas, int atrasadas, BigDecimal multasEmAberto, int titulos, BigDecimal faturamentoMes,
                         int reservasProntas) {
    }

    private static final int PROXIMAS_DEVOLUCOES = 8;

    private final LocacaoService locacoes;
    private final ReservaService reservas;
    private final TituloService titulos;

    public DashboardService(LocacaoService locacoes, ReservaService reservas, TituloService titulos) {
        this.locacoes = locacoes;
        this.reservas = reservas;
        this.titulos = titulos;
    }

    public Resumo resumo() {
        return new Resumo(locacoes.ativas().size(), locacoes.atrasadas().size(), locacoes.multasEmAberto(), titulos.listar().size(),
                locacoes.faturamentoDoMes(), reservas.prontas().size());
    }

    public List<Locacao> proximasDevolucoes() {
        LocalDate hoje = locacoes.hoje();
        return locacoes.ativas().stream().filter(l -> !l.atrasada(hoje)).limit(PROXIMAS_DEVOLUCOES).toList();
    }
}
