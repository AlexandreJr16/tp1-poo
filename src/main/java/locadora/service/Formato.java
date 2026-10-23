package locadora.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/* Formatos brasileiros usados nas mensagens e nas telas.
   dinheiro: R$ 1.234,50
   data: 01/10/2026 (vazio para data nula)
   normalizar: texto sem acento, sem espacos nas pontas e em minusculas, para comparar em buscas */
public final class Formato {

    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() {
    }

    public static String dinheiro(BigDecimal valor) {
        return "R$ " + String.format(BRASIL, "%,.2f", valor);
    }

    public static String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }

    public static BigDecimal lerDinheiro(String texto) {
        return null;
    }

    public static String dataPorExtenso(LocalDate data) {
        return "";
    }

    public static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto == null ? "" : texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(BRASIL);
    }
}
