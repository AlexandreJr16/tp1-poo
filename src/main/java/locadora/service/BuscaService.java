package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.ExemplarDao;
import locadora.dao.TituloDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* - Busca global (Ctrl+K):
     buscar: acha titulos pelo nome, clientes pelo nome ou cpf e exemplares pelo codigo,
             ignorando acentos, maiusculas e pequenos erros de digitacao; o id de um exemplar encontrado e o do titulo dele */
public class BuscaService {

    public static final int LIMITE = 12;
    private static final int MINIMO_DE_LETRAS = 2;
    private static final int MINIMO_DE_DIGITOS_DO_CPF = 3;

    public enum Tipo {
        TITULO("Título"),
        CLIENTE("Cliente"),
        EXEMPLAR("Exemplar");

        private final String rotulo;

        Tipo(String rotulo) {
            this.rotulo = rotulo;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    public record Resultado(Tipo tipo, int id, String rotulo, String detalhe) {
    }

    private final TituloDao titulos;
    private final ClienteDao clientes;
    private final ExemplarDao exemplares;

    public BuscaService(TituloDao titulos, ClienteDao clientes, ExemplarDao exemplares) {
        this.titulos = titulos;
        this.clientes = clientes;
        this.exemplares = exemplares;
    }

    public List<Resultado> buscar(String texto) {
        String termo = Formato.normalizar(texto);
        if (termo.length() < MINIMO_DE_LETRAS)
            return List.of();
        String digitos = termo.replaceAll("\\D", "");
        List<Resultado> resultados = new ArrayList<>();
        titulos.listar().stream()
                .filter(t -> Formato.parecido(termo, t.getNome()))
                .forEach(t -> resultados.add(new Resultado(Tipo.TITULO, t.getId(), t.getNome(), t.tipo() + " · " + t.getAno())));
        clientes.listar().stream()
                .filter(c -> Formato.parecido(termo, c.getNome()) || digitos.length() >= MINIMO_DE_DIGITOS_DO_CPF && c.getCpf().contains(digitos))
                .forEach(c -> resultados.add(new Resultado(Tipo.CLIENTE, c.getId(), c.getNome(), "CPF " + c.cpfFormatado())));
        if (termo.matches("t\\d.*")) {
            exemplares.listarPorCodigo(termo.toUpperCase(Locale.ROOT))
                    .forEach(e -> resultados.add(new Resultado(Tipo.EXEMPLAR, e.getTitulo().getId(), e.getCodigo(),
                            e.getTitulo().getNome() + " · " + e.getSituacao())));
        }
        return resultados.stream().limit(LIMITE).toList();
    }
}
