package locadora.service;

import locadora.dao.AquisicaoDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.PerdaDao;
import locadora.dao.TituloDao;
import locadora.model.Aquisicao;
import locadora.model.BluRay;
import locadora.model.Cd;
import locadora.model.Dvd;
import locadora.model.Estoque;
import locadora.model.Evento;
import locadora.model.Exemplar;
import locadora.model.FitaVideo;
import locadora.model.Locacao;
import locadora.model.Perda;
import locadora.model.Situacao;
import locadora.model.Titulo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Regras do cadastro de titulos e consultas do acervo.
 */
public class TituloService {

    public static final List<String> TIPOS = List.of(BluRay.TIPO, Dvd.TIPO, Cd.TIPO, FitaVideo.TIPO);
    public static final int ANO_MINIMO = 1900;
    public static final int PRAZO_MAXIMO_DIAS = 30;

    private final TituloDao titulos;
    private final ExemplarDao exemplares;
    private final AquisicaoDao aquisicoes;
    private final LocacaoDao locacoes;
    private final PerdaDao perdas;
    private final Clock relogio;

    public TituloService(TituloDao titulos, ExemplarDao exemplares, AquisicaoDao aquisicoes, LocacaoDao locacoes, PerdaDao perdas, Clock relogio) {
        this.titulos = titulos;
        this.exemplares = exemplares;
        this.aquisicoes = aquisicoes;
        this.locacoes = locacoes;
        this.perdas = perdas;
        this.relogio = relogio;
    }

    /**
     * Valida e grava um titulo novo (id 0) ou editado, ja na subclasse do tipo escolhido.
     */
    public Titulo salvar(String tipo, int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        AuthService.exigirLogado();
        int anoMaximo = LocalDate.now(relogio).getYear() + 1;
        RegraException.se(nome.isBlank(), "Informe o nome do título.");
        RegraException.se(tipo == null || !TIPOS.contains(tipo), "Escolha o tipo de mídia.");
        RegraException.se(ano < ANO_MINIMO || ano > anoMaximo, "Informe um ano entre " + ANO_MINIMO + " e " + anoMaximo + ".");
        RegraException.se(prazoMaximoDias < 1 || prazoMaximoDias > PRAZO_MAXIMO_DIAS, "O prazo deve ser de 1 a " + PRAZO_MAXIMO_DIAS + " dias.");
        Titulo titulo = TituloDao.instanciar(tipo, id, nome.trim(), descricao.trim(), ano, prazoMaximoDias);
        if (id == 0)
            titulos.inserir(titulo);
        else
            titulos.atualizar(titulo);
        return titulo;
    }

    /**
     * Titulo pelo id.
     */
    public Titulo buscar(int id) {
        return titulos.buscar(id);
    }

    /**
     * Todos os titulos em ordem de nome.
     */
    public List<Titulo> listar() {
        return titulos.listar();
    }

    /**
     * Contagem por situacao dos exemplares de cada titulo.
     */
    public Map<Integer, Estoque> estoque() {
        return exemplares.estoquePorTitulo();
    }

    /**
     * Contagem de um titulo, vazia se ele ainda nao tem exemplares.
     */
    public Estoque estoqueDe(int tituloId) {
        return estoque().getOrDefault(tituloId, Estoque.VAZIO);
    }

    /**
     * Exemplares de um titulo.
     */
    public List<Exemplar> exemplares(int tituloId) {
        return exemplares.listarPorTitulo(tituloId);
    }

    /**
     * Titulo vazio de um tipo, para a tela mostrar os valores da midia.
     */
    // titulo vazio do tipo, so para mostrar na tela a multa e o valor que cada midia cobra
    public Titulo amostra(String tipo) {
        return TituloDao.instanciar(tipo, 0, "", "", ANO_MINIMO, 1);
    }

    /**
     * Exemplares que ainda nao foram perdidos, em ordem de titulo.
     */
    public List<Exemplar> exemplaresNoAcervo() {
        return Stream.of(Situacao.DISPONIVEL, Situacao.ALUGADO, Situacao.RESERVADO)
                .flatMap(situacao -> exemplares.listarPorSituacao(situacao).stream())
                .sorted(Comparator.comparing((Exemplar e) -> e.getTitulo().getNome(), String.CASE_INSENSITIVE_ORDER).thenComparing(Exemplar::getCodigo))
                .toList();
    }

    /**
     * Aquisicao, locacoes, devolucoes e perda de um exemplar em ordem cronologica.
     */
    public List<Evento> linhaDoTempo(int exemplarId) {
        List<Evento> eventos = new ArrayList<>();
        Aquisicao aquisicao = aquisicoes.buscarDoExemplar(exemplarId);
        if (aquisicao != null) {
            String descricao = "Adquirido de " + aquisicao.getFornecedor() + " por " + Formato.dinheiro(aquisicao.getCustoUnitario());
            eventos.add(new Evento(aquisicao.getData(), descricao, aquisicao.getFuncionario().getNome()));
        }
        Perda perda = perdas.doExemplar(exemplarId);
        int locacaoPerdida = perda != null && perda.getLocacao() != null ? perda.getLocacao().getId() : 0;
        for (Locacao locacao : locacoes.doExemplar(exemplarId)) {
            eventos.add(new Evento(locacao.getDataLocacao(), "Alugado para " + locacao.getCliente().getNome(),
                    locacao.getFuncionarioLocacao().getNome()));
            if (!locacao.ativa() && locacao.getId() != locacaoPerdida)
                eventos.add(new Evento(locacao.getDataDevolucao(), descricaoDevolucao(locacao), locacao.getFuncionarioDevolucao().getNome()));
        }
        if (perda != null)
            eventos.add(new Evento(perda.getData(), descricaoPerda(perda), perda.getFuncionario().getNome()));
        eventos.sort(Comparator.comparing(Evento::data));
        return eventos;
    }

    private static String descricaoDevolucao(Locacao locacao) {
        String devolucao = "Devolvido por " + locacao.getCliente().getNome();
        return locacao.getMulta().signum() > 0 ? devolucao + " com multa de " + Formato.dinheiro(locacao.getMulta()) : devolucao;
    }

    private static String descricaoPerda(Perda perda) {
        String descricao = "Dado como perdido: " + perda.getMotivo();
        Locacao locacao = perda.getLocacao();
        return locacao == null ? descricao
                : descricao + ". Cobrado " + Formato.dinheiro(locacao.getMulta()) + " de " + locacao.getCliente().getNome();
    }
}
