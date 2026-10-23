package locadora.service;

import locadora.dao.AquisicaoDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.PerdaDao;
import locadora.dao.TituloDao;
import locadora.model.BluRay;
import locadora.model.Cd;
import locadora.model.Dvd;
import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.FitaVideo;
import locadora.model.Titulo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/* - Cadastro:
     salvar: valida e grava um titulo novo (id 0) ou editado, ja na subclasse do tipo escolhido
   - Consulta:
     buscar: titulo pelo id
     listar: todos os titulos em ordem de nome
     estoque: contagem por situacao dos exemplares de cada titulo
     estoqueDe: contagem de um titulo, vazia se ele ainda nao tem exemplares
     exemplares: exemplares de um titulo */
public class TituloService {

    public static final List<String> TIPOS = List.of(BluRay.TIPO, Dvd.TIPO, Cd.TIPO, FitaVideo.TIPO);
    private static final int ANO_MINIMO = 1900;
    private static final int PRAZO_MAXIMO_DIAS = 30;

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

    public Titulo buscar(int id) {
        return titulos.buscar(id);
    }

    public List<Titulo> listar() {
        return titulos.listar();
    }

    public Map<Integer, Estoque> estoque() {
        return exemplares.estoquePorTitulo();
    }

    public Estoque estoqueDe(int tituloId) {
        return estoque().getOrDefault(tituloId, Estoque.VAZIO);
    }

    public List<Exemplar> exemplares(int tituloId) {
        return exemplares.listarPorTitulo(tituloId);
    }
}
