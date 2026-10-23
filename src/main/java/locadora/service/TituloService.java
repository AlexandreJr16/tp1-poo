package locadora.service;

import locadora.dao.AquisicaoDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.PerdaDao;
import locadora.dao.TituloDao;
import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.Titulo;

import java.time.Clock;
import java.util.List;
import java.util.Map;

public class TituloService {

    public static final List<String> TIPOS = List.of();

    public TituloService(TituloDao titulos, ExemplarDao exemplares, AquisicaoDao aquisicoes, LocacaoDao locacoes, PerdaDao perdas, Clock relogio) {
    }

    public Titulo salvar(String tipo, int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        throw new UnsupportedOperationException();
    }

    public Titulo buscar(int id) {
        throw new UnsupportedOperationException();
    }

    public List<Titulo> listar() {
        throw new UnsupportedOperationException();
    }

    public Map<Integer, Estoque> estoque() {
        throw new UnsupportedOperationException();
    }

    public Estoque estoqueDe(int tituloId) {
        throw new UnsupportedOperationException();
    }

    public List<Exemplar> exemplares(int tituloId) {
        throw new UnsupportedOperationException();
    }
}
