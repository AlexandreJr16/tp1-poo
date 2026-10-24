package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.model.Cliente;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Locacao;
import locadora.model.Recomendacao;
import locadora.model.Reserva;
import locadora.model.Situacao;
import locadora.model.SituacaoReserva;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* - Locacao:
     locar: registra a locacao para o funcionario logado, se cliente e exemplar estiverem aptos
     previaMulta: multa que a devolucao cobraria hoje
     devolver: fecha a locacao com o funcionario logado e separa o exemplar para o primeiro da fila
     pagarMulta: registra o pagamento da multa hoje
     pagarMultasDoCliente: quita de uma vez todas as multas pendentes de um cliente
     renovar: estende o prazo uma vez, se nao estiver atrasada nem houver fila
   - Consulta:
     alugaveisPara: exemplares disponiveis mais os separados para o proprio cliente
     buscar: locacao pelo id
     listar: todas as locacoes, da mais recente para a mais antiga
     ativas: locacoes nao devolvidas, pela data prevista
     atrasadas: locacoes ativas que ja passaram da data prevista
     comMultaPendente: locacoes devolvidas com multa ainda nao paga
     multasEmAberto: soma das multas nao pagas
     faturamentoDoMes: locacoes feitas mais multas pagas no mes atual
     hoje: dia atual pelo relogio do sistema
   - Recomendacao:
     recomendacoes: ate 5 titulos que o cliente ainda nao alugou, primeiro os de quem tem gosto parecido,
                    depois os mais alugados da locadora */
public class LocacaoService {

    public static final int LIMITE_DE_RECOMENDACOES = 5;
    private static final int LIMITE_LOCACOES_ATIVAS = 3;

    private final LocacaoDao locacoes;
    private final ExemplarDao exemplares;
    private final ClienteDao clientes;
    private final ReservaDao reservas;
    private final Clock relogio;

    public LocacaoService(LocacaoDao locacoes, ExemplarDao exemplares, ClienteDao clientes, ReservaDao reservas, Clock relogio) {
        this.locacoes = locacoes;
        this.exemplares = exemplares;
        this.clientes = clientes;
        this.reservas = reservas;
        this.relogio = relogio;
    }

    public Locacao locar(int clienteId, int exemplarId) {
        Funcionario funcionario = AuthService.exigirLogado();
        Cliente cliente = clienteApto(clienteId);
        Exemplar exemplar = exemplares.buscar(exemplarId);
        RegraException.se(exemplar == null, "Exemplar não encontrado.");
        RegraException.se(exemplar.getSituacao() == Situacao.ALUGADO, "Este exemplar já está alugado.");
        RegraException.se(exemplar.getSituacao() == Situacao.PERDIDO, "Este exemplar foi dado como perdido.");
        Reserva reserva = exemplar.getSituacao() == Situacao.RESERVADO ? reservas.prontaDoExemplar(exemplarId) : null;
        if (reserva != null) {
            RegraException.se(reserva.getCliente().getId() != clienteId,
                    "Este exemplar está separado para a reserva de " + reserva.getCliente().getNome() + ".");
            reserva.atender();
        }
        Locacao locacao = Locacao.abrir(exemplar, cliente, funcionario, hoje());
        locacoes.registrar(locacao, reserva);
        return locacao;
    }

    public BigDecimal previaMulta(int locacaoId) {
        return ativa(locacaoId).multaAte(hoje());
    }

    public Locacao devolver(int locacaoId) {
        Funcionario funcionario = AuthService.exigirLogado();
        Locacao locacao = ativa(locacaoId);
        locacao.devolver(hoje(), funcionario);
        Reserva proxima = reservas.fila(locacao.getExemplar().getTitulo().getId()).stream().findFirst().orElse(null);
        if (proxima != null)
            proxima.preparar(locacao.getExemplar());
        locacoes.registrarDevolucao(locacao, proxima);
        return locacao;
    }

    public void pagarMulta(int locacaoId) {
        AuthService.exigirLogado();
        Locacao locacao = existente(locacaoId);
        RegraException.se(!locacao.multaPendente(), "Esta locação não tem multa pendente.");
        locacao.pagarMulta(hoje());
        locacoes.atualizar(locacao);
    }

    public void pagarMultasDoCliente(int clienteId) {
        AuthService.exigirLogado();
        List<Locacao> pendentes = locacoes.doCliente(clienteId).stream().filter(Locacao::multaPendente).toList();
        RegraException.se(pendentes.isEmpty(), "O cliente não tem multas pendentes.");
        pendentes.forEach(l -> l.pagarMulta(hoje()));
        locacoes.atualizarTodas(pendentes);
    }

    public Locacao renovar(int locacaoId) {
        AuthService.exigirLogado();
        Locacao locacao = ativa(locacaoId);
        RegraException.se(locacao.isRenovada(), "Esta locação já foi renovada uma vez.");
        RegraException.se(locacao.atrasada(hoje()), "Locação atrasada não pode ser renovada.");
        RegraException.se(!reservas.fila(locacao.getExemplar().getTitulo().getId()).isEmpty(), "Há clientes na fila de reserva deste título.");
        locacao.renovar(hoje());
        locacoes.atualizar(locacao);
        return locacao;
    }

    public List<Exemplar> alugaveisPara(int clienteId) {
        Stream<Exemplar> separados = reservas.doCliente(clienteId).stream()
                .filter(r -> r.getSituacao() == SituacaoReserva.PRONTA)
                .map(Reserva::getExemplar);
        return Stream.concat(separados, exemplares.listarPorSituacao(Situacao.DISPONIVEL).stream())
                .sorted(Comparator.comparing((Exemplar e) -> e.getTitulo().getNome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Exemplar::getCodigo))
                .toList();
    }

    public Locacao buscar(int id) {
        return locacoes.buscar(id);
    }

    public List<Locacao> listar() {
        return locacoes.listar();
    }

    public List<Locacao> ativas() {
        return locacoes.ativas();
    }

    public List<Locacao> atrasadas() {
        LocalDate hoje = hoje();
        return ativas().stream().filter(l -> l.atrasada(hoje)).toList();
    }

    public List<Locacao> comMultaPendente() {
        return locacoes.comMultaPendente();
    }

    public BigDecimal multasEmAberto() {
        return comMultaPendente().stream().map(Locacao::getMulta).reduce(Locacao.SEM_MULTA, BigDecimal::add);
    }

    public BigDecimal faturamentoDoMes() {
        LocalDate hoje = hoje();
        return locacoes.faturamentoEntre(hoje.withDayOfMonth(1), hoje.withDayOfMonth(hoje.lengthOfMonth()));
    }

    public List<Recomendacao> recomendacoes(int clienteId) {
        Set<Integer> jaVistos = locacoes.doCliente(clienteId).stream()
                .map(l -> l.getExemplar().getTitulo().getId())
                .collect(Collectors.toCollection(HashSet::new));
        Stream<Recomendacao> populares = locacoes.maisAlugados().stream().map(titulo -> new Recomendacao(titulo, null));
        return Stream.concat(locacoes.recomendacoes(clienteId, LIMITE_DE_RECOMENDACOES).stream(), populares)
                .filter(recomendacao -> jaVistos.add(recomendacao.titulo().getId()))
                .limit(LIMITE_DE_RECOMENDACOES)
                .toList();
    }

    public LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    private Cliente clienteApto(int clienteId) {
        Cliente cliente = clientes.buscar(clienteId);
        RegraException.se(cliente == null, "Cliente não encontrado.");
        RegraException.se(!cliente.isAtivo(), "Cliente inativo não pode alugar.");
        BigDecimal devendo = locacoes.multasPendentesDoCliente(clienteId);
        RegraException.se(devendo.signum() > 0,
                "O cliente tem " + Formato.dinheiro(devendo) + " em multas pendentes. Registre o pagamento antes de alugar.");
        long abertas = locacoes.doCliente(clienteId).stream().filter(Locacao::ativa).count();
        RegraException.se(abertas >= LIMITE_LOCACOES_ATIVAS, "O cliente já tem " + LIMITE_LOCACOES_ATIVAS + " locações em aberto.");
        return cliente;
    }

    private Locacao existente(int locacaoId) {
        Locacao locacao = locacoes.buscar(locacaoId);
        RegraException.se(locacao == null, "Locação não encontrada.");
        return locacao;
    }

    private Locacao ativa(int locacaoId) {
        Locacao locacao = existente(locacaoId);
        RegraException.se(!locacao.ativa(), "Esta locação já foi devolvida.");
        return locacao;
    }
}
