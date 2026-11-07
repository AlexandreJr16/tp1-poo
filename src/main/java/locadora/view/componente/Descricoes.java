package locadora.view.componente;

import locadora.model.Exemplar;
import locadora.model.Locacao;

import java.time.LocalDate;

/**
 * Textos curtos repetidos em varias telas.
 */
public final class Descricoes {

    private Descricoes() {
    }

    /**
     * Como uma locacao esta hoje (em aberto, atrasada, multa pendente...).
     */
    public static String situacao(Locacao locacao, LocalDate hoje) {
        if (locacao.atrasada(hoje))
            return "Atrasada há " + dias(locacao.diasAtraso(hoje));
        if (locacao.ativa())
            return locacao.isRenovada() ? "Em aberto (renovada)" : "Em aberto";
        if (locacao.multaPendente())
            return "Multa pendente";
        return locacao.multaPaga() ? "Devolvida, multa paga" : "Devolvida";
    }

    /**
     * Codigo e nome do titulo do exemplar.
     */
    public static String exemplar(Exemplar exemplar) {
        return exemplar.getCodigo() + " · " + exemplar.getTitulo().getNome();
    }

    private static String dias(long quantidade) {
        return quantidade + (quantidade == 1 ? " dia" : " dias");
    }

    /**
     * Texto puro que quebra linha na largura dada (escapa os simbolos especiais para o HTML do rotulo).
     */
    // no HTML do Swing, pt equivale a 1 pixel
    public static String quebrando(String texto, int largura) {
        String seguro = texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='width: " + largura + "pt'>" + seguro + "</div></html>";
    }
}
