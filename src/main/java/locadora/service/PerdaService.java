package locadora.service;

import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.PerdaDao;
import locadora.dao.ReservaDao;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Locacao;
import locadora.model.Perda;
import locadora.model.Reserva;
import locadora.model.Situacao;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras do tratamento de perdas: o exemplar sai do acervo e, se estava alugado, o cliente e cobrado.
 */
public class PerdaService {

    private final PerdaDao perdas;
    private final ExemplarDao exemplares;
    private final LocacaoDao locacoes;
    private final ReservaDao reservas;
    private final Clock relogio;

    public PerdaService(PerdaDao perdas, ExemplarDao exemplares, LocacaoDao locacoes, ReservaDao reservas, Clock relogio) {
        this.perdas = perdas;
        this.exemplares = exemplares;
        this.locacoes = locacoes;
        this.reservas = reservas;
        this.relogio = relogio;
    }

    /**
     * Quanto o cliente pagaria se o exemplar alugado fosse dado como perdido hoje.
     */
    public BigDecimal previaCobranca(int exemplarId) {
        Locacao locacao = locacoes.ativaDoExemplar(exemplarId);
        return locacao == null ? Locacao.SEM_MULTA : locacao.multaAte(LocalDate.now(relogio)).add(locacao.getExemplar().getCusto());
    }

    /**
     * Marca o exemplar como perdido; se estava alugado, encerra a locacao cobrando custo mais multa; se estava separado para uma reserva, devolve a reserva para a fila.
     */
    public Perda registrar(int exemplarId, String motivo) {
        Funcionario funcionario = AuthService.exigirGerente();
        RegraException.se(motivo.isBlank(), "Informe o motivo da perda.");
        Exemplar exemplar = exemplares.buscar(exemplarId);
        RegraException.se(exemplar == null, "Exemplar não encontrado.");
        RegraException.se(exemplar.getSituacao() == Situacao.PERDIDO, "Este exemplar já foi dado como perdido.");
        LocalDate hoje = LocalDate.now(relogio);
        Locacao locacao = locacoes.ativaDoExemplar(exemplarId);
        Reserva reserva = reservas.prontaDoExemplar(exemplarId);
        if (locacao != null) {
            locacao.encerrarPorPerda(hoje, funcionario);
            exemplar = locacao.getExemplar();
        } else {
            if (reserva != null)
                reserva.voltarParaFila();
            exemplar.perder();
        }
        Perda perda = new Perda(0, exemplar, motivo.trim(), hoje, funcionario, locacao);
        perdas.registrar(perda, reserva);
        return perda;
    }

    /**
     * Historico de perdas, da mais recente para a mais antiga.
     */
    public List<Perda> listar() {
        return perdas.listar();
    }
}
