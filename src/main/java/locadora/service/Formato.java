package locadora.service;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatacao de dinheiro, datas e textos para as telas e comparacao de textos sem acento.
 */
public final class Formato {

    private static final int MINIMO_PARA_APROXIMAR = 4;
    private static final int SEMELHANCA_MINIMA = 80;
    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter POR_EXTENSO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", BRASIL);

    private Formato() {
    }

    /**
     * R$ 1.234,50.
     */
    public static String dinheiro(BigDecimal valor) {
        return "R$ " + String.format(BRASIL, "%,.2f", valor);
    }

    /**
     * 01/10/2026 (vazio para data nula).
     */
    public static String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }

    /**
     * Valor digitado (29,90 ou R$ 1.234,50) em BigDecimal, ou erro amigavel.
     */
    public static BigDecimal lerDinheiro(String texto) {
        String numero = texto == null ? "" : texto.replace("R$", "").replace(".", "").replace(",", ".").trim();
        try {
            return new BigDecimal(numero).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new RegraException("Informe um valor como 29,90.");
        }
    }

    /**
     * Quinta-feira, 1 de outubro de 2026.
     */
    public static String dataPorExtenso(LocalDate data) {
        return data.format(POR_EXTENSO);
    }

    /**
     * O texto contem o termo ou se parece com ele (tolera acento, pontuacao e erro de digitacao).
     */
    public static boolean parecido(String termo, String texto) {
        String procurado = paraComparar(termo);
        String alvo = paraComparar(texto);
        return alvo.contains(procurado)
                || procurado.length() >= MINIMO_PARA_APROXIMAR && FuzzySearch.tokenSetPartialRatio(procurado, alvo) >= SEMELHANCA_MINIMA;
    }

    /**
     * Texto sem acento, sem espacos nas pontas e em minusculas, para comparar em buscas.
     */
    public static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto == null ? "" : texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(BRASIL);
    }

    /**
     * Pontuacao e hifen viram espaco, para "contra-ataca" e "contra ataca" serem o mesmo texto.
     */
    private static String paraComparar(String texto) {
        return normalizar(texto).replaceAll("[^\\p{Alnum}]+", " ").trim();
    }
}
