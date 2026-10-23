package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.ExemplarDao;
import locadora.dao.TituloDao;

import java.util.List;

public class BuscaService {

    public static final int LIMITE = 12;

    public enum Tipo { TITULO, CLIENTE, EXEMPLAR }

    public record Resultado(Tipo tipo, int id, String rotulo, String detalhe) {
    }

    public BuscaService(TituloDao titulos, ClienteDao clientes, ExemplarDao exemplares) {
    }

    public List<Resultado> buscar(String texto) {
        throw new UnsupportedOperationException();
    }
}
