package locadora.service;

import locadora.dao.AquisicaoDao;
import locadora.dao.ExemplarDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Aquisicao;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Reserva;
import locadora.model.Situacao;
import locadora.model.Titulo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras das aquisicoes (so gerente): quantidade, custo e fornecedor validos, exemplares com codigos sequenciais e reservas da fila atendidas na hora.
 */
public class AquisicaoService {

    public static final int QUANTIDADE_MAXIMA = 100;

    private final AquisicaoDao aquisicoes;
    private final TituloDao titulos;
    private final ExemplarDao exemplares;
    private final ReservaDao reservas;
    private final Clock relogio;

    public AquisicaoService(AquisicaoDao aquisicoes, TituloDao titulos, ExemplarDao exemplares, ReservaDao reservas, Clock relogio) {
        this.aquisicoes = aquisicoes;
        this.titulos = titulos;
        this.exemplares = exemplares;
        this.reservas = reservas;
        this.relogio = relogio;
    }

    /**
     * Gera N exemplares com codigos sequenciais, separa os primeiros para a fila de reservas e grava tudo junto.
     */
    public Aquisicao registrar(int tituloId, int quantidade, BigDecimal custoUnitario, String fornecedor) {
        Funcionario funcionario = AuthService.exigirGerente();
        Titulo titulo = titulos.buscar(tituloId);
        RegraException.se(titulo == null, "Título não encontrado.");
        RegraException.se(quantidade < 1 || quantidade > QUANTIDADE_MAXIMA, "A quantidade deve ser de 1 a " + QUANTIDADE_MAXIMA + ".");
        RegraException.se(custoUnitario == null || custoUnitario.signum() <= 0, "Informe um custo unitário maior que zero.");
        RegraException.se(fornecedor.isBlank(), "Informe o fornecedor.");
        Aquisicao aquisicao = new Aquisicao(0, titulo, quantidade, custoUnitario, fornecedor.trim(), LocalDate.now(relogio), funcionario);
        List<Exemplar> novos = novosExemplares(titulo, quantidade, custoUnitario);
        List<Reserva> fila = reservas.fila(tituloId);
        List<Reserva> atendidas = fila.subList(0, Math.min(fila.size(), novos.size()));
        for (int i = 0; i < atendidas.size(); i++)
            atendidas.get(i).preparar(novos.get(i));
        aquisicoes.registrar(aquisicao, novos, atendidas);
        return aquisicao;
    }

    /**
     * Historico de aquisicoes, da mais recente para a mais antiga.
     */
    public List<Aquisicao> listar() {
        return aquisicoes.listar();
    }

    private List<Exemplar> novosExemplares(Titulo titulo, int quantidade, BigDecimal custo) {
        int sequencia = exemplares.proximaSequencia(titulo.getId());
        List<Exemplar> novos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++)
            novos.add(new Exemplar(0, Exemplar.codigo(titulo.getId(), sequencia + i), titulo, custo, Situacao.DISPONIVEL));
        return novos;
    }
}
