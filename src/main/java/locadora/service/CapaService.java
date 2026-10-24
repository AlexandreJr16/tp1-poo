package locadora.service;

import locadora.dao.TituloDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Capa e sinopse dos titulos. A internet e opcional: sem ela a busca so nao acha nada.
   - Busca (Wikipedia em portugues):
     buscar: capa e sinopse do primeiro resultado com imagem para "nome categoria" (ex.: "Matrix filme"),
             ou vazio (offline, bloqueado ou nada encontrado)
   - Guarda (o banco serve de cache):
     guardar: grava a capa de um titulo
     capa: capa de um titulo, ou nulo
     capas: capas de todos os titulos que tem, pelo id
     carregarEmbutidas: grava as capas que vem no jar (resources/capas/{id}.jpg), usado depois do seed */
public class CapaService {

    public static final int TAMANHO_DA_SINOPSE = 300;
    private static final Logger LOG = Logger.getLogger(CapaService.class.getName());
    private static final String AGENTE = "LocadoraTP1/1.0 (trabalho academico UFAM)";
    private static final Duration ESPERA = Duration.ofSeconds(5);
    private static final String CONSULTA = "?action=query&format=json&formatversion=2&utf8=1&generator=search&gsrlimit=3"
            + "&prop=pageimages%7Cextracts&piprop=thumbnail&pithumbsize=400&exintro=1&explaintext=1&exsentences=2&exlimit=3&gsrsearch=";
    private static final Pattern INDICE = Pattern.compile("\"index\":(\\d+)");
    private static final Pattern IMAGEM = Pattern.compile("\"thumbnail\":\\{\"source\":\"([^\"]+)\"");
    private static final Pattern EXTRATO = Pattern.compile("\"extract\":\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern ESCAPE = Pattern.compile("\\\\(u[0-9a-fA-F]{4}|.)");

    public record Achado(byte[] imagem, String sinopse) {
    }

    record Pagina(String imagem, String sinopse) {
    }

    private final TituloDao titulos;
    private final String endereco;
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public CapaService(TituloDao titulos, String endereco) {
        this.titulos = titulos;
        this.endereco = endereco;
    }

    public Optional<Achado> buscar(String nome, String categoria) {
        if (nome == null || nome.isBlank())
            return Optional.empty();
        String termo = nome.trim() + " " + categoria;
        try {
            Optional<Pagina> pagina = ler(new String(baixar(endereco + CONSULTA + URLEncoder.encode(termo, StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            if (pagina.isEmpty())
                return Optional.empty();
            return Optional.of(new Achado(baixar(pagina.get().imagem()), pagina.get().sinopse()));
        } catch (IOException e) {
            LOG.log(Level.FINE, "Capa nao encontrada para " + termo, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    public void guardar(int tituloId, byte[] imagem) {
        titulos.salvarCapa(tituloId, imagem);
    }

    public byte[] capa(int tituloId) {
        return titulos.capa(tituloId);
    }

    public Map<Integer, byte[]> capas() {
        return titulos.capas();
    }

    public void carregarEmbutidas() {
        titulos.listar().forEach(titulo -> {
            try (InputStream entrada = CapaService.class.getResourceAsStream("/capas/" + titulo.getId() + ".jpg")) {
                if (entrada != null)
                    titulos.salvarCapa(titulo.getId(), entrada.readAllBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    // resposta da API: escolhe, na ordem da propria Wikipedia, a primeira pagina que tem imagem
    static Optional<Pagina> ler(String json) {
        return Arrays.stream(json.split("\\{\"pageid\":"))
                .skip(1)
                .filter(pagina -> IMAGEM.matcher(pagina).find())
                .min(Comparator.comparingInt(pagina -> Integer.parseInt(primeiro(INDICE, pagina, "0"))))
                .map(pagina -> new Pagina(primeiro(IMAGEM, pagina, ""), sinopse(primeiro(EXTRATO, pagina, ""))));
    }

    // texto do JSON sem escapes, cortado no fim de uma frase para caber no cadastro
    static String sinopse(String extrato) {
        String texto = ESCAPE.matcher(extrato).replaceAll(escape -> Matcher.quoteReplacement(desescapar(escape.group(1))))
                .replace("\u200b", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (texto.length() <= TAMANHO_DA_SINOPSE)
            return texto;
        String inicio = texto.substring(0, TAMANHO_DA_SINOPSE);
        int fimDaFrase = inicio.lastIndexOf(". ");
        return fimDaFrase > 0 ? inicio.substring(0, fimDaFrase + 1) : inicio.substring(0, inicio.lastIndexOf(' ')) + "…";
    }

    private static String desescapar(String escape) {
        if (escape.length() == 5)
            return String.valueOf((char) Integer.parseInt(escape.substring(1), 16));
        return "ntr".contains(escape) ? " " : escape;
    }

    private static String primeiro(Pattern padrao, String texto, String padraoSeFaltar) {
        Matcher achado = padrao.matcher(texto);
        return achado.find() ? achado.group(1) : padraoSeFaltar;
    }

    private byte[] baixar(String url) throws IOException, InterruptedException {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url)).timeout(ESPERA).header("User-Agent", AGENTE).build();
        HttpResponse<byte[]> resposta = cliente.send(pedido, HttpResponse.BodyHandlers.ofByteArray());
        if (resposta.statusCode() != 200)
            throw new IOException("Resposta " + resposta.statusCode() + " de " + url);
        return resposta.body();
    }
}
