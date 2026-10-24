package locadora.service;

import locadora.dao.TituloDao;

import java.util.Map;
import java.util.Optional;

public class CapaService {

    public static final int TAMANHO_DA_SINOPSE = 300;

    public record Achado(byte[] imagem, String sinopse) {
    }

    record Pagina(String imagem, String sinopse) {
    }

    private final TituloDao titulos;
    private final String endereco;

    public CapaService(TituloDao titulos, String endereco) {
        this.titulos = titulos;
        this.endereco = endereco;
    }

    public Optional<Achado> buscar(String nome, String tipo) {
        return Optional.of(new Achado(new byte[0], ""));
    }

    public void guardar(int tituloId, byte[] imagem) {
    }

    public byte[] capa(int tituloId) {
        return new byte[0];
    }

    public Map<Integer, byte[]> capas() {
        return Map.of();
    }

    static Optional<Pagina> ler(String json) {
        return Optional.of(new Pagina("", ""));
    }

    static String sinopse(String extrato) {
        return "";
    }
}
